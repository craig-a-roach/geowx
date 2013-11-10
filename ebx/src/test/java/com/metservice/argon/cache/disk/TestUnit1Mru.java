/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.TestHelpC;
import com.metservice.argon.cache.ArgonCacheException;
import com.metservice.argon.management.IArgonSpaceId;

/**
 * @author roach
 */
public class TestUnit1Mru {

	@Test
	public void t50() {
		final ArgonServiceId SID = TestHelpC.SID;
		final SpaceId SPACE = new SpaceId("t50");
		final Probe probe = new Probe();
		try {
			final ArgonDiskCacheController.Config cfg = ArgonDiskCacheController.newConfig(probe, SID, SPACE);
			cfg.cacheFileLimit = 10;
			cfg.bcCacheSizeQuota = 3 * CArgon.K * 8;
			final ArgonDiskCacheController dcc = ArgonDiskCacheController.newInstance(cfg);
			final Supplier supplier = new Supplier();
			supplier.put("A", 5000, "v1");
			supplier.put("B", 3000, "v1");
			supplier.put("C", 7000, "v1");
			{
				final MruRequest rq = new MruRequest("A", "v1");
				final File oFile = dcc.find(supplier, rq);
				Assert.assertNotNull("Found Av1", oFile);
			}// cache 1
			{
				final MruRequest rq = new MruRequest("C", "v1");
				final File oFile = dcc.find(supplier, rq);
				Assert.assertNotNull("Found Cv1", oFile);
			}// cache 2
		} catch (final ArgonCacheException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPlatformException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	private static class Cacheable implements IArgonDiskCacheable {

		private static Binary createBinary(int bc) {
			if (bc < 0) return null;
			final byte[] payload = new byte[bc];
			for (int i = 0; i < bc; i++) {
				final int ib = 32 + (i % 64);
				payload[i] = (byte) ib;
			}
			return Binary.newFromTransient(payload);
		}

		@Override
		public Binary getContent() {
			return m_oContent;
		}

		@Override
		public String getContentValidator() {
			return m_ozValidator;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("<");
			if (m_ozValidator == null) {
				sb.append("nil");
			} else {
				sb.append(m_ozValidator);
			}
			sb.append(">");
			if (m_oContent == null) {
				sb.append("na");
			} else {
				sb.append(m_oContent.byteCount());
			}
			return sb.toString();
		}

		public Cacheable(int bcPayload, String ozValidator) {
			m_oContent = createBinary(bcPayload);
			m_ozValidator = ozValidator;
		}
		private final Binary m_oContent;
		private final String m_ozValidator;
	}

	private static class MruRequest implements IArgonDiskCacheMruRequest {

		@Override
		public String getContentValidator(Date now) {
			return zContentValidator;
		}

		@Override
		public boolean isValid(Date now, String zContentValidator) {
			return this.zContentValidator.equals(zContentValidator);
		}

		@Override
		public String qccResourceId() {
			return qccResourceId;
		}

		@Override
		public String toString() {
			return qccResourceId + "(" + zContentValidator + ")";
		}

		public MruRequest(String qccResourceId, String zContentValidator) {
			this.qccResourceId = qccResourceId;
			this.zContentValidator = zContentValidator;
		}
		public final String qccResourceId;
		public final String zContentValidator;
	}

	private static class Probe implements IArgonDiskCacheProbe {

		@Override
		public void failFile(Ds diagnostic, File ofile) {
			if (show.get()) {
				System.out.println(diagnostic);
				if (ofile != null) {
					System.out.println(ofile);
				}
			}
			failFile.set(true);
		}

		@Override
		public void failSoftware(Ds diagnostic) {
			if (show.get()) {
				System.out.println(diagnostic);
			}
			failSoftware.set(true);
		}

		@Override
		public void failSoftware(RuntimeException exRT) {
			if (show.get()) {
				System.out.println(Ds.format(exRT, true));
			}
			failSoftware.set(true);
		}

		@Override
		public void warnFile(Ds diagnostic, File ofile) {
			if (show.get()) {
				System.out.println(diagnostic);
				if (ofile != null) {
					System.out.println(ofile);
				}
			}
			warnFile.set(true);
		}

		public Probe() {
		}
		final AtomicBoolean show = new AtomicBoolean(true);
		final AtomicBoolean failFile = new AtomicBoolean(false);
		final AtomicBoolean warnFile = new AtomicBoolean(false);
		final AtomicBoolean failSoftware = new AtomicBoolean(false);
	}

	private static class SpaceId implements IArgonSpaceId {

		@Override
		public String format() {
			return m_qId;
		}

		public SpaceId(String qId) {
			m_qId = qId;
		}

		private final String m_qId;
	}

	private static class Supplier implements IArgonDiskCacheSupplier<MruRequest> {

		@Override
		public IArgonDiskCacheable getCacheable(MruRequest request)
				throws ArgonCacheException {
			return m_map.get(request.qccResourceId);
		}

		public void put(String qccResourceId, int bcPayload, String ozValidator) {
			final Cacheable cacheable = new Cacheable(bcPayload, ozValidator);
			m_map.put(qccResourceId, cacheable);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Supplier");
			ds.a("rid_cacheable", m_map);
			return ds.s();
		}

		public Supplier() {
		}
		private final Map<String, Cacheable> m_map = new HashMap<>();
	}
}
