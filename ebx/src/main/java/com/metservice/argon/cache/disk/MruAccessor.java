/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.util.Date;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonDigester;
import com.metservice.argon.ArgonNameLock;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonSensorHitRate;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.ArgonTransformer;
import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.argon.cache.ArgonCacheException;

/**
 * @author roach
 */
class MruAccessor {

	private static final String TrySave = "Save content to file";

	public static MruAccessor newInstance(ArgonDiskMruCacheController.Config cfg, MruTable table, ArgonSensorHitRate sensor) {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		if (table == null) throw new IllegalArgumentException("object is null");
		if (sensor == null) throw new IllegalArgumentException("object is null");
		final ArgonDigester oDigester = cfg.safeNaming ? ArgonDigester.newSHA1() : null;
		return new MruAccessor(cfg.probe, cfg.cndir, cfg.impliedFreshMs, table, sensor, oDigester);
	}

	private String failSave(Throwable ex) {
		return "Failed to save cacheable resource content..." + ex.getMessage();
	}

	private <R extends IArgonDiskCacheRequest> File findLk(Goal<R> goal)
			throws ArgonCacheException, InterruptedException {
		assert goal != null;
		final long tsRq = goal.tsRequested;
		final String qccFileName = goal.qccFileName;
		File oRef = null;
		final MruDescriptor oDescriptor = m_table.findDescriptor(qccFileName, tsRq);
		if (oDescriptor != null && oDescriptor.isFresh(tsRq)) {
			registerMruHit(goal.request, tsRq);
			if (oDescriptor.isFound()) {
				oRef = oDescriptor.newRef(m_cndir);
			}
		} else {
			registerMruMiss(goal.request, tsRq);
			final IArgonDiskCacheable oCacheable;
			if (oDescriptor == null || oDescriptor.oLastModified == null || !oDescriptor.isFound()) {
				oCacheable = goal.getCacheable();
			} else {
				oCacheable = goal.getCacheableConditional(oDescriptor.oLastModified);
			}
			if (oCacheable == null) {
				probeSupplyError(goal, "returned null");
			} else if (oCacheable instanceof IArgonDiskCacheableWithContent) {
				final IArgonDiskCacheableWithContent c = (IArgonDiskCacheableWithContent) oCacheable;
				final long tsExpires = tsExpires(c.getExpires(), c.getResponseAt(), tsRq);
				final Binary oContent = c.getContent();
				if (oContent == null) {
					m_table.newDescriptor(qccFileName, tsRq, tsExpires);
					probeSupplyNotFound(goal);
				} else {
					final Dcu dcu = Dcu.newInstance(oContent);
					final Tsn lastModified = Tsn.newInstance(c.getLastModified());
					final MruDescriptor neo = m_table.newDescriptor(qccFileName, tsRq, dcu, lastModified, tsExpires);
					oRef = neo.newRef(m_cndir);
					save(oRef, oContent);
				}
			} else if (oCacheable instanceof IArgonDiskCacheableNotModified) {
				final IArgonDiskCacheableNotModified c = (IArgonDiskCacheableNotModified) oCacheable;
				if (oDescriptor == null) {
					probeSupplyError(goal, "returned not-modified cacheable to unconditional request");
				} else {
					final Dcu dcu = oDescriptor.dcu;
					final Tsn lastModified = Tsn.newInstance(oDescriptor.oLastModified);
					final long tsExpires = tsExpires(c.getExpires(), c.getResponseAt(), tsRq);
					final MruDescriptor neo = m_table.newDescriptor(qccFileName, tsRq, dcu, lastModified, tsExpires);
					oRef = neo.newRef(m_cndir);
				}
			} else if (oCacheable instanceof IArgonDiskCacheableNotFound) {
				final IArgonDiskCacheableNotFound c = (IArgonDiskCacheableNotFound) oCacheable;
				final long tsExpires = tsExpires(c.getExpires(), c.getResponseAt(), tsRq);
				m_table.newDescriptor(qccFileName, tsRq, tsExpires);
			} else if (oCacheable instanceof IArgonDiskCacheableError) {
				final IArgonDiskCacheableError c = (IArgonDiskCacheableError) oCacheable;
				final String oqtwReason = ArgonText.oqtw(c.getReason());
				if (oqtwReason == null) {
					probeSupplyError(goal, "did not provide error reason");
				} else {
					probeSupplyError(goal, oqtwReason);
				}
			} else {
				final String msg = "returned unsupported cacheable (" + oCacheable.getClass() + ")";
				probeSupplyError(goal, msg);
			}
		}
		return oRef;
	}

