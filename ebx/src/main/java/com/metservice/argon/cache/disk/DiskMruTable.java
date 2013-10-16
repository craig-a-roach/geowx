/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.cache.ArgonCacheException;
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
	static final String p_contentValidator = "cv";
	static final String p_fileKB = "kb";
	static final String p_trackers = "trackers";

	public static final String CheckpointFileName = "checkpoint.json";
	public static final int CheckpointQuotaBc = 1024 * CArgon.M;

	public static final int GoalPctLo = 50;
	public static final int GoalPctHi = 99;
	public static final int FileLimitLo = 1;
	public static final int FileLimitHi = 128 * CArgon.K;

	private static final String TryDecodeCp = "Decode MRU Checkpoint";
	private static final String CsqScrub = "Wipe contents of cache";
	private static final String TryTick = "Perform MRU table resource management functions";
	private static final String CsqRetry = "Abandon this attempt then retry at scheduled frequency";
	private static final String[] NONAMES = new String[0];
	private static final int NOAUDIT = Integer.MAX_VALUE;
	private static final int NOKB = -1;

	private static State newState(Cfg cfg, int cap) {
		assert cfg != null;
		final File srcFile = new File(cfg.cndir, CheckpointFileName);
		final Binary oBinary = Binary.createFromFile(cfg.probe, srcFile, CheckpointQuotaBc);
		State oState = null;
		try {
			if (oBinary != null) {
				final JsonObject src = JsonDecoder.Default.decodeObject(oBinary.newStringUTF8());
				oState = new State(cfg, src);
			}
		} catch (final JsonSchemaException ex) {
			final Ds ds = Ds.triedTo(TryDecodeCp, ex, CsqScrub);
			cfg.probe.warnFile(ds, srcFile);
		} catch (final ArgonFormatException ex) {
			final Ds ds = Ds.triedTo(TryDecodeCp, ex, CsqScrub);
			cfg.probe.warnFile(ds, srcFile);
		}
		if (oState != null) return oState;
		return new State(cfg, cap);
	}

	private static Integer okbFile(String qccFileName, Long obcFile)
			throws ArgonCacheException {
		if (obcFile == null) return null;
		final long bcFile = obcFile.longValue();
		final long kbFileL = bcFile <= 0L ? 0L : Math.max(1L, bcFile / CArgon.K);
		if (kbFileL <= Integer.MAX_VALUE) return new Integer((int) kbFileL);
		throw new ArgonCacheException("Size of file " + qccFileName + " is " + bcFile + ", which exceeds 2GB limit");
	}

	public static DiskMruTable newInstance(IArgonDiskCacheProbe probe, File cndir, long bcQuota, int popLimit, int goalPct,
			int auditCycle) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (cndir == null) throw new IllegalArgumentException("object is null");
		final long cbcQuota = Math.max(0L, bcQuota);
		final long kbQuota = cbcQuota / CArgon.K;
		final int cpopLimit = Math.max(FileLimitLo, Math.min(FileLimitHi, popLimit));
		final int cpctGoal = Math.max(GoalPctLo, Math.min(GoalPctHi, goalPct));
		final long kbGoal = (cbcQuota * cpctGoal) / 100L / CArgon.K;
		final int popGoal = (cpopLimit * cpctGoal) / 100;
		final int cauditCycle = auditCycle <= 0 ? NOAUDIT : auditCycle;
		final Cfg cfg = new Cfg(probe, cndir, kbQuota, cpopLimit, kbGoal, popGoal, cauditCycle);
		final State state = newState(cfg, cpopLimit);
		return new DiskMruTable(cfg, state);
	}

	private void audit() {
		final String[] zptDiskFileNamesAsc = zptDiskFileNamesAsc();
		if (zptDiskFileNamesAsc.length == 0) return;
		List<String> zlUnreferencedAsc = Collections.emptyList();
		m_lockState.lock();
		try {
			zlUnreferencedAsc = m_state.zlUnreferencedAsc(zptDiskFileNamesAsc);
		} finally {
			m_lockState.unlock();
		}
		final int unrefCount = zlUnreferencedAsc.size();
		for (int i = 0; i < unrefCount; i++) {
			final String qccFileName = zlUnreferencedAsc.get(i);
			m_lockState.lock();
			try {
				m_state.reclaimUnreferenced(qccFileName);
			} finally {
				m_lockState.unlock();
			}
		}
	}

	private void checkpoint() {
		final JsonObject cp = newCheckpointJson();
		final Binary out = Binary.newFromStringUTF8(JsonEncoder.Default.encode(cp));
		final File destFile = new File(cfg.cndir, CheckpointFileName);
		out.save(cfg.probe, destFile, false);
	}

	private JsonObject newCheckpointJson() {
		m_lockState.lock();
		try {
			return m_state.newJsonCheckpoint();
		} finally {
			m_lockState.unlock();
		}
	}

	private List<String> newPurgeAgenda() {
		m_lockState.lock();
		try {
			final boolean due = m_state.isPurgeDue();
			if (due) return m_state.newPurgeFileNames();
			return Collections.emptyList();
		} finally {
			m_lockState.unlock();
		}
	}

	private void purge() {
		purge(newPurgeAgenda());
		m_checkpointDue.set(true);
	}

	private void purge(List<String> zlAgenda) {
		assert zlAgenda != null;
		final int agendaCount = zlAgenda.size();
		for (int i = 0; i < agendaCount; i++) {
			final String qccFileName = zlAgenda.get(i);
			m_lockState.lock();
			try {
				m_state.purge(qccFileName);
			} finally {
				m_lockState.unlock();
			}
		}
	}

	private String[] zptDiskFileNamesAsc() {
		final String[] ozpt = cfg.cndir.list();
		if (ozpt == null || ozpt.length == 0) return NONAMES;
		Arrays.sort(ozpt);
		return ozpt;
	}

	public Descriptor findDescriptor(String qccFileName, long tsNow) {
		if (qccFileName == null || qccFileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_lockState.lock();
		try {
			m_checkpointDue.set(true);
			final Tracker oTracker = m_state.findTracker(qccFileName, tsNow);
			return (oTracker == null) ? null : oTracker.newDescriptor();
		} finally {
			m_lockState.unlock();
		}
	}

	public Descriptor newDescriptor(String qccFileName, String zContentValidator, Long obcFile, long tsNow)
			throws ArgonCacheException {
		if (qccFileName == null || qccFileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (zContentValidator == null) throw new IllegalArgumentException("object is null");
		final Integer okbFile = okbFile(qccFileName, obcFile);
		m_lockState.lock();
		try {
			m_checkpointDue.set(true);
			if (cfg.auditCycle != NOAUDIT) {
				m_auditCounter.incrementAndGet();
			}
			final long kbEx = m_state.kbActual();
			final Tracker tracker = m_state.putTracker(qccFileName, zContentValidator, okbFile, tsNow);
			final long kbDelta = m_state.kbActual() - kbEx;
			if (kbDelta > 0L) {
				m_purgeDue.set(true);
			}
			return tracker.newDescriptor();
		} finally {
			m_lockState.unlock();
		}
	}

	public SensorSnapshot newSensorSnapshot() {
		m_lockState.lock();
		try {
			return m_state.newSensorSnapshot();
		} finally {
			m_lockState.unlock();
		}
	}

	public void tick() {
		try {
			if (m_purgeDue.getAndSet(false)) {
				purge();
			}
			if (m_checkpointDue.getAndSet(false)) {
				checkpoint();
			}
			if (m_auditCounter.compareAndSet(cfg.auditCycle, 0)) {
				audit();
			}
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryTick, ex, CsqRetry);
			ds.a("purgeDue", m_purgeDue);
			ds.a("checkpointDue", m_checkpointDue);
			ds.a("auditCounter", m_auditCounter);
			cfg.probe.failSoftware(ds);
		}
	}

	public DiskMruTable(Cfg cfg, State state) {
		assert cfg != null;
		assert state != null;
		this.cfg = cfg;
		m_state = state;
	}
	private final Cfg cfg;
	private final AtomicBoolean m_purgeDue = new AtomicBoolean();
	private final AtomicBoolean m_checkpointDue = new AtomicBoolean();
	private final AtomicInteger m_auditCounter = new AtomicInteger();
	private final Lock m_lockState = new ReentrantLock();
	private final State m_state;

	private static class Cfg {

		Cfg(IArgonDiskCacheProbe probe, File cndir, long kbQuota, int popLimit, long kbGoal, int popGoal, int auditCycle) {
			assert probe != null;
			assert cndir != null;
			this.probe = probe;
			this.cndir = cndir;
			this.kbQuota = kbQuota;
			this.popLimit = popLimit;
			this.kbGoal = kbGoal;
			this.popGoal = popGoal;
			this.auditCycle = auditCycle;
		}
		final IArgonDiskCacheProbe probe;
		final File cndir;
		final long kbQuota;
		final int popLimit;
		final long kbGoal;
		final int popGoal;
		final int auditCycle;
	}

	private static class State {

		private File newFile(String qccFileName) {
			return new File(cfg.cndir, qccFileName);
		}

		private List<String> newPurgeFileNames() {
			final int popCacheFileActual = m_mapFileName_Tracker.size();
			final int popReclaim = popCacheFileActual - cfg.popGoal;
			final long kbReclaim = m_kbActual - cfg.kbGoal;
			if (popReclaim <= 0 && kbReclaim <= 0) return Collections.emptyList();
			final List<String> zlNames = new ArrayList<>(Math.max(64, popReclaim));
			final List<Tracker> zlTrackersAsc = new ArrayList<>(m_mapFileName_Tracker.values());
			Collections.sort(zlTrackersAsc);
			final int trackerCount = zlTrackersAsc.size();
			long kbNeo = m_kbActual;
			int popNeo = popCacheFileActual;
			for (int i = 0; i < trackerCount && (kbNeo > cfg.kbGoal || popNeo > cfg.popGoal); i++) {
				final Tracker tracker = zlTrackersAsc.get(i);
				zlNames.add(tracker.qccFileName());
				tracker.purgeMark();
				kbNeo -= tracker.kbFile();
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

		public boolean isPurgeDue() {
			final int popCacheFileActual = m_mapFileName_Tracker.size();
			return (m_kbActual > cfg.kbQuota) || (popCacheFileActual > cfg.popLimit);
		}

		public long kbActual() {
			return m_kbActual;
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

		public SensorSnapshot newSensorSnapshot() {
			return new SensorSnapshot(m_kbActual, m_mapFileName_Tracker.size());
		}

		public void purge(String qccFileName) {
			final Tracker oEx = m_mapFileName_Tracker.get(qccFileName);
			if (oEx != null && oEx.isPurgeSafe()) {
				final File target = newFile(qccFileName);
				final boolean deleted = ArgonFileManagement.deleteFile(cfg.probe, target);
				if (deleted) {
					m_mapFileName_Tracker.remove(qccFileName);
					m_kbActual -= oEx.kbFile();
				}
			}
		}

		public Tracker putTracker(String qccFileName, String zContentValidator, Integer okbFile, long tsNow) {
			Tracker vTracker = m_mapFileName_Tracker.get(qccFileName);
			if (vTracker == null) {
				vTracker = new Tracker(qccFileName, zContentValidator, okbFile, tsNow);
				m_mapFileName_Tracker.put(qccFileName, vTracker);
			} else {
				m_kbActual -= vTracker.kbFile();
				vTracker.registerReload(zContentValidator, okbFile);
			}
			m_kbActual += vTracker.kbFile();
			return vTracker;
		}

		public void reclaimUnreferenced(String qccFileName) {
			final boolean isReferenced = m_mapFileName_Tracker.containsKey(qccFileName);
			if (!isReferenced) {
				final File target = new File(cfg.cndir, qccFileName);
				ArgonFileManagement.deleteFile(cfg.probe, target);
			}
		}

		public List<String> zlUnreferencedAsc(String[] zptDiskFileNamesAsc) {
			final List<String> zl = new ArrayList<>();
			final int cc = zptDiskFileNamesAsc.length;
			for (int i = 0; i < cc; i++) {
				final String qccFileName = zptDiskFileNamesAsc[i];
				if (!m_mapFileName_Tracker.containsKey(qccFileName)) {
					zl.add(qccFileName);
				}
			}
			return zl;
		}

		public State(Cfg cfg, int cap) {
			this.cfg = cfg;
			m_mapFileName_Tracker = new HashMap<>(cap);
			ArgonDirectoryManagement.remove(cfg.probe, cfg.cndir, true);
		}

		public State(Cfg cfg, JsonObject src) throws JsonSchemaException {
			this.cfg = cfg;
			final JsonArray array = src.accessor(p_trackers).datumArray();
			final int trackerCount = array.jsonMemberCount();
			m_mapFileName_Tracker = new HashMap<>(trackerCount);
			for (int i = 0; i < trackerCount; i++) {
				final Tracker tracker = new Tracker(array.accessor(i).datumObject());
				m_mapFileName_Tracker.put(tracker.qccFileName(), tracker);
				m_kbActual += tracker.kbFile();
			}
		}
		private final Cfg cfg;
		private final Map<String, Tracker> m_mapFileName_Tracker;
		private long m_kbActual;
	}

	private static class Tracker implements Comparable<Tracker> {

		private static int kbn(Integer okb) {
			return okb == null ? NOKB : okb.intValue();
		}

		@Override
		public int compareTo(Tracker rhs) {
			return ArgonCompare.fwd(m_tsLastAccess, rhs.m_tsLastAccess);
		}

		public boolean isPurgeSafe() {
			return m_purgeMarked;
		}

		public int kbFile() {
			return m_kbnFile == NOKB ? 0 : m_kbnFile;
		}

		public Descriptor newDescriptor() {
			final boolean exists = m_kbnFile != NOKB;
			return new Descriptor(m_qccFileName, m_tsLastAccess, m_zContentValidator, exists);
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

		public void registerReload(String zContentValidator, Integer okbFile) {
			m_zContentValidator = zContentValidator;
			m_kbnFile = kbn(okbFile);
		}

		public void save(JsonObject dst) {
			dst.putString(p_fileName, m_qccFileName);
			dst.putTime(p_lastAccess, m_tsLastAccess);
			dst.putInteger(p_fileKB, m_kbnFile);
			dst.putString(p_contentValidator, m_zContentValidator);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("DiskMruTable.Tracker");
			ds.a("fileName", m_qccFileName);
			ds.at8("lastAccess", m_tsLastAccess);
			ds.a("kB", m_kbnFile);
			ds.a("contentValidator", m_zContentValidator);
			ds.a("purgeMarked", m_purgeMarked);
			return ds.s();
		}

		public Tracker(JsonObject src) throws JsonSchemaException {
			if (src == null) throw new IllegalArgumentException("object is null");
			m_qccFileName = src.accessor(p_fileName).datumQtwString();
			m_tsLastAccess = src.accessor(p_lastAccess).datumTs();
			m_kbnFile = src.accessor(p_fileKB).datumInteger();
			m_zContentValidator = src.accessor(p_contentValidator).datumZtwString();
		}

		public Tracker(String qccFileName, String zContentValidator, Integer okbFile, long tsNow) {
			m_qccFileName = qccFileName;
			m_tsLastAccess = tsNow;
			m_kbnFile = kbn(okbFile);
			m_zContentValidator = zContentValidator;
		}
		private final String m_qccFileName;
		private long m_tsLastAccess;
		private int m_kbnFile;
		private String m_zContentValidator;
		private boolean m_purgeMarked;
	}

	static class SensorSnapshot {

		@Override
		public String toString() {
			final Ds ds = Ds.o("DiskMruTable.SensorSnapshot");
			ds.a("kbActual", kbActual);
			ds.a("popActual", popActual);
			return ds.s();
		}

		public SensorSnapshot(long kbActual, int popActual) {
			this.kbActual = kbActual;
			this.popActual = popActual;
		}
		public final long kbActual;
		public final int popActual;
	}

	public static class Descriptor {

		@Override
		public String toString() {
			final Ds ds = Ds.o("DiskMruTable.Descriptor");
			ds.a("fileName", qccFileName);
			ds.at8("lastAccess", tsLastAccess);
			ds.a("contentValidator", zContentValidator);
			ds.a("exists", exists);
			return ds.s();
		}

		private Descriptor(String qccFileName, long tsLastAccess, String zContentValidator, boolean exists) {
			assert qccFileName != null && qccFileName.length() > 0;
			assert zContentValidator != null;
			this.qccFileName = qccFileName;
			this.tsLastAccess = tsLastAccess;
			this.zContentValidator = zContentValidator;
			this.exists = exists;
		}
		public final String qccFileName;
		public long tsLastAccess;
		public String zContentValidator;
		public boolean exists;
	}

}
