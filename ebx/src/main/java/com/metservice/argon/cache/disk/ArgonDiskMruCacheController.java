/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonDigester;
import com.metservice.argon.ArgonNameLock;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonSensorHitRate;
import com.metservice.argon.ArgonSensorId;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.ArgonTransformer;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Ds;
import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.IArgonSensor;
import com.metservice.argon.IArgonSensorMap;
import com.metservice.argon.IArgonSpaceId;
import com.metservice.argon.cache.ArgonCacheException;
import com.metservice.argon.cache.disk.DiskMruTable.Descriptor;
import com.metservice.argon.file.ArgonDirectoryManagement;

/**
 * @author roach
 */
public class ArgonDiskMruCacheController implements IArgonSensorMap {

	public static final String ThreadPrefix = "argon-cache-disk-";
	public static final String SubDir = "diskmrucache";

	private static final String TrySave = "Save content to file";

	public static final ArgonSensorId SensorMruCacheHitRate = new ArgonSensorId("MruCacheHitRate");
	private static final ArgonSensorId[] SENSORS = { SensorMruCacheHitRate };

	private static ArgonSensorHitRate newSensor() {
		return new ArgonSensorHitRate(ElapsedFactory.newElapsed(15, TimeUnit.MINUTES),
				"Smoothed ratio of MRU cache hits to misses");
	}

