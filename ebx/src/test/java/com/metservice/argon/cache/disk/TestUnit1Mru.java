/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonJoiner;
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
	public void t50_main() {
		final ArgonServiceId SID = TestHelpC.SID;
		final SpaceId SPACE = new SpaceId("t50");
		try {
			final Probe probe = new Probe();
			final ArgonDiskCacheController.Config cfg = ArgonDiskCacheController.newConfig(probe, SID, SPACE);
			cfg.mruPopulationLimit(10);
			cfg.enableMRUClean(true);
			cfg.mruSizeLimitBytes(3 * CArgon.K * 8);
			cfg.mruAuditCycle(5);
			cfg.mruCheckpointHoldoff(TimeUnit.SECONDS, 1);
			cfg.mruCheckpointPeriod(TimeUnit.SECONDS, 2);
			final ArgonDiskCacheController dcc = ArgonDiskCacheController.newInstance(cfg);
			final Supplier supplier = new Supplier();
			supplier.put("A", 5000, "v1");
			supplier.put("B", 3000, "v1");
			supplier.put("C", 7000, "v1");
			supplier.put("E", -1, "v1");
			supplier.put("F", 0, "v1");
			try {
				new File(cfg.cndirMRU, "wilful_damage").createNewFile();
			} catch (final IOException ex) {
				Assert.fail("Cannot create damage file..." + ex.getMessage());
			}
			final Reflector r = new Reflector();
			{
				final MruRequest rq = new MruRequest("A", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found Av1", r.put("A", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
			}// cache 1 : A1
			{
				final MruRequest rq = new MruRequest("C", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found Cv1", r.put("C", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
			}// cache 2 : A1 C1
			{
				probe.allowPurge();
				final MruRequest rq = new MruRequest("B", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found Bv1", r.put("B", oFile));
				Assert.assertTrue(probe.reachedPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertFalse("file A purged", r.select("A").exists());
			}// cache 2: C1 B1
			{
				final MruRequest rq = new MruRequest("C", "v1");
				final File oFile = dcc.find(supplier, rq); // HIT
				Assert.assertNotNull("Found Cv1", r.put("C", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertEquals("Cv1 Hit", r.select("C").getPath(), oFile.getPath());
				Assert.assertTrue("file C exists", r.select("C").exists());
			}// cache 2: B1 C1
			{
				final MruRequest rq = new MruRequest("D", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNull("NotFound Dv1", r.put("D", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
			}// cache 2: B1 C1
			{
				final MruRequest rq = new MruRequest("E", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNull("NotFound Ev1", r.put("E", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
			}// cache 2: B1 C1 E?
			{
				final MruRequest rq = new MruRequest("F", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found Fv1", r.put("F", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.reachedAudit());
			}// cache 2: B1 C1 E? F0
			{
				final MruRequest rq = new MruRequest("E", "v1");
				final File oFile = dcc.find(supplier, rq); // HIT
				Assert.assertNull("NotFound Ev1", r.put("E", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
			}// cache 2: B1 C1 F0 E?
			dcc.cancel();
			System.out.println(r);
			final List<String> rqt = probe.mruRequestTranscript;
			Assert.assertEquals("MISS:A", rqt.get(0));
			Assert.assertEquals("MISS:C", rqt.get(1));
			Assert.assertEquals("MISS:B", rqt.get(2));
			Assert.assertEquals("HIT:C", rqt.get(3));
			Assert.assertEquals("MISS:D", rqt.get(4));
			Assert.assertEquals("MISS:E", rqt.get(5));
			Assert.assertEquals("MISS:F", rqt.get(6));
			Assert.assertEquals("HIT:E", rqt.get(7));
			final List<String> mgt = probe.mruManagementTranscript;
			Assert.assertEquals("newState.noCheckpoint", mgt.get(0));
			Assert.assertEquals("newState.initialise", mgt.get(1));
			Assert.assertEquals("purge.agenda(StatePre{kB=24  agenda(0)=bc1M4j2I4u6VaLpUbAB8Y9kTHBs})", mgt.get(2));
			Assert.assertEquals("purge.reclaim(StatePost{kB=16})", mgt.get(3));
			Assert.assertEquals("audit.delete(State{unreferenced(0)=wilful_damage})", mgt.get(4));
		} catch (final ArgonCacheException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPlatformException ex) {
			Assert.fail(ex.getMessage());
		} catch (final InterruptedException ex) {
			Assert.fail("Latch interrupted");
		}
		try {
			final Probe probe = new Probe();
			final ArgonDiskCacheController.Config cfg = ArgonDiskCacheController.newConfig(probe, SID, SPACE);
			cfg.mruPopulationLimit(10);
			cfg.enableMRUClean(false);
			cfg.mruSizeLimitBytes(5 * CArgon.K * 8);
			cfg.mruAuditCycle(5);
			cfg.mruCheckpointHoldoff(TimeUnit.SECONDS, 1);
			cfg.mruCheckpointPeriod(TimeUnit.SECONDS, 2);
			final ArgonDiskCacheController dcc = ArgonDiskCacheController.newInstance(cfg);
			final Supplier supplier = new Supplier();
			supplier.put("B", 9000, "v2");
			supplier.put("C", 5000, "v2");
			supplier.put("E", -1, "v1");
			supplier.put("F", 0, "v1");
			final Reflector r = new Reflector();
			// cache 2: B1 C1 F0 E?
			{
				final MruRequest rq = new MruRequest("B", "v1");
				final File oFile = dcc.find(supplier, rq); // HIT
				Assert.assertNotNull("Found Bv1", r.put("B", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
				Assert.assertEquals("Bv1 length", 3000, r.select("B").length());
			}// cache 2: C1 F0 E? B1
			{
				final MruRequest rq = new MruRequest("F", "v1");
				final File oFile = dcc.find(supplier, rq); // HIT
				Assert.assertNotNull("Found Fv1", r.put("F", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());

			}// cache 2: C1 E? B1 F0
			{
				final MruRequest rq = new MruRequest("E", "v1");
				final File oFile = dcc.find(supplier, rq); // HIT
				Assert.assertNull("NotFound Ev1", r.put("E", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
			}// cache 2: C1 B1 F0 E?
			{
				final MruRequest rq = new MruRequest("B", "v2");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found Bv2", r.put("B", oFile));
				Assert.assertTrue(probe.noPurge());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
				Assert.assertEquals("Bv1 length", 9000, r.select("B").length());
			}// cache 3: C1 F0 E? B2
			dcc.cancel();
			System.out.println(r);
			final List<String> rqt = probe.mruRequestTranscript;
			Assert.assertEquals("HIT:B", rqt.get(0));
			Assert.assertEquals("HIT:F", rqt.get(1));
			Assert.assertEquals("HIT:E", rqt.get(2));
			Assert.assertEquals("MISS:B", rqt.get(3));
		} catch (final ArgonCacheException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPlatformException ex) {
			Assert.fail(ex.getMessage());
		} catch (final InterruptedException ex) {
			Assert.fail("Latch interrupted");
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
			return (this.zContentValidator.compareTo(zContentValidator) <= 0);
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

		public void allowPurge()
				throws InterruptedException {
			boxPurgeAgenda.put("ALLOW");
		}

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
		public boolean isLiveMruManagement() {
			return true;
		}

		@Override
		public boolean isLiveMruRequest() {
			return true;
		}

		@Override
		public void liveMruManagement(String message, Object... args) {
			final String qargs = "(" + ArgonJoiner.zComma(args) + ")";
			if (show.get()) {
				System.out.println("mru." + message + qargs);
			}
			try {
				if (message.equals("purge.agenda")) {
					mruManagementTranscript.add(message + qargs);
					boxPurgeAgenda.poll(5, TimeUnit.SECONDS);
				} else if (message.equals("purge.reclaim")) {
					mruManagementTranscript.add(message + qargs);
					boxPurgeReclaim.put("PURGE");
				} else if (message.equals("audit.delete")) {
					mruManagementTranscript.add(message + qargs);
					boxAudit.put("AUDIT");
				} else if (message.equals("checkpoint.saved")) {
					boxCheckpoint.put("CHECKPOINT");
				} else {
					if (message.startsWith("newState.")) {
						mruManagementTranscript.add(message);
					}
				}
			} catch (final InterruptedException ex) {
				System.out.println("MruManagement Interrupted");
			}
		}

		@Override
		public void liveMruRequestHit(String qccResourceId) {
			mruRequestTranscript.add("HIT:" + qccResourceId);
			if (show.get()) {
				System.out.println("HIT: " + qccResourceId);
			}
		}

		@Override
		public void liveMruRequestMiss(String qccResourceId) {
			mruRequestTranscript.add("MISS:" + qccResourceId);
			if (show.get()) {
				System.out.println("MISS: " + qccResourceId);
			}
		}

		public boolean noAudit()
				throws InterruptedException {
			return boxAudit.peek() == null;
		}

		public boolean noPurge()
				throws InterruptedException {
			return boxPurgeReclaim.peek() == null;
		}

		public boolean reachedAudit()
				throws InterruptedException {
			return boxAudit.poll(7, TimeUnit.SECONDS) != null;
		}

		public boolean reachedCheckpoint()
				throws InterruptedException {
			return boxCheckpoint.poll(7, TimeUnit.SECONDS) != null;
		}

		public boolean reachedPurge()
				throws InterruptedException {
			return boxPurgeReclaim.poll(7, TimeUnit.SECONDS) != null;
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
		final BlockingQueue<String> boxCheckpoint = new LinkedBlockingQueue<>(1);
		final BlockingQueue<String> boxPurgeAgenda = new LinkedBlockingQueue<>(1);
		final BlockingQueue<String> boxPurgeReclaim = new LinkedBlockingQueue<>(1);
		final BlockingQueue<String> boxAudit = new LinkedBlockingQueue<>(1);
		final List<String> mruManagementTranscript = new ArrayList<>();
		final List<String> mruRequestTranscript = new ArrayList<>();
	}

	private static class Reflector {

		public File put(String rid, File oFile) {
			if (oFile == null) {
				notfound.add(rid);
			} else {
				found.put(rid, oFile);
			}
			return oFile;
		}

		public File select(String rid) {
			if (rid == null || rid.length() == 0) throw new IllegalArgumentException("string is null or empty");
			final File oFile = found.get(rid);
			if (oFile == null) throw new IllegalArgumentException("Unknown resource '" + rid + "'");
			return oFile;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("Found");
			final List<String> fk = new ArrayList<>(found.keySet());
			final int fkc = fk.size();
			Collections.sort(fk);
			for (int i = 0; i < fkc; i++) {
				final String rid = fk.get(i);
				final String fn = found.get(rid).getName();
				sb.append("\n").append(rid).append('=').append(fn);
			}
			sb.append("\nNotFound:");
			final List<String> nfk = new ArrayList<>(notfound);
			final int nfkc = nfk.size();
			Collections.sort(nfk);
			for (int i = 0; i < nfkc; i++) {
				final String rid = nfk.get(i);
				sb.append(' ').append(rid);
			}
			return sb.toString();
		}

		public Reflector() {
		}
		final Map<String, File> found = new HashMap<String, File>();
		final Set<String> notfound = new HashSet<String>();
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