	private void probeSave(File dst, Throwable cause, Binary content) {
		final Ds ds = Ds.triedTo(TrySave, cause, ArgonCacheException.class);
		ds.a("byteCount", content.zptReadOnly);
		m_probe.failFile(ds, dst);
	}

	private <R extends IArgonDiskCacheRequest> void probeSupplyError(Goal<R> goal, String qtwReason) {
		if (m_probe.isLiveSupply()) {
			m_probe.liveSupply("Supplier error..." + qtwReason, goal.request.qccResourceId());
		}
	}

	private <R extends IArgonDiskCacheRequest> void probeSupplyNotFound(Goal<R> goal) {
		if (m_probe.isLiveSupply()) {
			m_probe.liveSupply("Supplier could not find resource " + goal.request.qccResourceId());
		}
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

	private long tsExpires(Date oExpires, Date oResponseAt, long tsNow) {
		if (oExpires != null) return oExpires.getTime();
		final long tsResponseAt = oResponseAt == null ? tsNow : oResponseAt.getTime();
		return tsResponseAt + m_impliedFreshMs;
	}

	public <R extends IArgonDiskCacheRequest> File find(IArgonDiskCacheSupplier<R> supplier, R request)
			throws ArgonCacheException, InterruptedException {
		if (supplier == null) throw new IllegalArgumentException("object is null");
		if (request == null) throw new IllegalArgumentException("object is null");
		final String qccResourceId = request.qccResourceId();
		final String qccFileName = qccFileName(qccResourceId);
		final Goal<R> goal = new Goal<R>(supplier, request, qccFileName);
		final long tsRq = goal.tsRequested;
		final MruDescriptor oDescriptor = m_table.findDescriptor(qccFileName, tsRq);
		if (oDescriptor != null && oDescriptor.isFresh(tsRq)) {
			registerMruHit(request, tsRq);
			return oDescriptor.isFound() ? oDescriptor.newRef(m_cndir) : null;
		}
		m_lock.lock(qccFileName);
		try {
			return findLk(goal);
		} finally {
			m_lock.unlock(qccFileName);
		}
	}

	private MruAccessor(IArgonDiskMruCacheProbe probe, File cndir, long impliedFreshMs, MruTable table,
			ArgonSensorHitRate sensor, ArgonDigester oDigester) {
		assert probe != null;
		assert cndir != null;
		assert table != null;
		m_probe = probe;
		m_cndir = cndir;
		m_impliedFreshMs = impliedFreshMs;
		m_table = table;
		m_sensor = sensor;
		m_oDigester = oDigester;
	}

	private final IArgonDiskMruCacheProbe m_probe;
	private final File m_cndir;
	private final long m_impliedFreshMs;
	private final MruTable m_table;
	private final ArgonSensorHitRate m_sensor;
	private final ArgonDigester m_oDigester;
	private final ArgonNameLock m_lock = new ArgonNameLock();

	private static class Goal<R extends IArgonDiskCacheRequest> {

		public IArgonDiskCacheable getCacheable()
				throws ArgonCacheException, InterruptedException {
			return supplier.getCacheable(request);
		}

		public IArgonDiskCacheable getCacheableConditional(Date lastModified)
				throws ArgonCacheException, InterruptedException {
			assert lastModified != null;
			return supplier.getCacheableConditional(request, lastModified);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("MruAccessor.Goal");
			ds.a("request", request);
			ds.a("fileName", qccFileName);
			ds.at8("requested", tsRequested);
			return ds.s();
		}

		public Goal(IArgonDiskCacheSupplier<R> supplier, R request, String qccFileName) {
			assert supplier != null;
			assert request != null;
			assert qccFileName != null && qccFileName.length() > 0;
			this.supplier = supplier;
			this.request = request;
			this.qccFileName = qccFileName;
			this.tsRequested = ArgonClock.tsNow();
		}
		public final IArgonDiskCacheSupplier<R> supplier;
		public final R request;
		public final String qccFileName;
		public final long tsRequested;
	}
}
