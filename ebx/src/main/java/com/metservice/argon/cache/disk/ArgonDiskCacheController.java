/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonDigester;
import com.metservice.argon.ArgonNameLock;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonSensorId;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.ArgonTransformer;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.IArgonSensor;
import com.metservice.argon.IArgonSensorMap;
import com.metservice.argon.cache.ArgonCacheException;
import com.metservice.argon.cache.disk.DiskMruTable.Descriptor;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.management.ArgonSensorHitRate;
import com.metservice.argon.management.IArgonSpaceId;

/**
 * @author roach
 */
public class ArgonDiskCacheController implements IArgonSensorMap {

	public static final String ThreadPrefix = "argon-cache-disk-";
	public static final String SubDirDiskCache = "diskcache";
	public static final String SubDirMRU = "mru";
	public static final String SubDirJAR = "jar";
	public static final int ClasspathFileSizeLimit = Integer.MAX_VALUE;

	private static final String TrySave = "Save content to file";
	private static final String TryLoadCp = "Load content from classpath";

	public static final ArgonSensorId SensorMruCacheHitRate = new ArgonSensorId("MruCacheHitRate");
	private static final ArgonSensorId[] SENSORS = { SensorMruCacheHitRate };

	private static void cleanJarDir(Config cfg) {
		assert cfg != null;
		if (cfg.cleanJAR) {
			ArgonDirectoryManagement.remove(cfg.probe, cfg.cndirJAR, true);
		}
	}

	private static void cleanMruDir(Config cfg) {
		assert cfg != null;
		if (cfg.cleanMRU) {
			ArgonDirectoryManagement.remove(cfg.probe, cfg.cndirMRU, true);
		}
	}

	private static ArgonSensorHitRate newMruSensor() {
		return new ArgonSensorHitRate(ElapsedFactory.newElapsed(15, TimeUnit.MINUTES),
				"Smoothed ratio of MRU cache hits to misses");
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
		final ArgonDigester oDigester = cfg.safeNaming ? ArgonDigester.newSHA1() : null;
		cleanMruDir(cfg);
		cleanJarDir(cfg);
		final DiskMruTable mruTable = DiskMruTable.newInstance(cfg);
		final ArgonSensorHitRate mruSensor = newMruSensor();
		final MruTask task = new MruTask(mruTable, mruSensor);
		final Timer timer = new Timer(cfg.qccThreadName, true);
		timer.schedule(task, cfg.mruCheckpointDelayMs, cfg.mruCheckpointPeriodMs);
		return new ArgonDiskCacheController(cfg, mruTable, timer, oDigester, mruSensor);
	}

	private String failLoadCp(Throwable ex) {
		return "Failed to load resource content from class path..." + ex.getMessage();
	}

	private String failSave(Throwable ex) {
		return "Failed to save cacheable resource content..." + ex.getMessage();
	}

	private Binary getBinaryFromClasspath(File ref, String qccResourcePath, Class<?> resourceRef)
			throws ArgonCacheException {
		final InputStream oins = resourceRef.getResourceAsStream(qccResourcePath);
		if (oins == null) return null;
		try {
			return Binary.newFromInputStream(oins, m_bcSizeEst, qccResourcePath, ClasspathFileSizeLimit);
		} catch (final ArgonQuotaException ex) {
			throw new ArgonCacheException(failLoadCp(ex));
		} catch (final ArgonStreamReadException ex) {
			probeLoadCp(resourceRef, ex);
			throw new ArgonCacheException(failLoadCp(ex));
		}
	}

	private boolean isValid(IArgonDiskCacheMruRequest request, Date now, Descriptor oDescriptor) {
		return oDescriptor != null && request.isValid(now, oDescriptor.zContentValidator);
	}

	private File newFileJAR(String qccFileName) {
		return new File(m_cndirJAR, qccFileName);
	}

	private File newFileMRU(String qccFileName) {
		return new File(m_cndirMRU, qccFileName);
	}

