/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.file.ArgonFileManagement;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonDecoder;
import com.metservice.argon.json.JsonEncoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
class DiskMruTable {

	static final String p_fileName = "fn";
	static final String p_lastAccess = "la";
	static final String p_lastModified = "lm";
	static final String p_fileKB = "kb";
	static final String p_contentType = "ct";
	static final String p_trackers = "trackers";

	public static final String CheckpointFileName = "checkpoint.json";
	public static final int CheckpointQuotaBc = 1024 * CArgon.M;

	public static final int GoalPctLo = 50;
	public static final int GoalPctHi = 99;
	public static final int FileLimitLo = 1;
	public static final int FileLimitHi = 128 * CArgon.K;

	private static final String TryDecodeCp = "Decode MRU Checkpoint";
	private static final String CsqScrub = "Wipe contents of cache";

	private static State newState(IArgonFileProbe probe, File cndir, int cap) {
		final File srcFile = new File(cndir, CheckpointFileName);
		final Binary oBinary = Binary.createFromFile(probe, srcFile, CheckpointQuotaBc);
		State oState = null;
		try {
			if (oBinary != null) {
				final JsonObject src = JsonDecoder.Default.decodeObject(oBinary.newStringUTF8());
				oState = new State(src);
			}
		} catch (final JsonSchemaException ex) {
			final Ds ds = Ds.triedTo(TryDecodeCp, ex, CsqScrub);
			probe.warnFile(ds, srcFile);
		} catch (final ArgonFormatException ex) {
			final Ds ds = Ds.triedTo(TryDecodeCp, ex, CsqScrub);
			probe.warnFile(ds, srcFile);
		}
		if (oState != null) return oState;
		return new State(probe, cndir, cap);
	}

