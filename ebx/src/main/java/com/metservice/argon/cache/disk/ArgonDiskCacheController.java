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

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonDigester;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonSensorId;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonSensor;
import com.metservice.argon.IArgonSensorMap;
import com.metservice.argon.cache.ArgonCacheException;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.management.IArgonSpaceId;

/**
 * @author roach
 */
public class ArgonDiskCacheController implements IArgonSensorMap {

	public static final String ThreadPrefix = "argon-cache-disk-";
	public static final String SubDirDiskCache = "diskcache";
	public static final String SubDirMRU = "mru";
	public static final String SubDirJAR = "jar";

	public static final ArgonSensorId SensorCacheHitRate = new ArgonSensorId("CacheHitRate");
	private static final ArgonSensorId[] SENSORS = { SensorCacheHitRate };

	private static DiskMruTable newMruTable(Config cfg) {
		assert cfg != null;
		return DiskMruTable.newInstance(cfg.probe, cfg.cndirMRU, cfg.bcCacheSizeQuota, cfg.cacheFileLimit,
				cfg.pctCacheSizeGoal, cfg.auditCycle);
	}

	public static Config newConfig(IArgonDiskCacheProbe probe, ArgonServiceId sid, IArgonSpaceId idSpace)
			throws ArgonPermissionException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (sid == null) throw new IllegalArgumentException("object is null");
		if (idSpace == null) throw new IllegalArgumentException("object is null");
		final String spc = idSpace.format();
		final File cndirMRU = ArgonDirectoryManagement.cndirEnsureUserWriteable(sid.qtwVendor, sid.qtwService, SubDirDiskCache,
				spc, SubDirMRU);
		final File cndirJAR = ArgonDirectoryManagement.cndirEnsureUserWriteable(sid.qtwVendor, sid.qtwService, SubDirDiskCache,
				spc, SubDirJAR);
		final String qccThreadName = ThreadPrefix + idSpace.format();
		return new Config(probe, sid, idSpace, cndirMRU, cndirJAR, qccThreadName);
	}

	public static ArgonDiskCacheController newInstance(Config cfg)
			throws ArgonPlatformException {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final ArgonDigester digester = ArgonDigester.newSHA1();
		final DiskMruTable mruTable = newMruTable(cfg);
		final MruTask task = new MruTask(mruTable);
		final Timer timer = new Timer(cfg.qccThreadName, true);
		timer.schedule(task, cfg.msCheckpointTimerDelay, cfg.msCheckpointTimerPeriod);
		return new ArgonDiskCacheController(cfg, mruTable, timer, digester);
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
		final File destFile = new File(m_cndirJAR, qccFileName);
		src.save(destFile, false);
		return destFile;
	}

	@Override
	public IArgonSensor findSensor(ArgonSensorId id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArgonSensorId getSensorId(int index) {
		return SENSORS[index];
	}

	@Override
	public int sensorCount() {
		return SENSORS.length;
	}

	@Override
	public ArgonServiceId serviceId() {
		return m_idService;
	}

	@Override
	public IArgonSpaceId spaceId() {
		return m_idSpace;
	}

	private ArgonDiskCacheController(Config config, DiskMruTable mruTable, Timer timer, ArgonDigester digester) {
		assert config != null;
		assert mruTable != null;
		assert timer != null;
		assert digester != null;
		m_probe = config.probe;
		m_idService = config.idService;
		m_idSpace = config.idSpace;
		m_mruTable = mruTable;
		m_timer = timer;
		m_digester = digester;
		m_cndirJAR = config.cndirJAR;
		m_bcSizeQuota = config.bcSizeQuota;
		m_bcSizeEst = config.bcSizeEst;
	}
	private final IArgonDiskCacheProbe m_probe;
	private final ArgonServiceId m_idService;
	private final IArgonSpaceId m_idSpace;
	private final DiskMruTable m_mruTable;
	private final Timer m_timer;
	private final ArgonDigester m_digester;
	private final File m_cndirJAR;
	private final int m_bcSizeQuota;
	private final int m_bcSizeEst;

	private static class MruTask extends TimerTask {

		@Override
		public void run() {
			m_mruTable.tick();
		}

		public MruTask(DiskMruTable mruTable) {
			assert mruTable != null;
			m_mruTable = mruTable;
		}
		private final DiskMruTable m_mruTable;
	}

	public static class Config {

		public static final long DefaultCacheSizeQuota = 4 * CArgon.G;
		public static final int DefaultCacheFileLimit = 1000;
		public static final int DefaultCacheSizeGoal = 95;
		public static final int DefaultSizeQuota = Integer.MAX_VALUE;
		public static final int DefaultSizeEst = 64 * CArgon.K;
		public static final int DefaultCheckpointTimerDelayMs = 90 * CArgon.SEC_TO_MS;
		public static final int DefaultCheckpointTimerPeriodMs = 150 * CArgon.SEC_TO_MS;
		public static final int DefaultAuditCycle = 5000;

		public Config auditCycle(int count) {
			auditCycle = count;
			return this;
		}

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

		public Config disableAuditCycle() {
			auditCycle = -1;
			return this;
		}

		public File mruDirectory() {
			return cndirMRU;
		}

		public Config mruDirectory(String qccPath)
				throws ArgonPermissionException {
			if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
			cndirMRU = ArgonDirectoryManagement.cndirEnsureWriteable(qccPath);
			return this;
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("ArgonDiskCacheController.Config");
			ds.a("cndirMRU", cndirMRU);
			ds.a("bcCacheSizeQuota", bcCacheSizeQuota);
			ds.a("cacheFileLimit", cacheFileLimit);
			ds.a("pctCacheSizeGoal", pctCacheSizeGoal);
			ds.a("bcSizeQuota", bcSizeQuota);
			ds.a("msCheckpointTimerDelay", msCheckpointTimerDelay);
			ds.a("msCheckpointTimerPeriod", msCheckpointTimerPeriod);
			ds.a("auditCycle", auditCycle);
			ds.a("idService", idService);
			ds.a("idSpace", idSpace);
			return ds.s();
		}

		Config(IArgonDiskCacheProbe probe, ArgonServiceId sid, IArgonSpaceId idSpace, File cndirMRU, File cndirJAR,
				String qccThreadName) {
			assert probe != null;
			assert sid != null;
			assert idSpace != null;
			assert cndirMRU != null;
			assert cndirJAR != null;
			assert qccThreadName != null && qccThreadName.length() > 0;
			this.probe = probe;
			this.idService = sid;
			this.idSpace = idSpace;
			this.cndirMRU = cndirMRU;
			this.cndirJAR = cndirJAR;
			this.qccThreadName = qccThreadName;
		}
		final IArgonDiskCacheProbe probe;
		final ArgonServiceId idService;
		final IArgonSpaceId idSpace;
		File cndirMRU;
		final File cndirJAR;
		final String qccThreadName;
		long bcCacheSizeQuota = DefaultCacheSizeQuota;
		int cacheFileLimit = DefaultCacheFileLimit;
		int pctCacheSizeGoal = DefaultCacheSizeGoal;
		int bcSizeQuota = DefaultSizeQuota;
		int bcSizeEst = DefaultSizeEst;
		long msCheckpointTimerDelay = DefaultCheckpointTimerDelayMs;
		long msCheckpointTimerPeriod = DefaultCheckpointTimerPeriodMs;
		int auditCycle = DefaultAuditCycle;
	}
}
