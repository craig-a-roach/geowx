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

	private static long tsResponseAt(Date oResponseAt, long tsNow) {
		return oResponseAt == null ? tsNow : oResponseAt.getTime();
	}

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
		final File oRef;
		final MruDescriptor oDescriptor = m_table.findDescriptor(goal.qccFileName, tsRq);
		if (oDescriptor != null && oDescriptor.isFresh(tsRq)) {
			registerMruHit(goal.request, tsRq);
			oRef = oDescriptor.createRef(m_cndir);
		} else {
			registerMruMiss(goal.request, tsRq);
			final MruConditional oCond = oDescriptor == null ? null : oDescriptor.createConditional();
			final IArgonDiskCacheable oCacheable;
			if (oCond == null) {
				oCacheable = goal.getCacheable();
			} else {
				oCacheable = goal.getCacheableConditional(oCond);
			}
			if (oCacheable == null) {
				probeSupplyError(goal, "returned null");
				oRef = null;
			} else if (oCacheable instanceof IArgonDiskCacheableWithContent) {
				final IArgonDiskCacheableWithContent c = (IArgonDiskCacheableWithContent) oCacheable;
				final Date oExpires = c.getExpires();
				final Binary oContent = c.getContent();
				final Dcu dcu = Dcu.newInstance(oContent);
				if (oContent == null || !dcu.exists()) {
					if (oExpires != null) {
						putRefNotFound(goal, oExpires);
					}
					oRef = null;
				} else {
					final Date oResponseAt = c.getResponseAt();
					final long tsResponseAt = tsResponseAt(oResponseAt, tsRq);
					final Tsn lastModified = Tsn.newInstance(c.getLastModified());
					final long tsExpires = tsExpires(oExpires, tsResponseAt);
					final MruDescriptor neo = m_table.newDescriptor(goal.qccFileName, tsRq, dcu, lastModified, tsExpires);
					oRef = neo.createRef(m_cndir);
					if (oContent != null && oRef != null) {
						save(oRef, oContent);
					}
				}
			} else if (oCacheable instanceof IArgonDiskCacheableNotModified) {
				final IArgonDiskCacheableNotModified c = (IArgonDiskCacheableNotModified) oCacheable;
				if (oCond == null) {
					probeSupplyError(goal, "returned not-modified cacheable to unconditional request");
					oRef = null;
				} else {
					oRef = createRefNotModified(goal, c, oCond);
				}
			} else if (oCacheable instanceof IArgonDiskCacheableNotFound) {
				final IArgonDiskCacheableNotFound c = (IArgonDiskCacheableNotFound) oCacheable;
				final Date oExpires = c.getExpires();
				if (oExpires != null) {
					putRefNotFound(goal, oExpires);
				}
				oRef = null;
			} else if (oCacheable instanceof IArgonDiskCacheableError) {
				final IArgonDiskCacheableError c = (IArgonDiskCacheableError) oCacheable;
				final String oqtwReason = ArgonText.oqtw(c.getReason());
				if (oqtwReason == null) {
					probeSupplyError(goal, "did not provide error reason");
				} else {
					probeSupplyError(goal, oqtwReason);
				}
				oRef = null;
			} else {
				final String msg = "returned unsupported cacheable (" + oCacheable.getClass() + ")";
				probeSupplyError(goal, msg);
				oRef = null;
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

	}

	private <R extends IArgonDiskCacheRequest> void putRefNotFound(Goal<R> goal, Date expires) {

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

	private long tsExpires(Date oExpires, long tsResponseAt) {
		return oExpires == null ? (tsResponseAt + m_impliedFreshMs) : oExpires.getTime();
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
			return oDescriptor.createRef(m_cndir);
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

		public IArgonDiskCacheable getCacheableConditional(MruConditional cond)
				throws ArgonCacheException, InterruptedException {
			assert cond != null;
			return supplier.getCacheableConditional(request, cond.lastModified);
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
