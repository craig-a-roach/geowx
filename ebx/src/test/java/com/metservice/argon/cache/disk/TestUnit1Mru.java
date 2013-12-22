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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertEquals("A:H0M1", probe.statsReport());
			}// cache 1 : A1
			{
				final MruRequest rq = new MruRequest("C", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found Cv1", r.put("C", oFile));
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertEquals("C:H0M1", probe.statsReport());
			}// cache 2 : A1 C1
			{
				probe.allowPurgeAgenda();
				final MruRequest rq = new MruRequest("B", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found Bv1", r.put("B", oFile));
				Assert.assertTrue(probe.reachedPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertFalse("file A purged", r.select("A").exists());
				Assert.assertEquals("B:H0M1", probe.statsReport());
			}// cache 2: C1 B1
			{
				final MruRequest rq = new MruRequest("C", "v1");
				final File oFile = dcc.find(supplier, rq); // HIT
				Assert.assertNotNull("Found Cv1", r.put("C", oFile));
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertEquals("Cv1 Hit", r.select("C").getPath(), oFile.getPath());
				Assert.assertTrue("file C exists", r.select("C").exists());
				Assert.assertEquals("C:H1M0", probe.statsReport());
			}// cache 2: B1 C1
			{
				final MruRequest rq = new MruRequest("D", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNull("NotFound Dv1", r.put("D", oFile));
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
				Assert.assertEquals("D:H0M1", probe.statsReport());
			}// cache 2: B1 C1
			{
				final MruRequest rq = new MruRequest("E", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNull("NotFound Ev1", r.put("E", oFile));
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
				Assert.assertEquals("E:H0M1", probe.statsReport());
			}// cache 2: B1 C1 E?
			{
				final MruRequest rq = new MruRequest("F", "v1");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found Fv1", r.put("F", oFile));
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.reachedAudit());
				Assert.assertEquals("F:H0M1", probe.statsReport());
			}// cache 2: B1 C1 E? F0
			{
				final MruRequest rq = new MruRequest("E", "v1");
				final File oFile = dcc.find(supplier, rq); // HIT
				Assert.assertNull("NotFound Ev1", r.put("E", oFile));
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
				Assert.assertEquals("E:H1M0", probe.statsReport());
			}// cache 2: B1 C1 F0 E?
			dcc.cancel();
			System.out.println(r);
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
			supplier.put("G", 13000, "v1");
			supplier.put("H", 17000, "v1");
			final Reflector r = new Reflector();
			final ExecutorService xc = Executors.newFixedThreadPool(3);
			final Future<File> fuBv1 = xc.submit(new Agent(dcc, supplier, new MruRequest("B", "v1"))); // HIT
			final Future<File> fuFv1 = xc.submit(new Agent(dcc, supplier, new MruRequest("F", "v1"))); // HIT
			final Future<File> fuEv1 = xc.submit(new Agent(dcc, supplier, new MruRequest("E", "v1"))); // HIT
			// cache 2: B1 C1 F0 E?
			try {
				final File oFile = fuBv1.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found Bv1", r.put("B", oFile));
				Assert.assertEquals("Bv1 length", 3000, r.select("B").length());
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 2: C1 F0 E? B1
			try {
				final File oFile = fuFv1.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found Fv1", r.put("F", oFile));
				Assert.assertEquals("Fv1 length", 0, r.select("F").length());
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 2: C1 E? B1 F0
			try {
				final File oFile = fuEv1.get(10, TimeUnit.SECONDS);
				Assert.assertNull("NotFound Ev1", r.put("E", oFile));
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 2: C1 B1 F0 E?
			Assert.assertTrue(probe.noPurgeReclaim());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertTrue(probe.noAudit());
			Assert.assertEquals("B:H1M0 E:H1M0 F:H1M0", probe.statsReport());

			probe.allowPurgeAgenda();
			final Future<File> fuBv2 = xc.submit(new Agent(dcc, supplier, new MruRequest("B", "v2"))); // MISS
			final Future<File> fuGv1 = xc.submit(new Agent(dcc, supplier, new MruRequest("G", "v1"))); // MISS
			try {
				final File oFile = fuBv2.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found Bv2", r.put("B", oFile));
				Assert.assertEquals("Bv2 length", 9000, r.select("B").length());
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 3: C1 F0 E? B2
			try {
				final File oFile = fuGv1.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found Gv1", r.put("G", oFile));
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 5: C1 F0 E? B2 G2 -> cache 4: F0 E? B2 G2

			Assert.assertTrue(probe.reachedPurgeReclaim());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertTrue(probe.noAudit());
			Assert.assertEquals("B:H0M1 G:H0M1", probe.statsReport());

			// cache 4: F0 E? B2 G2
			Assert.assertTrue(probe.noPurgeAgenda());
			final Future<File> fuHv1 = xc.submit(new Agent(dcc, supplier, new MruRequest("H", "v1"))); // MISS
			try {
				final File oFile = fuHv1.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found Hv1", r.put("H", oFile));
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			} // cache 4: F0 E? B2 G2 -> cache 7: F0 E? B2 G2 H3
			final Future<File> fu2Fv1 = xc.submit(new Agent(dcc, supplier, new MruRequest("F", "v1"))); // HIT
			try {
				final File oFile = fu2Fv1.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found Fv1", r.put("F", oFile));
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}
			probe.allowPurgeAgenda();
			Assert.assertTrue(probe.reachedPurgeReclaim());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertEquals("F:H1M0 H:H0M1", probe.statsReport());
			// cache 3: H3 F0

			dcc.cancel();
			System.out.println(r);
			final List<String> mgt = probe.mruManagementTranscript;
			Assert.assertEquals("newState.loadedCheckpoint", mgt.get(0));
			Assert.assertEquals("purge.agenda(StatePre{kB=40  agenda(0)=MglsLg7_M9hE7m1nVAes4YKJNX0})", mgt.get(1));
			Assert.assertEquals("purge.reclaim(StatePost{kB=32})", mgt.get(2));
			Assert.assertEquals("purge.agenda(StatePre{" + "\n" + "kB=56" + "\n" + "agenda(0)=5p8g6faDkg0_tDKavZUeh4sfk3I"
					+ "\n" + "agenda(1)=4BhK3t-ROwdmJmRtP1LDtJw5rW0" + "\n" + "agenda(2)=rk8oHfWl0P88rWNx921cKbbZU-w"
					+ "\n" + "agenda(3)=o2pnGPVFJNhGiU-wS1uIW05D5js" + "\n" + "}StatePre)", mgt.get(3));
			Assert.assertEquals("purge.reclaim(StatePost{kB=24})", mgt.get(3));
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPlatformException ex) {
			Assert.fail(ex.getMessage());
		} catch (final InterruptedException ex) {
			Assert.fail("Latch interrupted");
		}
	}

	private static class Agent implements Callable<File> {

		@Override
		public File call()
				throws Exception {
			return dcc.find(s, rq);
		}

		@Override
		public String toString() {
			return rq.toString();
		}

		public Agent(ArgonDiskCacheController dcc, Supplier s, MruRequest rq) {
			this.dcc = dcc;
			this.s = s;
			this.rq = rq;
		}
		private final ArgonDiskCacheController dcc;
		private final Supplier s;
		private final MruRequest rq;
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

		public void allowPurgeAgenda()
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
					boxPurgeAgenda.poll(25, TimeUnit.SECONDS);
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
			if (show.get()) {
				System.out.println("HIT:" + qccResourceId);
			}
			m_lockHM.lock();
			try {
				final Integer o = m_hit.get(qccResourceId);
				final int x = o == null ? 1 : o.intValue() + 1;
				m_hit.put(qccResourceId, new Integer(x));
			} finally {
				m_lockHM.unlock();
			}
		}

		@Override
		public void liveMruRequestMiss(String qccResourceId) {
			if (show.get()) {
				System.out.println("MISS:" + qccResourceId);
			}
			m_lockHM.lock();
			try {
				final Integer o = m_miss.get(qccResourceId);
				final int x = o == null ? 1 : o.intValue() + 1;
				m_miss.put(qccResourceId, x);
			} finally {
				m_lockHM.unlock();
			}
		}

		public boolean noAudit()
				throws InterruptedException {
			return boxAudit.peek() == null;
		}

		public boolean noPurgeAgenda()
				throws InterruptedException {
			return boxPurgeAgenda.peek() == null;
		}

		public boolean noPurgeReclaim()
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

		public boolean reachedPurgeReclaim()
				throws InterruptedException {
			return boxPurgeReclaim.poll(17, TimeUnit.SECONDS) != null;
		}

		public String statsReport() {
			m_lockHM.lock();
			try {
				final List<String> keys = new ArrayList<String>();
				keys.addAll(m_hit.keySet());
				keys.addAll(m_miss.keySet());
				Collections.sort(keys);
				final int kc = keys.size();
				final StringBuilder sb = new StringBuilder();
				for (int i = 0; i < kc; i++) {
					final String k = keys.get(i);
					final Integer oh = m_hit.get(k);
					final int h = oh == null ? 0 : oh.intValue();
					final Integer om = m_miss.get(k);
					final int m = om == null ? 0 : om.intValue();
					if (i > 0) {
						sb.append(' ');
					}
					sb.append(k).append(":H").append(h).append("M").append(m);
				}
				m_hit.clear();
				m_miss.clear();
				return sb.toString();
			} finally {
				m_lockHM.unlock();
			}
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
		private final Lock m_lockHM = new ReentrantLock();
		final Map<String, Integer> m_hit = new HashMap<String, Integer>();
		final Map<String, Integer> m_miss = new HashMap<String, Integer>();
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