	public static Config newConfig(IArgonDiskMruCacheProbe probe, ArgonServiceId sid, IArgonSpaceId idSpace)
			throws ArgonPermissionException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (sid == null) throw new IllegalArgumentException("object is null");
		if (idSpace == null) throw new IllegalArgumentException("object is null");
		final String spc = idSpace.format();
		final File cndir = ArgonDirectoryManagement.cndirEnsureUserWriteable(sid.qtwVendor, sid.qtwService, SubDir, spc);
		final String qccThreadName = ThreadPrefix + idSpace.format();
		return new Config(probe, sid, idSpace, cndir, qccThreadName);
	}

	public static ArgonDiskMruCacheController newInstance(Config cfg)
			throws ArgonPlatformException {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final ArgonDigester oDigester = cfg.safeNaming ? ArgonDigester.newSHA1() : null;
		if (cfg.clean) {
			ArgonDirectoryManagement.remove(cfg.probe, cfg.cndir, true);
		}
		final DiskMruTable table = DiskMruTable.newInstance(cfg);
		final ArgonSensorHitRate sensor = newSensor();
		final MruTask task = new MruTask(table, sensor);
		final Timer timer = new Timer(cfg.qccThreadName, true);
		timer.schedule(task, cfg.checkpointDelayMs, cfg.checkpointPeriodMs);
		return new ArgonDiskMruCacheController(cfg, table, timer, oDigester, sensor);
	}

	private String failSave(Throwable ex) {
		return "Failed to save cacheable resource content..." + ex.getMessage();
	}

	private <R extends IArgonDiskCacheRequest> File findLk(IArgonDiskCacheSupplier<R> supplier, R request, String qccFileName)
			throws ArgonCacheException, InterruptedException {
		final Date now = now(supplier);
		final long tsNow = now.getTime();
		Descriptor oDescriptor = m_table.findDescriptor(qccFileName, tsNow);
		if (oDescriptor == null) {
			registerMruMiss(request, tsNow);
			final IArgonDiskCacheable vCacheable = supplier.getCacheable(request);
			if (vCacheable == null) return null;

		}

		if (isFresh(oDescriptor, tsNow)) {
			registerMruHit(request, tsNow);
			return oFile(oDescriptor, qccFileName);
		}
		registerMruMiss(request, tsNow);
		final IArgonDiskCacheable oCacheable;
		if (oDescriptor == null || oDescriptor.oLastModified == null) {
			oCacheable = supplier.getCacheable(request);
		} else {
			oCacheable = supplier.getCacheableConditional(request, oDescriptor.oLastModified);
		}

		if (oCacheable != null) {
			final String ztwContentValidator = ArgonText.ztw(oCacheable.getContentValidator());
			final Binary oContent = oCacheable.getContent();
			final Dcu dcu = Dcu.newInstance(oContent);
			oDescriptor = m_table.newDescriptor(qccFileName, ztwContentValidator, dcu, tsNow);
			if (oContent != null) {
				save(ref, oContent);
			}
		}
		return oFile(oDescriptor, qccFileName);
	}

	private boolean isFresh(Descriptor oDescriptor, long tsNow) {
		return oDescriptor != null && tsNow < oDescriptor.tsExpires;
	}

	private Descriptor newDescriptor(String qccFileName, IArgonDiskCacheable cacheable, long tsNow)
			throws ArgonCacheException {
		final Binary oContent = cacheable.getContent();
		final Date oLastModified = cacheable.getLastModified();
		final Date oExpires = cacheable.getExpires();
		final Dcu dcu = Dcu.newInstance(oContent);
		return m_table.newDescriptor(qccFileName, dcu, oLastModified, oExpires, tsNow);

	}

	private File oFile(Descriptor oDescriptor, String qccFileName) {
		if (oDescriptor == null || !oDescriptor.exists) return null;
		return new File(m_cndir, qccFileName);
	}

	private void probeSave(File dst, Throwable cause, Binary content) {
		final Ds ds = Ds.triedTo(TrySave, cause, ArgonCacheException.class);
		ds.a("byteCount", content.zptReadOnly);
		m_probe.failFile(ds, dst);
	}

	private String qccFileName(String qccResourceId) {
		assert qccResourceId != null && qccResourceId.length() > 0;
		if (m_oDigester == null) return ArgonTransformer.zPosixSanitized(qccResourceId);
		return m_oDigester.digestUTF8B64URL(qccResourceId);
	}

	private void registerMruHit(IArgonDiskCacheRequest request, long tsNow) {
		assert request != null;
		if (m_probe.isLiveRequest()) {
			m_probe.liveRequestHit(request.qccResourceId());
		}
		m_sensor.addSample(true, tsNow);
	}

	private void registerMruMiss(IArgonDiskCacheRequest request, long tsNow) {
		assert request != null;
		if (m_probe.isLiveRequest()) {
			m_probe.liveRequestMiss(request.qccResourceId());
		}
		m_sensor.addSample(false, tsNow);
	}

	private void save(File ref, Binary content)
			throws ArgonCacheException {
		assert ref != null;
		assert content != null;
		try {
			content.save(ref, false);
		} catch (final ArgonPermissionException ex) {
			probeSave(ref, ex, content);
			throw new ArgonCacheException(failSave(ex));
		} catch (final ArgonStreamWriteException ex) {
			probeSave(ref, ex, content);
			throw new ArgonCacheException(failSave(ex));
		}
	}

	public void cancel() {
		m_timer.cancel();
	}

	public <R extends IArgonDiskCacheRequest> File find(IArgonDiskCacheSupplier<R> supplier, R request)
			throws ArgonCacheException, InterruptedException {
		if (supplier == null) throw new IllegalArgumentException("object is null");
		if (request == null) throw new IllegalArgumentException("object is null");
		final String qccResourceId = request.qccResourceId();
		final String qccFileName = qccFileName(qccResourceId);
		final long tsNow = now(supplier).getTime();
		final Descriptor oDescriptor = m_table.findDescriptor(qccFileName, tsNow);
		if (isFresh(oDescriptor, tsNow)) {
			registerMruHit(request, tsNow);
			return oFile(oDescriptor, qccFileName);
		}
		m_lock.lock(qccFileName);
		try {
			return findLk(supplier, request, qccFileName);
		} finally {
			m_lock.unlock(qccFileName);
		}
	}

	@Override
	public IArgonSensor findSensor(ArgonSensorId id) {
		if (id.equals(SensorMruCacheHitRate)) return m_sensor;
		return null;
	}

	@Override
	public ArgonSensorId getSensorId(int index) {
		return SENSORS[index];
	}

	public Date now(IArgonDiskCacheSupplier<?> supplier) {
		assert supplier != null;
		final Date oNow = supplier.getNow();
		return oNow == null ? DateFactory.newDate(ArgonClock.tsNow()) : oNow;

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

	private ArgonDiskMruCacheController(Config config, DiskMruTable table, Timer timer, ArgonDigester oDigester,
			ArgonSensorHitRate sensor) {
		assert config != null;
		assert table != null;
		assert timer != null;
		assert sensor != null;
		m_probe = config.probe;
		m_idService = config.idService;
		m_idSpace = config.idSpace;
		m_table = table;
		m_timer = timer;
		m_oDigester = oDigester;
		m_sensor = sensor;
		m_cndir = config.cndir;
	}
	private final IArgonDiskMruCacheProbe m_probe;
	private final ArgonServiceId m_idService;
	private final IArgonSpaceId m_idSpace;
	private final DiskMruTable m_table;
	private final Timer m_timer;
	private final ArgonDigester m_oDigester;
	private final ArgonSensorHitRate m_sensor;
	private final File m_cndir;
	private final ArgonNameLock m_lock = new ArgonNameLock();

	private static class MruTask extends TimerTask {

		@Override
		public void run() {
			final long tsNow = System.currentTimeMillis();
			m_table.tick(tsNow);
			m_sensor.tick(tsNow);
		}

		public MruTask(DiskMruTable table, ArgonSensorHitRate sensor) {
			assert table != null;
			m_table = table;
			m_sensor = sensor;
		}
		private final DiskMruTable m_table;
		private final ArgonSensorHitRate m_sensor;
	}

	public static class Config {

		public static final long DefaultSizeLimitBytes = 4 * CArgon.G;
		public static final int DefaultPopulationLimit = 1000;
		public static final int DefaultPurgeGoalPct = 95;
		public static final int DefaultPurgeWakePct = 98;
		public static final int DefaultCheckpointDelayMs = 90 * CArgon.SEC_TO_MS;
		public static final int DefaultCheckpointPeriodMs = 150 * CArgon.SEC_TO_MS;
		public static final int DefaultAuditCycle = 5000;
		public static final boolean DefaultClean = false;
		public static final boolean DefaultSafeNaming = true;
		public static final int DefaultSizeEst = 64 * CArgon.K;
		public static final long DefaultMinLifeMs = 60 * CArgon.SEC_TO_MS;

		public Config auditCycle(int count) {
			auditCycle = count;
			return this;
		}

		public Config checkpointHoldoff(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			checkpointDelayMs = unit.toMillis(count);
			return this;
		}

		public Config checkpointPeriod(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			checkpointPeriodMs = unit.toMillis(count);
			return this;
		}

		public File directory() {
			return cndir;
		}

		public Config directory(String qccPath)
				throws ArgonPermissionException {
			if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
			cndir = ArgonDirectoryManagement.cndirEnsureWriteable(qccPath);
			return this;
		}

		public Config disableAuditCycle() {
			auditCycle = -1;
			return this;
		}

		public Config enableClean(boolean enable) {
			clean = enable;
			return this;
		}

		public Config enableSafeNaming(boolean enable) {
			safeNaming = enable;
			return this;
		}

		public Config minLife(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			minLifeMs = unit.toMillis(count);
			return this;
		}

		public Config populationLimit(int count) {
			populationLimit = Math.max(1, count);
			return this;
		}

		public Config purgeGoalPercent(int pct) {
			purgeGoalPct = Math.min(100, Math.max(1, pct));
			return this;
		}

		public Config purgeWakePercent(int pct) {
			purgeWakePct = Math.min(100, Math.max(1, pct));
			return this;
		}

		public Config sizeLimitBytes(int bc) {
			sizeLimitBytes = Math.max(1, bc);
			return this;
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("ArgonDiskMruCacheController.Config");
			ds.a("cndir", cndir);
			ds.a("sizeLimitBytes", sizeLimitBytes);
			ds.a("populationLimit", populationLimit);
			ds.a("purgeGoalPct", purgeGoalPct);
			ds.a("purgeWakePct", purgeWakePct);
			ds.ae("checkpointDelay", checkpointDelayMs);
			ds.ae("checkpointPeriod", checkpointPeriodMs);
			ds.ae("minLife", minLifeMs);
			ds.a("auditCycle", auditCycle);
			ds.a("clean", clean);
			ds.a("safeNaming", safeNaming);
			ds.a("idService", idService);
			ds.a("idSpace", idSpace);
			return ds.s();
		}

		Config(IArgonDiskMruCacheProbe probe, ArgonServiceId sid, IArgonSpaceId idSpace, File cndir, String qccThreadName) {
			assert probe != null;
			assert sid != null;
			assert idSpace != null;
			assert cndir != null;
			assert qccThreadName != null && qccThreadName.length() > 0;
			this.probe = probe;
			this.idService = sid;
			this.idSpace = idSpace;
			this.cndir = cndir;
			this.qccThreadName = qccThreadName;
		}
		final IArgonDiskMruCacheProbe probe;
		final ArgonServiceId idService;
		final IArgonSpaceId idSpace;
		final String qccThreadName;
		File cndir;
		long sizeLimitBytes = DefaultSizeLimitBytes;
		int populationLimit = DefaultPopulationLimit;
		int purgeGoalPct = DefaultPurgeGoalPct;
		int purgeWakePct = DefaultPurgeWakePct;
		long checkpointDelayMs = DefaultCheckpointDelayMs;
		long checkpointPeriodMs = DefaultCheckpointPeriodMs;
		long minLifeMs = DefaultMinLifeMs;
		int auditCycle = DefaultAuditCycle;
		boolean clean = DefaultClean;
		boolean safeNaming = DefaultSafeNaming;
		int bcSizeEst = DefaultSizeEst;
	}
}
