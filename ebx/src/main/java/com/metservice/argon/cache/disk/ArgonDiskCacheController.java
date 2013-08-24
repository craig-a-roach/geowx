/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonDigester;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;
import com.metservice.argon.cache.ArgonCacheException;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.management.IArgonSpaceId;

/**
 * @author roach
 */
public class ArgonDiskCacheController {

	public static final String ThreadPrefix = "argon-cache-disk-";
	public static final String SubDirDiskCache = "diskcache";

	public static Config newConfig(IArgonFileProbe probe, ArgonServiceId sid, IArgonSpaceId idSpace)
			throws ArgonPermissionException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (sid == null) throw new IllegalArgumentException("object is null");
		if (idSpace == null) throw new IllegalArgumentException("object is null");
		final File cndir = ArgonDirectoryManagement.cndirEnsureUserWriteable(sid.qtwVendor, sid.qtwService, SubDirDiskCache,
				idSpace.format());
		final String qccThreadName = ThreadPrefix + idSpace.format();
		return new Config(probe, cndir, qccThreadName);
	}

	public static ArgonDiskCacheController newInstance(Config cfg)
			throws ArgonPlatformException {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final ArgonDigester digester = ArgonDigester.newSHA1();
		final CheckpointTask task = new CheckpointTask(cfg);
		final Timer timer = new Timer(cfg.qccThreadName, true);
		timer.schedule(task, cfg.msCheckpointTimerDelay, cfg.msCheckpointTimerPeriod);
		return new ArgonDiskCacheController(cfg, timer, digester);
	}

	private void registerHit(String qlcFileName) {
		// TODO Auto-generated method stub

	}

	private void registerMiss() {
		// TODO Auto-generated method stub

	}

	public <R extends IArgonDiskCacheRequest> File find(IArgonDiskCacheSupplier<R> supplier, R request)
			throws ArgonCacheException {
		if (supplier == null) throw new IllegalArgumentException("object is null");
		if (request == null) throw new IllegalArgumentException("object is null");
		final String qccResourceId = request.qccResourceId();
		if (qccResourceId == null || qccResourceId.length() == 0)
			throw new IllegalArgumentException("Request resource id is empty");
		final String fileName = m_digester.digestUTF8B64URL(qccResourceId);
		final long tsNow = ArgonClock.tsNow();
		final IArgonDiskCacheable oCacheable = supplier.find(request);
		if (oCacheable == null) return null;
		final Binary oContent = oCacheable.getContent();
		if (oContent == null) return null;
		return null;
	}

	public File findByClassPath(Class<?> resourceRef, String qccFileName)
			throws ArgonQuotaException, ArgonPermissionException, ArgonStreamReadException, ArgonStreamWriteException {
		if (resourceRef == null) throw new IllegalArgumentException("object is null");
		if (qccFileName == null || qccFileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final InputStream oins = resourceRef.getResourceAsStream(qccFileName);
		if (oins == null) return null;
		final Binary src = Binary.newFromInputStream(oins, m_bcSizeEst, qccFileName, m_bcSizeQuota);
		final File destFile = new File(m_cndir, qccFileName);
		src.save(destFile, false);
		return destFile;
	}

	private ArgonDiskCacheController(Config config, Timer timer, ArgonDigester digester) {
		assert config != null;
		assert timer != null;
		assert digester != null;
		m_probe = config.probe;
		m_cndir = config.cndir;
		m_timer = timer;
		m_digester = digester;
		m_bcSizeQuota = config.bcSizeQuota;
		m_bcSizeEst = config.bcSizeEst;
	}

	private final ReadWriteLock m_rwLock = new ReentrantReadWriteLock();
	private final IArgonFileProbe m_probe;
	private final File m_cndir;
	private final Timer m_timer;
	private final ArgonDigester m_digester;
	private final int m_bcSizeQuota;
	private final int m_bcSizeEst;

	private static class CheckpointTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

		public CheckpointTask(Config cfg) {
			assert cfg != null;
			m_probe = cfg.probe;
			m_cndir = cfg.cndir;
		}
		private final IArgonFileProbe m_probe;
		private final File m_cndir;
	}

	public static class Config {

		public static final long DefaultCacheSizeQuota = 4 * CArgon.G;
		public static final int DefaultCacheFileLimit = 1000;
		public static final int DefaultSizeQuota = Integer.MAX_VALUE;
		public static final int DefaultSizeEst = 64 * CArgon.K;
		public static final int DefaultCheckpointTimerDelayMs = 2 * CArgon.MIN_TO_MS;
		public static final int DefaultCheckpointTimerPeriodMs = 10 * CArgon.MIN_TO_MS;

		public Config cacheFileLimit(int count) {
			cacheFileLimit = Math.max(1, count);
			return this;
		}

		public Config cacheSizeQuota(int bc) {
			bcCacheSizeQuota = Math.max(16, bc);
			return this;
		}

		public Config checkpointHoldoff(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			msCheckpointTimerDelay = unit.toMillis(count);
			return this;
		}

		public Config checkpointPeriod(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			msCheckpointTimerPeriod = unit.toMillis(count);
			return this;
		}

		public Config classpathSizeQuota(int bc) {
			bcSizeQuota = Math.max(16, bc);
			return this;
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("ArgonDiskCacheController.Config");
			ds.a("cndir", cndir);
			ds.a("bcCacheSizeQuota", bcCacheSizeQuota);
			ds.a("cacheFileLimit", cacheFileLimit);
			ds.a("bcSizeQuota", bcSizeQuota);
			ds.a("msCheckpointTimerDelay", msCheckpointTimerDelay);
			ds.a("msCheckpointTimerPeriod", msCheckpointTimerPeriod);
			return ds.s();
		}

		Config(IArgonFileProbe probe, File cndir, String qccThreadName) {
			assert probe != null;
			assert cndir != null;
			assert qccThreadName != null && qccThreadName.length() > 0;
			this.probe = probe;
			this.cndir = cndir;
			this.qccThreadName = qccThreadName;
		}
		final IArgonFileProbe probe;
		final File cndir;
		final String qccThreadName;
		long bcCacheSizeQuota = DefaultCacheSizeQuota;
		int cacheFileLimit = DefaultCacheFileLimit;
		int bcSizeQuota = DefaultSizeQuota;
		int bcSizeEst = DefaultSizeEst;
		long msCheckpointTimerDelay = DefaultCheckpointTimerDelayMs;
		long msCheckpointTimerPeriod = DefaultCheckpointTimerPeriodMs;
	}
}
