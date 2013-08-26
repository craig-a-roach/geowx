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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;
import com.metservice.argon.file.ArgonFileManagement;
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

	public static final int GoalPctLo = 50;
	public static final int GoalPctHi = 99;
	public static final int FileLimitLo = 1;
	public static final int FileLimitHi = 128 * CArgon.K;

	private List<String> newPurgeAgenda() {
		List<String> zlAgenda = Collections.emptyList();
		m_rwlock.writeLock().lock();
		try {
			if (purgeRequiredLk()) {
				zlAgenda = newPurgeFileNamesLk();
			}
		} finally {
			m_rwlock.writeLock().unlock();
		}
		return zlAgenda;
	}

	private List<String> newPurgeFileNamesLk() {
		final int estReclaim = m_popCacheFileActual - m_popCacheFileGoal;
		final List<String> zlNames = new ArrayList<>(estReclaim);
		final List<Tracker> zlTrackersAsc = new ArrayList<>(m_mapFileName_Tracker.values());
		Collections.sort(zlTrackersAsc);
		final int trackerCount = zlTrackersAsc.size();
		long bcNeo = m_bcCacheSizeActual;
		int popNeo = m_popCacheFileActual;
		for (int i = 0; i < trackerCount && purgeMoreLk(bcNeo, popNeo); i++) {
			final Tracker tracker = zlTrackersAsc.get(i);
			zlNames.add(tracker.qccFileName());
			tracker.purgeMark();
			bcNeo -= tracker.bcFile();
			popNeo--;
		}
		return zlNames;
	}

	private void purge(IArgonFileProbe probe, File cndir, List<String> zlAgenda) {
		assert probe != null;
		assert cndir != null;
		assert zlAgenda != null;
		final int agendaCount = zlAgenda.size();
		for (int i = 0; i < agendaCount; i++) {
			final String qccFileName = zlAgenda.get(i);
			m_rwlock.writeLock().lock();
			try {
				final Tracker oEx = m_mapFileName_Tracker.get(qccFileName);
				if (oEx != null && oEx.isPurgeSafe()) {
					final File target = new File(cndir, qccFileName);
					final boolean deleted = ArgonFileManagement.deleteFile(probe, target);
					if (deleted) {
						m_mapFileName_Tracker.remove(qccFileName);
						m_bcCacheSizeActual -= oEx.bcFile();
						m_popCacheFileActual--;
					}
				}
			} finally {
				m_rwlock.writeLock().unlock();
			}
		}
	}

	private boolean purgeMoreLk(long bcCache, int filePop) {
		return (bcCache >= m_bcCacheSizeGoal || filePop >= m_popCacheFileGoal);
	}

	private boolean purgeRequiredLk() {
		return purgeRequiredLk(m_bcCacheSizeActual, m_popCacheFileActual);
	}

	private boolean purgeRequiredLk(long bcCache, int filePop) {
		return (bcCache >= m_bcCacheSizeQuota || filePop >= m_popCacheFileLimit);
	}

	public Descriptor findDescriptor(String qccFileName, long tsNow) {
		if (qccFileName == null || qccFileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_rwlock.writeLock().lock();
		try {
			final Tracker oTracker = m_mapFileName_Tracker.get(qccFileName);
			if (oTracker == null) return null;
			final Descriptor descriptor = oTracker.newDescriptor();
			oTracker.registerAccess(tsNow);
			return descriptor;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public Descriptor newDescriptor(String qccFileName, long tsLastModified, int bcFile, String qlcContentType, long tsNow) {
		if (qccFileName == null || qccFileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qlcContentType == null || qlcContentType.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		final int kbFile = Math.max(1, bcFile / CArgon.K);
		m_rwlock.writeLock().lock();
		try {
			Tracker vTracker = m_mapFileName_Tracker.get(qccFileName);
			if (vTracker == null) {
				vTracker = new Tracker(qccFileName, tsLastModified, kbFile, qlcContentType, tsNow);
				m_mapFileName_Tracker.put(qccFileName, vTracker);
				m_popCacheFileActual++;
			} else {
				m_bcCacheSizeActual -= vTracker.bcFile();
				vTracker.registerReload(tsLastModified, kbFile, qlcContentType);
			}
			m_bcCacheSizeActual += vTracker.bcFile();
			return vTracker.newDescriptor();
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void purge(IArgonFileProbe probe, File cndir) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (cndir == null) throw new IllegalArgumentException("object is null");
		final List<String> zlAgenda = newPurgeAgenda();
		purge(probe, cndir, zlAgenda);
	}

	public DiskMruTable(long bcCacheSizeQuota, int cacheFileLimit, int goalPct) {
		final int f = Math.max(FileLimitLo, Math.min(FileLimitHi, cacheFileLimit));
		final int g = Math.max(GoalPctLo, Math.min(GoalPctHi, goalPct));
		m_bcCacheSizeQuota = bcCacheSizeQuota;
		m_popCacheFileLimit = f;
		m_bcCacheSizeGoal = (bcCacheSizeQuota * g) / 100L;
		m_popCacheFileGoal = (f * g) / 100;
		m_mapFileName_Tracker = new HashMap<>(f);
	}
	private final long m_bcCacheSizeQuota;
	private final int m_popCacheFileLimit;
	private final long m_bcCacheSizeGoal;
	private final int m_popCacheFileGoal;
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private final Map<String, Tracker> m_mapFileName_Tracker;
	private long m_bcCacheSizeActual;
	private int m_popCacheFileActual;

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
