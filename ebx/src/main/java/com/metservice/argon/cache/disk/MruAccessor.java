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
import com.metservice.argon.ArgonTransformer;
import com.metservice.argon.Binary;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Ds;
import com.metservice.argon.cache.ArgonCacheException;
import com.metservice.argon.file.ArgonDirectoryManagement;

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
		if (cfg.clean) {
			ArgonDirectoryManagement.remove(cfg.probe, cfg.cndir, true);
		}
		return new MruAccessor(cfg.probe, cfg.cndir, table, sensor, oDigester);
	}

	private <R extends IArgonDiskCacheRequest> File createRef(Goal<R> goal, IArgonDiskCacheable oCacheable, long tsNow)
			throws ArgonCacheException {
		if (oCacheable == null) return null;
		final Binary oContent = oCacheable.getContent();
		final Date oLastModified = oCacheable.getLastModified();
		final Date oExpires = oCacheable.getExpires();
		final Dcu dcu = Dcu.newInstance(oContent);
		final MruDescriptor neo = m_table.newDescriptor(goal.qccFileName, dcu, oLastModified, oExpires, tsNow);
		final File oRef = neo.createRef(m_cndir);
		if (oContent != null && oRef != null) {
			save(oRef, oContent);
		}
		return oRef;
	}

	private String failSave(Throwable ex) {
		return "Failed to save cacheable resource content..." + ex.getMessage();
	}

	private <R extends IArgonDiskCacheRequest> File findLk(Goal<R> goal)
			throws ArgonCacheException, InterruptedException {
		assert goal != null;
		final Date now = goal.now();
		final long tsNow = now.getTime();
		final File oRef;
		final MruDescriptor oDescriptor = m_table.findDescriptor(goal.qccFileName, tsNow);
		if (oDescriptor != null && oDescriptor.isFresh(tsNow)) {
			registerMruHit(goal.request, tsNow);
			oRef = oDescriptor.createRef(m_cndir);
		} else {
			registerMruMiss(goal.request, tsNow);
			final IArgonDiskCacheable oCacheable = goal.getCacheable(oDescriptor);
			oRef = createRef(goal, oCacheable, tsNow);
		}
		return oRef;
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

	public <R extends IArgonDiskCacheRequest> File find(IArgonDiskCacheSupplier<R> supplier, R request)
			throws ArgonCacheException, InterruptedException {
		if (supplier == null) throw new IllegalArgumentException("object is null");
		if (request == null) throw new IllegalArgumentException("object is null");
		final String qccResourceId = request.qccResourceId();
		final String qccFileName = qccFileName(qccResourceId);
		final Goal<R> goal = new Goal<R>(supplier, request, qccFileName);
		final long tsNow = goal.now().getTime();
		final MruDescriptor oDescriptor = m_table.findDescriptor(qccFileName, tsNow);
		if (oDescriptor != null && oDescriptor.isFresh(tsNow)) {
			registerMruHit(request, tsNow);
			return oDescriptor.createRef(m_cndir);
		}
		m_lock.lock(qccFileName);
		try {
			return findLk(goal);
		} finally {
			m_lock.unlock(qccFileName);
		}
	}

	private MruAccessor(IArgonDiskMruCacheProbe probe, File cndir, MruTable table, ArgonSensorHitRate sensor,
			ArgonDigester oDigester) {
		assert probe != null;
		assert cndir != null;
		assert table != null;
		m_probe = probe;
		m_cndir = cndir;
		m_table = table;
		m_sensor = sensor;
		m_oDigester = oDigester;
	}

	private final IArgonDiskMruCacheProbe m_probe;
	private final File m_cndir;
	private final MruTable m_table;
	private final ArgonSensorHitRate m_sensor;
	private final ArgonDigester m_oDigester;
	private final ArgonNameLock m_lock = new ArgonNameLock();

	private static class Goal<R extends IArgonDiskCacheRequest> {

		public IArgonDiskCacheable getCacheable(MruDescriptor oDescriptor)
				throws ArgonCacheException, InterruptedException {
			final Date oLastModified = oDescriptor == null ? null : oDescriptor.oLastModified;
			if (oLastModified == null) return supplier.getCacheable(request);
			return supplier.getCacheableConditional(request, oLastModified);
		}

		public Date now() {
			final Date oNow = supplier.getNow();
			return oNow == null ? DateFactory.newDate(ArgonClock.tsNow()) : oNow;
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("MruAccessor.Goal");
			ds.a("request", request);
			ds.a("fileName", qccFileName);
			return ds.s();
		}

		public Goal(IArgonDiskCacheSupplier<R> supplier, R request, String qccFileName) {
			assert supplier != null;
			assert request != null;
			assert qccFileName != null && qccFileName.length() > 0;
			this.supplier = supplier;
			this.request = request;
			this.qccFileName = qccFileName;
		}
		public final IArgonDiskCacheSupplier<R> supplier;
		public final R request;
		public final String qccFileName;
	}
}