	public static DiskMruTable newInstance(IArgonFileProbe probe, File cndir, long bcQuota, int popLimit, int goalPct) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (cndir == null) throw new IllegalArgumentException("object is null");
		final long cbcQuota = Math.max(0L, bcQuota);
		final int cpopLimit = Math.max(FileLimitLo, Math.min(FileLimitHi, popLimit));
		final int cpctGoal = Math.max(GoalPctLo, Math.min(GoalPctHi, goalPct));
		final long bcGoal = (cbcQuota * cpctGoal) / 100L;
		final int popGoal = (cpopLimit * cpctGoal) / 100;
		final State state = newState(probe, cndir, cpopLimit);
		return new DiskMruTable(probe, cndir, cbcQuota, cpopLimit, bcGoal, popGoal, state);
	}

	private JsonObject newCheckpointJson() {
		m_lock.lock();
		try {
			return m_state.newJsonCheckpoint();
		} finally {
			m_lock.unlock();
		}
	}

	private List<String> newPurgeAgenda() {
		m_lock.lock();
		try {
			final boolean due = m_state.isPurgeDue(m_bcQuota, m_popCacheFileLimit);
			if (due) return m_state.newPurgeFileNames(m_bcCacheSizeGoal, m_popCacheFileGoal);
			return Collections.emptyList();
		} finally {
			m_lock.unlock();
		}
	}

	private void purge(List<String> zlAgenda) {
		assert probe != null;
		assert cndir != null;
		assert zlAgenda != null;
		final int agendaCount = zlAgenda.size();
		for (int i = 0; i < agendaCount; i++) {
			final String qccFileName = zlAgenda.get(i);
			m_lock.lock();
			try {
				m_state.purge(probe, cndir, qccFileName);
			} finally {
				m_lock.unlock();
			}
		}
	}

	public void checkpoint() {
		final JsonObject cp = newCheckpointJson();
		final Binary out = Binary.newFromStringUTF8(JsonEncoder.Default.encode(cp));
		final File destFile = new File(cndir, CheckpointFileName);
		out.save(probe, destFile, false);
	}

	public Descriptor findDescriptor(String qccFileName, long tsNow) {
		if (qccFileName == null || qccFileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_lock.lock();
		try {
			final Tracker oTracker = m_state.findTracker(qccFileName, tsNow);
			return (oTracker == null) ? null : oTracker.newDescriptor();
		} finally {
			m_lock.unlock();
		}
	}

	public Descriptor newDescriptor(String qccFileName, long tsLastModified, int bcFile, String qlcContentType, long tsNow) {
		if (qccFileName == null || qccFileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qlcContentType == null || qlcContentType.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		m_lock.lock();
		try {
			final Tracker tracker = m_state.putTracker(qccFileName, tsLastModified, bcFile, qlcContentType, tsNow);
			return tracker.newDescriptor();
		} finally {
			m_lock.unlock();
		}
	}

	public void purge() {
		purge(newPurgeAgenda());
	}

	public DiskMruTable(IArgonFileProbe probe, File cndir, long bcQuota, int popLimit, long bcGoal, int popGoal, State state) {
		assert probe != null;
		assert cndir != null;
		assert state != null;
		this.probe = probe;
		this.cndir = cndir;
		m_bcQuota = bcQuota;
		m_popCacheFileLimit = popLimit;
		m_bcCacheSizeGoal = bcGoal;
		m_popCacheFileGoal = popGoal;
		m_state = state;
	}
	final IArgonFileProbe probe;
	final File cndir;
	private final long m_bcQuota;
	private final int m_popCacheFileLimit;
	private final long m_bcCacheSizeGoal;
	private final int m_popCacheFileGoal;
	private final Lock m_lock = new ReentrantLock();
	private final State m_state;

	private static class State {

		private List<String> newPurgeFileNames(long bcGoal, int popGoal) {
			final int popCacheFileActual = m_mapFileName_Tracker.size();
			final int popReclaim = popCacheFileActual - popGoal;
			final long bcReclaim = m_bcCacheSizeActual - bcGoal;
			if (popReclaim <= 0 && bcReclaim <= 0) return Collections.emptyList();
			final List<String> zlNames = new ArrayList<>(Math.max(64, popReclaim));
			final List<Tracker> zlTrackersAsc = new ArrayList<>(m_mapFileName_Tracker.values());
			Collections.sort(zlTrackersAsc);
			final int trackerCount = zlTrackersAsc.size();
			long bcNeo = m_bcCacheSizeActual;
			int popNeo = popCacheFileActual;
			for (int i = 0; i < trackerCount && (bcNeo > bcGoal || popNeo > popGoal); i++) {
				final Tracker tracker = zlTrackersAsc.get(i);
				zlNames.add(tracker.qccFileName());
				tracker.purgeMark();
				bcNeo -= tracker.bcFile();
				popNeo--;
			}
			return zlNames;
		}

		public Tracker findTracker(String qccFileName, long tsNow) {
			final Tracker oTracker = m_mapFileName_Tracker.get(qccFileName);
			if (oTracker != null) {
				oTracker.registerAccess(tsNow);
			}
			return oTracker;
		}

		public boolean isPurgeDue(long bcQuota, int popLimit) {
			final int popCacheFileActual = m_mapFileName_Tracker.size();
			return (m_bcCacheSizeActual > bcQuota) || (popCacheFileActual > popLimit);
		}

		public JsonObject newJsonCheckpoint() {
			final List<Tracker> zlTrackersAsc = new ArrayList<>(m_mapFileName_Tracker.values());
			Collections.sort(zlTrackersAsc);
			final int trackerCount = zlTrackersAsc.size();
			final JsonArray array = JsonArray.newMutable(trackerCount);
			for (int i = 0; i < trackerCount; i++) {
				final Tracker tracker = zlTrackersAsc.get(i);
				final JsonObject t = JsonObject.newMutable();
				tracker.save(t);
				array.add(t);
			}
			final JsonObject neo = JsonObject.newMutable();
			neo.put(p_trackers, array);
			return neo;
		}

		public void purge(IArgonFileProbe probe, File cndir, String qccFileName) {
			final Tracker oEx = m_mapFileName_Tracker.get(qccFileName);
			if (oEx != null && oEx.isPurgeSafe()) {
				final File target = new File(cndir, qccFileName);
				final boolean deleted = ArgonFileManagement.deleteFile(probe, target);
				if (deleted) {
					m_mapFileName_Tracker.remove(qccFileName);
					m_bcCacheSizeActual -= oEx.bcFile();
				}
			}
		}

		public Tracker putTracker(String qccFileName, long tsLastModified, int bcFile, String qlcContentType, long tsNow) {
			final int kbFile = Math.max(1, bcFile / CArgon.K);
			Tracker vTracker = m_mapFileName_Tracker.get(qccFileName);
			if (vTracker == null) {
				vTracker = new Tracker(qccFileName, tsLastModified, kbFile, qlcContentType, tsNow);
				m_mapFileName_Tracker.put(qccFileName, vTracker);
			} else {
				m_bcCacheSizeActual -= vTracker.bcFile();
				vTracker.registerReload(tsLastModified, kbFile, qlcContentType);
			}
			m_bcCacheSizeActual += vTracker.bcFile();
			return vTracker;
		}

		public State(IArgonFileProbe probe, File cndir, int cap) {
			m_mapFileName_Tracker = new HashMap<>(cap);
			ArgonDirectoryManagement.remove(probe, cndir, true);
		}

		public State(JsonObject src) throws JsonSchemaException {
			final JsonArray array = src.accessor(p_trackers).datumArray();
			final int trackerCount = array.jsonMemberCount();
			m_mapFileName_Tracker = new HashMap<>(trackerCount);
			for (int i = 0; i < trackerCount; i++) {
				final Tracker tracker = new Tracker(array.accessor(i).datumObject());
				m_mapFileName_Tracker.put(tracker.qccFileName(), tracker);
				m_bcCacheSizeActual += tracker.bcFile();
			}
		}

		private final Map<String, Tracker> m_mapFileName_Tracker;
		private long m_bcCacheSizeActual;
	}

	private static class Tracker implements Comparable<Tracker> {

		public int bcFile() {
			return m_kbFile * CArgon.K;
		}

		@Override
		public int compareTo(Tracker rhs) {
			return ArgonCompare.fwd(m_tsLastAccess, rhs.m_tsLastAccess);
		}

		public boolean isPurgeSafe() {
			return m_purgeMarked;
		}

		public Descriptor newDescriptor() {
			return new Descriptor(m_tsLastAccess, m_tsLastModified, m_qlcContentType);
		}

		public void purgeMark() {
			m_purgeMarked = true;
		}

		public String qccFileName() {
			return m_qccFileName;
		}

		public void registerAccess(long tsNow) {
			if (tsNow > m_tsLastAccess) {
				m_tsLastAccess = tsNow;
				m_purgeMarked = false;
			}
		}

		public void registerReload(long tsLastModified, int kbFile, String qlcContentType) {
			m_tsLastModified = tsLastModified;
			m_kbFile = kbFile;
			m_qlcContentType = qlcContentType;
		}

		public void save(JsonObject dst) {
			dst.putString(p_fileName, m_qccFileName);
			dst.putTime(p_lastAccess, m_tsLastAccess);
			dst.putInteger(p_fileKB, m_kbFile);
			dst.putTime(p_lastModified, m_tsLastModified);
			dst.putString(p_contentType, m_qlcContentType);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("DiskMruTable.Tracker");
			ds.a("fileName", m_qccFileName);
			ds.at8("lastAccess", m_tsLastAccess);
			ds.a("kB", m_kbFile);
			ds.at8("lastModified", m_tsLastModified);
			ds.a("contentType", m_qlcContentType);
			ds.a("purgeMarked", m_purgeMarked);
			return ds.s();
		}

		public Tracker(JsonObject src) throws JsonSchemaException {
			if (src == null) throw new IllegalArgumentException("object is null");
			m_qccFileName = src.accessor(p_fileName).datumQtwString();
			m_tsLastAccess = src.accessor(p_lastAccess).datumTs();
			m_kbFile = src.accessor(p_fileKB).datumInteger();
			m_tsLastModified = src.accessor(p_lastModified).datumTs();
			m_qlcContentType = src.accessor(p_contentType).datumQtwString();
		}

		public Tracker(String qccFileName, long tsLastModified, int kbFile, String qlcContentType, long tsNow) {
			m_qccFileName = qccFileName;
			m_tsLastAccess = tsNow;
			m_kbFile = kbFile;
			m_tsLastModified = tsLastModified;
			m_qlcContentType = qlcContentType;
		}
		private final String m_qccFileName;
		private long m_tsLastAccess;
		private int m_kbFile;
		private long m_tsLastModified;
		private String m_qlcContentType;
		private boolean m_purgeMarked;
	}

	public static class Descriptor {

		@Override
		public String toString() {
			final Ds ds = Ds.o("DiskMruTable.Descriptor");
			ds.at8("lastAccess", tsLastAccess);
			ds.at8("lastModified", tsLastModified);
			ds.a("contentType", qlcContentType);
			return ds.s();
		}

		public Descriptor(long tsLastAccess, long tsLastModified, String qlcContentType) {
			this.tsLastAccess = tsLastAccess;
			this.tsLastModified = tsLastModified;
			this.qlcContentType = qlcContentType;
		}
		public long tsLastAccess;
		public long tsLastModified;
		public String qlcContentType;
	}

}