	private File oFile(Descriptor oDescriptor, File ref) {
		return (oDescriptor != null && oDescriptor.exists) ? ref : null;
	}

	private void probeLoadCp(Class<?> resourceRef, Throwable cause) {
		final Ds ds = Ds.triedTo(TryLoadCp, cause, ArgonCacheException.class);
		ds.a("resourceRef", resourceRef);
		ds.a("resourceRef.classLoader", resourceRef.getClassLoader());
		m_probe.failSoftware(ds);
	}

	private void probeSave(File dst, Throwable cause, Binary content) {
		final Ds ds = Ds.triedTo(TrySave, cause, ArgonCacheException.class);
		ds.a("byteCount", content.zptReadOnly);
		m_probe.failFile(ds, dst);
	}

	private String qccFileName(IArgonDiskCacheRequest request) {
		assert request != null;
		final String qccResourceId = request.qccResourceId();
		if (qccResourceId == null || qccResourceId.length() == 0)
			throw new IllegalArgumentException("Request resource id is empty");
		if (m_oDigester == null) return ArgonTransformer.zPosixSanitized(qccResourceId);
		return m_oDigester.digestUTF8B64URL(qccResourceId);
	}

	private void registerMruHit(IArgonDiskCacheRequest request, long tsNow) {
		assert request != null;
		if (m_probe.isLiveMruRequest()) {
			m_probe.liveMruRequestHit(request.qccResourceId());
		}
		m_sensorMRU.addSample(true, tsNow);
	}

