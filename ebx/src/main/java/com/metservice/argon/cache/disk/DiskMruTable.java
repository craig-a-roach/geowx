/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;
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

	private List<String> newPurgeFileNamesLk() {
		final int estReclaim = m_cacheFileActual - m_cacheFileGoal;
		final List<String> zlNames = new ArrayList<>(estReclaim);
		return zlNames;
	}

	private boolean purgeRequiredLk() {
		if (m_bcCacheSizeActual >= m_bcCacheSizeQuota) return true;
		if (m_cacheFileActual >= m_cacheFileLimit) return true;
		return false;
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
			final Tracker tracker = new Tracker(qccFileName, tsLastModified, kbFile, qlcContentType, tsNow);
			m_mapFileName_Tracker.put(qccFileName, tracker);
			m_bcCacheSizeActual += bcFile;
			m_cacheFileActual++;
			return tracker.newDescriptor();
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void purge(IArgonFileProbe oprobe, File cndir) {
		if (cndir == null) throw new IllegalArgumentException("object is null");
		m_rwlock.writeLock().lock();
		try {
			if (!purgeRequiredLk()) return;

		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public DiskMruTable(long bcCacheSizeQuota, int cacheFileLimit, int goalPct) {
		final int f = Math.max(FileLimitLo, Math.min(FileLimitHi, cacheFileLimit));
		final int g = Math.max(GoalPctLo, Math.min(GoalPctHi, goalPct));
		m_bcCacheSizeQuota = bcCacheSizeQuota;
		m_cacheFileLimit = f;
		m_bcCacheSizeGoal = (bcCacheSizeQuota * g) / 100L;
		m_cacheFileGoal = (f * g) / 100;
		m_mapFileName_Tracker = new HashMap<>(f);
	}
	private final long m_bcCacheSizeQuota;
	private final int m_cacheFileLimit;
	private final long m_bcCacheSizeGoal;
	private final int m_cacheFileGoal;
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private final Map<String, Tracker> m_mapFileName_Tracker;
	private long m_bcCacheSizeActual;
	private int m_cacheFileActual;

	private static class Tracker implements Comparable<Tracker> {

		@Override
		public int compareTo(Tracker rhs) {
			return ArgonCompare.fwd(m_tsLastAccess, rhs.m_tsLastAccess);
		}

		public Descriptor newDescriptor() {
			return new Descriptor(m_tsLastAccess, m_tsLastModified, m_qlcContentType);
		}

		public void registerAccess(long tsNow) {
			if (tsNow > m_tsLastAccess) {
				m_tsLastAccess = tsNow;
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