	private void registerMruMiss(IArgonDiskCacheRequest request, long tsNow) {
		assert request != null;
		if (m_probe.isLiveMruRequest()) {
			m_probe.liveMruRequestMiss(request.qccResourceId());
		}
		m_sensorMRU.addSample(false, tsNow);
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

	public <R extends IArgonDiskCacheMruRequest> File find(IArgonDiskCacheSupplier<R> supplier, R request)
			throws ArgonCacheException, InterruptedException {
		if (supplier == null) throw new IllegalArgumentException("object is null");
		if (request == null) throw new IllegalArgumentException("object is null");
		final String qccFileName = qccFileName(request);
		final long tsNow = ArgonClock.tsNow();
		final Date now = new Date(tsNow);
		final File ref = newFileMRU(qccFileName);
		Descriptor oDescriptor = m_mruTable.findDescriptor(qccFileName, tsNow);
		if (isValid(request, now, oDescriptor)) {
			registerMruHit(request, tsNow);
			return oFile(oDescriptor, ref);
		}
		m_lockMRU.lock(qccFileName);
		try {
			oDescriptor = m_mruTable.findDescriptor(qccFileName, tsNow);
			if (isValid(request, now, oDescriptor)) {
				registerMruHit(request, tsNow);
				return oFile(oDescriptor, ref);
			}
			registerMruMiss(request, tsNow);
			final IArgonDiskCacheable oCacheable = supplier.getCacheable(request);
			if (oCacheable != null) {
				final String ztwContentValidator = ArgonText.ztw(oCacheable.getContentValidator());
				final Binary oContent = oCacheable.getContent();
				final Dcu dcu = Dcu.newInstance(oContent);
				oDescriptor = m_mruTable.newDescriptor(qccFileName, ztwContentValidator, dcu, tsNow);
				if (oContent != null) {
					save(ref, oContent);
				}
			}
			return oFile(oDescriptor, ref);
		} finally {
			m_lockMRU.unlock(qccFileName);
		}
	}

	public <R extends IArgonDiskCacheClasspathRequest> File find(R request)
			throws ArgonCacheException, InterruptedException {
		if (request == null) throw new IllegalArgumentException("object is null");
		final String qccFileName = qccFileName(request);
		final File ref = newFileJAR(qccFileName);
		if (ref.exists()) return ref;
		m_lockJAR.lock(qccFileName);
		try {
			if (ref.exists()) return ref;
			final Binary oContent = getBinaryFromClasspath(ref, request.qccResourcePath(), request.resourceRef());
			if (oContent != null) {
				save(ref, oContent);
			}
			return oContent == null ? null : ref;
		} finally {
			m_lockJAR.unlock(qccFileName);
		}
	}

	public File findClasspath(Class<?> resourceRef, String qccResourcePath)
			throws ArgonCacheException, InterruptedException {
		if (resourceRef == null) throw new IllegalArgumentException("object is null");
		if (qccResourcePath == null || qccResourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		return find(new DefaultClasspathRequest(resourceRef, qccResourcePath));
	}

	@Override
	public IArgonSensor findSensor(ArgonSensorId id) {
		if (id.equals(SensorMruCacheHitRate)) return m_sensorMRU;
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

	private ArgonDiskCacheController(Config config, DiskMruTable mruTable, Timer timer, ArgonDigester oDigester,
			ArgonSensorHitRate sensorMRU) {
		assert config != null;
		assert mruTable != null;
		assert timer != null;
		assert sensorMRU != null;
		m_probe = config.probe;
		m_idService = config.idService;
		m_idSpace = config.idSpace;
		m_mruTable = mruTable;
		m_timer = timer;
		m_oDigester = oDigester;
		m_sensorMRU = sensorMRU;
		m_cndirMRU = config.cndirMRU;
		m_cndirJAR = config.cndirJAR;
		m_bcSizeEst = config.bcSizeEst;
	}
	private final IArgonDiskCacheProbe m_probe;
	private final ArgonServiceId m_idService;
	private final IArgonSpaceId m_idSpace;
	private final DiskMruTable m_mruTable;
	private final Timer m_timer;
	private final ArgonDigester m_oDigester;
	private final ArgonSensorHitRate m_sensorMRU;
	private final File m_cndirMRU;
	private final File m_cndirJAR;
	private final int m_bcSizeEst;
	private final ArgonNameLock m_lockMRU = new ArgonNameLock();
	private final ArgonNameLock m_lockJAR = new ArgonNameLock();

	private static class DefaultClasspathRequest implements IArgonDiskCacheClasspathRequest {

		@Override
		public String qccResourceId() {
			return m_ref.getPackage().getName() + "." + m_qccPath;
		}

		@Override
		public String qccResourcePath() {
			return m_qccPath;
		}

		@Override
		public Class<?> resourceRef() {
			return m_ref;
		}

		private DefaultClasspathRequest(Class<?> ref, String qccPath) {
			assert ref != null;
			assert qccPath != null;
			m_ref = ref;
			m_qccPath = qccPath;
		}
		private final Class<?> m_ref;
		private final String m_qccPath;
	}

	private static class MruTask extends TimerTask {

		@Override
		public void run() {
			final long tsNow = System.currentTimeMillis();
			m_mruTable.tick(tsNow);
			m_sensor.tick(tsNow);
		}

		public MruTask(DiskMruTable mruTable, ArgonSensorHitRate sensor) {
			assert mruTable != null;
			m_mruTable = mruTable;
			m_sensor = sensor;
		}
		private final DiskMruTable m_mruTable;
		private final ArgonSensorHitRate m_sensor;
	}

	public static class Config {

		public static final long DefaultMruSizeLimitBytes = 4 * CArgon.G;
		public static final int DefaultMruPopulationLimit = 1000;
		public static final int DefaultMruPurgeGoalPct = 95;
		public static final int DefaultMruPurgeWakePct = 98;
		public static final int DefaultMruCheckpointDelayMs = 90 * CArgon.SEC_TO_MS;
		public static final int DefaultMruCheckpointPeriodMs = 150 * CArgon.SEC_TO_MS;
		public static final int DefaultAuditCycle = 5000;
		public static final boolean DefaultCleanMRU = false;
		public static final boolean DefaultCleanJAR = true;
		public static final boolean DefaultSafeNaming = true;
		public static final int DefaultSizeEst = 64 * CArgon.K;
		public static final long DefaultMinLifeMs = 60 * CArgon.SEC_TO_MS;

		public Config disableMruAuditCycle() {
			mruAuditCycle = -1;
			return this;
		}

		public Config enableJARClean(boolean enable) {
			cleanJAR = enable;
			return this;
		}

		public Config enableMRUClean(boolean enable) {
			cleanMRU = enable;
			return this;
		}

		public Config enableSafeNaming(boolean enable) {
			safeNaming = enable;
			return this;
		}

		public File jarDirectory() {
			return cndirJAR;
		}

		public Config jarDirectory(String qccPath)
				throws ArgonPermissionException {
			if (qccPath == null || qccPath.length() == 0) throw new IllegalArgumentException("string is null or empty");
			cndirJAR = ArgonDirectoryManagement.cndirEnsureWriteable(qccPath);
			return this;
		}

		public Config mruAuditCycle(int count) {
			mruAuditCycle = count;
			return this;
		}

		public Config mruCheckpointHoldoff(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			mruCheckpointDelayMs = unit.toMillis(count);
			return this;
		}

		public Config mruCheckpointPeriod(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			mruCheckpointPeriodMs = unit.toMillis(count);
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

		public Config mruMinLife(TimeUnit unit, int count) {
			if (unit == null) throw new IllegalArgumentException("object is null");
			mruMinLifeMs = unit.toMillis(count);
			return this;
		}

		public Config mruPopulationLimit(int count) {
			mruPopulationLimit = Math.max(1, count);
			return this;
		}

		public Config mruPurgeGoalPercent(int pct) {
			mruPurgeGoalPct = Math.min(100, Math.max(1, pct));
			return this;
		}

		public Config mruPurgeWakePercent(int pct) {
			mruPurgeWakePct = Math.min(100, Math.max(1, pct));
			return this;
		}

		public Config mruSizeLimitBytes(int bc) {
			mruSizeLimitBytes = Math.max(1, bc);
			return this;
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("ArgonDiskCacheController.Config");
			ds.a("cndirMRU", cndirMRU);
			ds.a("cndirJAR", cndirJAR);
			ds.a("mruSizeLimitBytes", mruSizeLimitBytes);
			ds.a("mruPopulationLimit", mruPopulationLimit);
			ds.a("mruPurgeGoalPct", mruPurgeGoalPct);
			ds.a("mruPurgeWakePct", mruPurgeWakePct);
			ds.ae("mruCheckpointDelay", mruCheckpointDelayMs);
			ds.ae("mruCheckpointPeriod", mruCheckpointPeriodMs);
			ds.ae("mruMinLife", mruMinLifeMs);
			ds.a("mruAuditCycle", mruAuditCycle);
			ds.a("cleanMRU", cleanMRU);
			ds.a("cleanJAR", cleanJAR);
			ds.a("safeNaming", safeNaming);
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
		final String qccThreadName;
		File cndirMRU;
		File cndirJAR;
		long mruSizeLimitBytes = DefaultMruSizeLimitBytes;
		int mruPopulationLimit = DefaultMruPopulationLimit;
		int mruPurgeGoalPct = DefaultMruPurgeGoalPct;
		int mruPurgeWakePct = DefaultMruPurgeWakePct;
		long mruCheckpointDelayMs = DefaultMruCheckpointDelayMs;
		long mruCheckpointPeriodMs = DefaultMruCheckpointPeriodMs;
		long mruMinLifeMs = DefaultMinLifeMs;
		int mruAuditCycle = DefaultAuditCycle;
		boolean cleanMRU = DefaultCleanMRU;
		boolean cleanJAR = DefaultCleanJAR;
		boolean safeNaming = DefaultSafeNaming;
		int bcSizeEst = DefaultSizeEst;
	}
}
