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

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonJoiner;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.DateFactory;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonSensor;
import com.metservice.argon.IArgonSensorRatio;
import com.metservice.argon.IArgonSpaceId;
import com.metservice.argon.TestHelpC;
import com.metservice.argon.cache.ArgonCacheException;

/**
 * @author roach
 */
public class TestUnit1Mru {

	private static void fixNow(String tx) {
		ArgonClock.simulatedNow(DateFactory.newTsConstantFromTX(tx));
	}

	@Test
	public void t20_space() {
		final String LM03 = "20131201T0300Z";
		final String LM09 = "20131201T0900Z";
		final String LM11 = "20131201T1100Z";
		final String EX08 = "20131201T0800Z";
		final String EX11 = "20131201T1100Z";
		final String EX13 = "20131201T1300Z";
		final ArgonServiceId SID = TestHelpC.SID;
		final SpaceId SPACE = new SpaceId("t20");
		try {
			final Probe probe = new Probe();
			final ArgonDiskMruCacheController.Config cfg = ArgonDiskMruCacheController.newConfig(probe, SID, SPACE);
			cfg.enableSafeNaming(false);
			cfg.populationLimit(10);
			cfg.enableClean(true);
			cfg.sizeLimitBytes(3 * CArgon.K * 8);
			cfg.auditCycle(5);
			cfg.checkpointHoldoff(TimeUnit.SECONDS, 1);
			cfg.checkpointPeriod(TimeUnit.SECONDS, 2);
			cfg.impliedFresh(TimeUnit.SECONDS, 60);
			cfg.minLife(TimeUnit.SECONDS, 3);
			final ArgonDiskMruCacheController dcc = ArgonDiskMruCacheController.newInstance(cfg);
			final Supplier supplier = new Supplier();
			supplier.put("A", 5000, LM03, EX08);
			supplier.put("B", 3000, LM03, EX08);
			supplier.put("C", 7000, LM03, EX08);
			supplier.put("E", -1, LM03, EX08);
			supplier.put("F", 0, LM03, EX11);
			try {
				new File(cfg.cndir, "wilful_damage").createNewFile();
			} catch (final IOException ex) {
				Assert.fail("Cannot create damage file..." + ex.getMessage());
			}
			fixNow("20131201T0600Z");
			File fA = null;
			{
				final Request rq = new Request("A");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found A", oFile);
				Assert.assertTrue(probe.noPurgeAgenda());
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertEquals("A:H0M1", probe.statsReport());
				fA = oFile;
			}// cache 1 : A1
			{
				final Request rq = new Request("C");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found C", oFile);
				Assert.assertTrue(probe.noPurgeAgenda());
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertEquals("C:H0M1", probe.statsReport());
			}// cache 2 : A1 C1
			{
				final Request rq = new Request("B");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found B", oFile);
				Assert.assertTrue(probe.reachedPurgeAgenda());
				probe.allowPurgeReclaim();
				Assert.assertTrue(probe.reachedPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertFalse("file A purged", fA.exists());
				Assert.assertEquals("B:H0M1", probe.statsReport());
			}// cache 2: C1 B1
			{
				final Request rq = new Request("C");
				final File oFile = dcc.find(supplier, rq); // HIT
				Assert.assertNotNull("Found C", oFile);
				Assert.assertTrue(probe.noPurgeAgenda());
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue("file C exists", oFile.exists());
				Assert.assertEquals("C:H1M0", probe.statsReport());
			}// cache 2: B1 C1
			{
				final Request rq = new Request("D"); // Not in Supplier
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNull("NotFound D", oFile);
				Assert.assertTrue(probe.noPurgeAgenda());
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.noCheckpoint());
				Assert.assertTrue(probe.noAudit());
				Assert.assertEquals("D:H0M1", probe.statsReport());
			}// cache 2: B1 C1
			{
				final Request rq = new Request("E");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNull("NotFound E", oFile);
				Assert.assertTrue(probe.noPurgeAgenda());
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
				Assert.assertEquals("E:H0M1", probe.statsReport());
			}// cache 2: B1 C1 E?
			{
				final Request rq = new Request("F");
				final File oFile = dcc.find(supplier, rq); // MISS
				Assert.assertNotNull("Found F", oFile);
				Assert.assertTrue(probe.noPurgeAgenda());
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.reachedAudit());
				Assert.assertEquals("F:H0M1", probe.statsReport());
			}// cache 2: B1 C1 E? F0
			{
				final Request rq = new Request("E");
				final File oFile = dcc.find(supplier, rq); // HIT
				Assert.assertNull("NotFound E", oFile);
				Assert.assertTrue(probe.noPurgeAgenda());
				Assert.assertTrue(probe.noPurgeReclaim());
				Assert.assertTrue(probe.reachedCheckpoint());
				Assert.assertTrue(probe.noAudit());
				Assert.assertEquals("E:H1M0", probe.statsReport());
			}// cache 2: B1 C1 F0 E?
			dcc.cancel();
			final List<String> mgt = probe.mruManagementTranscript;
			Assert.assertEquals("newState.noCheckpoint", mgt.get(0));
			Assert.assertEquals("newState.initialise", mgt.get(1));
			Assert.assertEquals("purge.agenda(StatePre{kB=24  agenda(0)=A})", mgt.get(2));
			Assert.assertEquals("purge.reclaim(StatePost{kB=16})", mgt.get(3));
			Assert.assertEquals("audit.delete(State{unreferenced(0)=wilful_damage})", mgt.get(4));

			final IArgonSensor oSensor = dcc.findSensor(dcc.getSensorId(0));
			Assert.assertNotNull(oSensor);
			Assert.assertTrue(oSensor instanceof IArgonSensorRatio);
			final float sensorRatio = ((IArgonSensorRatio) oSensor).ratio();
			Assert.assertTrue(!Float.isNaN(sensorRatio) && sensorRatio > 0.0 && sensorRatio < 1.0f);
			System.out.println("SENSOR: " + oSensor);

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
			final ArgonDiskMruCacheController.Config cfg = ArgonDiskMruCacheController.newConfig(probe, SID, SPACE);
			cfg.enableSafeNaming(false);
			cfg.populationLimit(10);
			cfg.enableClean(false);
			cfg.sizeLimitBytes(5 * CArgon.K * 8);
			cfg.auditCycle(3);
			cfg.checkpointHoldoff(TimeUnit.SECONDS, 1);
			cfg.checkpointPeriod(TimeUnit.SECONDS, 2);
			cfg.minLife(TimeUnit.SECONDS, 3);
			final ArgonDiskMruCacheController dcc = ArgonDiskMruCacheController.newInstance(cfg);
			final Supplier supplier = new Supplier();
			fixNow("20131201T0700Z");
			final ExecutorService xc = Executors.newFixedThreadPool(3);
			probe.setConcurrentMode(true);
			final Future<File> fuB = xc.submit(new Agent(dcc, supplier, new Request("B"))); // HIT
			final Future<File> fuF = xc.submit(new Agent(dcc, supplier, new Request("F"))); // HIT
			final Future<File> fuE = xc.submit(new Agent(dcc, supplier, new Request("E"))); // HIT
			// cache 2: B1 C1 F0 E?
			try {
				final File oFile = fuB.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found B", oFile);
				Assert.assertEquals("Bv1 length", 3000, oFile.length());
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}
			try {
				final File oFile = fuF.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found F", oFile);
				Assert.assertEquals("Fv1 length", 0, oFile.length());
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}
			try {
				final File oFile = fuE.get(10, TimeUnit.SECONDS);
				Assert.assertNull("NotFound E", oFile);
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}
			// cache 2: C1 (B1 F0 E?)
			Assert.assertTrue(probe.noPurgeAgenda());
			Assert.assertTrue(probe.noPurgeReclaim());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertTrue(probe.noAudit());
			Assert.assertEquals("B:H1M0 E:H1M0 F:H1M0", probe.statsReport());

			supplier.put("B", 9000, LM09, EX11);
			supplier.put("G", 13000, LM09, EX11);
			supplier.put("H", 17000, LM09, EX11);

			fixNow("20131201T1000Z");
			probe.setConcurrentMode(true);
			probe.allowPurgeReclaim();
			final Future<File> fuBm09 = xc.submit(new Agent(dcc, supplier, new Request("B"))); // MISS
			final Future<File> fuG = xc.submit(new Agent(dcc, supplier, new Request("G"))); // MISS
			try {
				final File oFile = fuBm09.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found B mod 09", oFile);
				Assert.assertEquals("B mod 09 length", 9000, oFile.length());
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 3: C1 (F0 E?) B2
			try {
				final File oFile = fuG.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found G", oFile);
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 5: C1 (F0 E?) B2 G2 -> cache 4: (F0 E?) B2 G2

			Assert.assertTrue(probe.reachedPurgeAgenda());
			Assert.assertTrue(probe.reachedPurgeReclaim());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertTrue(probe.noAudit());
			Assert.assertEquals("B:H0M1 G:H0M1", probe.statsReport());

			// cache 4: (F0 E?) B2 G2
			Assert.assertTrue(probe.noPurgeAgenda());
			final Future<File> fuH = xc.submit(new Agent(dcc, supplier, new Request("H"))); // MISS
			try {
				final File oFile = fuH.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found H", oFile);
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}
			// cache 7: (F0 E?) B2 G2 H3
			Assert.assertEquals("H:H0M1", probe.statsReport());
			Assert.assertTrue(probe.reachedPurgeAgenda());
			final Future<File> fu2F = xc.submit(new Agent(dcc, supplier, new Request("F"))); // HIT
			try {
				final File oFile = fu2F.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found F", oFile);
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}
			probe.allowPurgeReclaim();
			Assert.assertTrue(probe.reachedPurgeReclaim());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertTrue(probe.noAudit());
			Assert.assertEquals("F:H1M0", probe.statsReport());
			// cache 3: H3 F0

			supplier.put("J", 7000, LM11, EX13);

			fixNow("20131201T1200Z");
			final Future<File> fuJav1 = xc.submit(new Agent(dcc, supplier, new Request("J"))); // MISS
			final Future<File> fuJbv1 = xc.submit(new Agent(dcc, supplier, new Request("J"))); // HIT
			final Future<File> fuJcv1 = xc.submit(new Agent(dcc, supplier, new Request("J"))); // HIT
			try {
				final File oFileA = fuJav1.get(10, TimeUnit.SECONDS);
				final File oFileB = fuJbv1.get(10, TimeUnit.SECONDS);
				final File oFileC = fuJcv1.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found Jav1", oFileA);
				Assert.assertNotNull("Found Jbv1", oFileB);
				Assert.assertNotNull("Found Jcv1", oFileC);
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}
			Assert.assertTrue(probe.noPurgeAgenda());
			Assert.assertTrue(probe.noPurgeReclaim());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertTrue(probe.noAudit());
			Assert.assertEquals("J:H2M1", probe.statsReport());
			// cache 4: H3 F0 J1

			dcc.cancel();
			final List<String> mgt = probe.mruManagementTranscript;
			Assert.assertEquals("newState.loadedCheckpoint", mgt.get(0));
			Assert.assertEquals("purge.agenda(StatePre{kB=40  agenda(0)=C})", mgt.get(1));
			Assert.assertEquals("purge.reclaim(StatePost{kB=32})", mgt.get(2));
			final String mgt3a = "purge.agenda(StatePre{kB=56  agenda(0)=F  agenda(1)=E  agenda(2)=B  agenda(3)=G})";
			final String mgt3b = "purge.agenda(StatePre{kB=56  agenda(0)=E  agenda(1)=F  agenda(2)=B  agenda(3)=G})";
			Assert.assertTrue(mgt.get(3).equals(mgt3a) || mgt.get(3).equals(mgt3b));
			Assert.assertEquals("purge.file.trackerNotPurgeSafe", mgt.get(4));
			Assert.assertEquals("purge.reclaim(StatePost{kB=24})", mgt.get(5));
		} catch (final ArgonPermissionException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonPlatformException ex) {
			Assert.fail(ex.getMessage());
		} catch (final InterruptedException ex) {
			Assert.fail("Latch interrupted");
		}
	}

	@Test
	public void t50_age() {
		final String LM03 = "20131201T0300Z";
		final String EX08 = "20131201T0800Z";
		final ArgonServiceId SID = TestHelpC.SID;
		final SpaceId SPACE = new SpaceId("t50");
		try {
			final Probe probe = new Probe();
			final ArgonDiskMruCacheController.Config cfg = ArgonDiskMruCacheController.newConfig(probe, SID, SPACE);
			cfg.enableSafeNaming(false);
			cfg.populationLimit(10);
			cfg.enableClean(true);
			cfg.sizeLimitBytes(3 * CArgon.K * 8);
			cfg.auditCycle(4);
			cfg.checkpointHoldoff(TimeUnit.SECONDS, 1);
			cfg.checkpointPeriod(TimeUnit.SECONDS, 2);
			cfg.minLife(TimeUnit.SECONDS, 10);
			final ArgonDiskMruCacheController dcc = ArgonDiskMruCacheController.newInstance(cfg);
			final Supplier supplier = new Supplier();
			supplier.put("A", 3000, LM03, EX08);
			supplier.put("B", 13000, LM03, EX08);
			supplier.put("C", 9000, LM03, EX08);
			final ExecutorService xc = Executors.newFixedThreadPool(2);
			fixNow("20131201T0600Z");
			final Future<File> fuA = xc.submit(new Agent(dcc, supplier, new Request("A"))); // MISS
			try {
				final File oFile = fuA.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found A", oFile);
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 1: A1
			Assert.assertTrue(probe.noPurgeAgenda());
			Assert.assertTrue(probe.noPurgeReclaim());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertTrue(probe.noAudit());
			Assert.assertEquals("A:H0M1", probe.statsReport());

			probe.allowPurgeReclaim();

			final Future<File> fuB = xc.submit(new Agent(dcc, supplier, new Request("B"))); // MISS
			try {
				final File oFile = fuB.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found B", oFile);
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 3: A1 B2
			Assert.assertTrue(probe.reachedPurgeAgeLimited());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertTrue(probe.noAudit());
			Assert.assertEquals("B:H0M1", probe.statsReport());

			System.out.print("Ageing for 10sec...");
			Thread.sleep(10000);
			System.out.println("Done");

			final Future<File> fuCv1 = xc.submit(new Agent(dcc, supplier, new Request("C"))); // MISS
			try {
				final File oFile = fuCv1.get(10, TimeUnit.SECONDS);
				Assert.assertNotNull("Found C", oFile);
			} catch (ExecutionException | TimeoutException ex) {
				Assert.fail(ex.getMessage());
			}// cache 5: A1 B2 C2 -> cache 2: C2

			Assert.assertTrue(probe.reachedPurgeAgenda());
			Assert.assertTrue(probe.reachedPurgeReclaim());
			Assert.assertTrue(probe.reachedCheckpoint());
			Assert.assertTrue(probe.noAudit());
			Assert.assertEquals("C:H0M1", probe.statsReport());

			dcc.cancel();
			final List<String> mgt = probe.mruManagementTranscript;
			Assert.assertEquals("newState.noCheckpoint", mgt.get(0));
			Assert.assertEquals("newState.initialise", mgt.get(1));
			Assert.assertEquals("purge.ageLimited", mgt.get(2));
			Assert.assertEquals("purge.agenda(StatePre{kB=40  agenda(0)=A  agenda(1)=B})", mgt.get(3));
			Assert.assertEquals("purge.reclaim(StatePost{kB=16})", mgt.get(4));

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

		public Agent(ArgonDiskMruCacheController dcc, Supplier s, Request rq) {
			this.dcc = dcc;
			this.s = s;
			this.rq = rq;
		}
		private final ArgonDiskMruCacheController dcc;
		private final Supplier s;
		private final Request rq;
	}

	private static class Cacheable implements IArgonDiskCacheable {

		@Override
		public Binary getContent() {
			return m_oContent;
		}

		@Override
		public Date getExpires() {
			return m_oExpires;
		}

		@Override
		public Date getLastModified() {
			return m_oLastModified;
		}

		@Override
		public Date getResponseAt() {
			return m_oResponseAt;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			if (m_oContent == null) {
				sb.append("no content");
			} else {
				sb.append("bytes=");
				sb.append(m_oContent.byteCount());
			}
			if (m_oResponseAt != null) {
				sb.append(", responseAt=");
				sb.append(DateFormatter.newYMDHMSFromTs(m_oResponseAt.getTime()));
			}
			if (m_oLastModified != null) {
				sb.append(", modified=");
				sb.append(DateFormatter.newYMDHMSFromTs(m_oLastModified.getTime()));
			}
			if (m_oExpires != null) {
				sb.append(", expires=");
				sb.append(DateFormatter.newYMDHMSFromTs(m_oExpires.getTime()));
			}
			return sb.toString();
		}

		public Cacheable(Binary oContent, Date oResponseAt, Date oLastModified, Date oExpires) {
			m_oContent = oContent;
			m_oResponseAt = oResponseAt;
			m_oLastModified = oLastModified;
			m_oExpires = oExpires;
		}
		private final Binary m_oContent;
		private final Date m_oResponseAt;
		private final Date m_oLastModified;
		private final Date m_oExpires;
	}

	private static class Probe implements IArgonDiskMruCacheProbe {

		public void allowPurgeReclaim()
				throws InterruptedException {
			if (!boxPurgeReclaimProceed.offer("PURGE", 60, TimeUnit.SECONDS)) {
				System.err.println("Could not enable mru reclaim within 60 secs");
			}
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
		public boolean isLiveManagement() {
			return true;
		}

		@Override
		public boolean isLiveRequest() {
			return true;
		}

		@Override
		public void liveManagement(String message, Object... args) {
			final String qargs = "(" + ArgonJoiner.zComma(args) + ")";
			if (show.get()) {
				System.out.println("pre mru." + message + qargs);
			}
			boolean showpost = false;
			try {
				if (message.equals("purge.agenda")) {
					mruManagementTranscript.add(message + qargs);
					boxPurgeAgendaReached.put("PURGE");
					if (boxPurgeReclaimProceed.poll(60, TimeUnit.SECONDS) == null) {
						System.err.println("Did not receive mru reclaim proceed within 60 secs");
					}
					showpost = true;
				} else if (message.equals("purge.reclaim")) {
					mruManagementTranscript.add(message + qargs);
					boxPurgeReclaimReached.put("PURGE");
					showpost = true;
				} else if (message.startsWith("purge.file.")) {
					mruManagementTranscript.add(message);
				} else if (message.equals("purge.ageLimited")) {
					mruManagementTranscript.add(message);
					boxPurgeAgeLimited.put("LIMITED");
				} else if (message.equals("audit.delete")) {
					mruManagementTranscript.add(message + qargs);
					boxAudit.put("AUDIT");
				} else if (message.equals("checkpoint.saved")) {
					if (concurrentMode.get()) {
						boxCheckpoint.offer("CHECKPOINT");
					} else {
						boxCheckpoint.put("CHECKPOINT");
					}
				} else {
					if (message.startsWith("newState.")) {
						mruManagementTranscript.add(message);
					}
				}
			} catch (final InterruptedException ex) {
				System.out.println("MruManagement Interrupted");
			}
			if (showpost && show.get()) {
				System.out.println("post mru." + message);
			}
		}

		@Override
		public void liveRequestHit(String qccResourceId) {
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
		public void liveRequestMiss(String qccResourceId) {
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

		public boolean noCheckpoint()
				throws InterruptedException {
			return boxCheckpoint.peek() == null;
		}

		public boolean noPurgeAgenda()
				throws InterruptedException {
			return boxPurgeAgendaReached.peek() == null;
		}

		public boolean noPurgeReclaim()
				throws InterruptedException {
			return boxPurgeReclaimReached.peek() == null;
		}

		public boolean reachedAudit()
				throws InterruptedException {
			return boxAudit.poll(7, TimeUnit.SECONDS) != null;
		}

		public boolean reachedCheckpoint()
				throws InterruptedException {
			return boxCheckpoint.poll(7, TimeUnit.SECONDS) != null;
		}

		public boolean reachedPurgeAgeLimited()
				throws InterruptedException {
			return boxPurgeAgeLimited.poll(27, TimeUnit.SECONDS) != null;
		}

		public boolean reachedPurgeAgenda()
				throws InterruptedException {
			return boxPurgeAgendaReached.poll(27, TimeUnit.SECONDS) != null;
		}

		public boolean reachedPurgeReclaim()
				throws InterruptedException {
			return boxPurgeReclaimReached.poll(27, TimeUnit.SECONDS) != null;
		}

		public void setConcurrentMode(boolean enabled) {
			concurrentMode.set(enabled);
		}

		public String statsReport() {
			m_lockHM.lock();
			try {
				final Set<String> hm = new HashSet<>();
				hm.addAll(m_hit.keySet());
				hm.addAll(m_miss.keySet());
				final List<String> keys = new ArrayList<String>(hm);
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
		final AtomicBoolean concurrentMode = new AtomicBoolean(false);
		final BlockingQueue<String> boxCheckpoint = new LinkedBlockingQueue<>(1);
		final BlockingQueue<String> boxPurgeAgendaReached = new LinkedBlockingQueue<>(1);
		final BlockingQueue<String> boxPurgeReclaimProceed = new LinkedBlockingQueue<>(1);
		final BlockingQueue<String> boxPurgeReclaimReached = new LinkedBlockingQueue<>(1);
		final BlockingQueue<String> boxPurgeAgeLimited = new LinkedBlockingQueue<>(1);
		final BlockingQueue<String> boxAudit = new LinkedBlockingQueue<>(1);
		final List<String> mruManagementTranscript = new ArrayList<>();
		private final Lock m_lockHM = new ReentrantLock();
		final Map<String, Integer> m_hit = new HashMap<String, Integer>();
		final Map<String, Integer> m_miss = new HashMap<String, Integer>();
	}

	private static class Request implements IArgonDiskCacheRequest {

		@Override
		public String qccResourceId() {
			return qccResourceId;
		}

		@Override
		public String toString() {
			return qccResourceId;
		}

		public Request(String qccResourceId) {
			this.qccResourceId = qccResourceId;
		}
		public final String qccResourceId;
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

	private static class Supplier implements IArgonDiskCacheSupplier<Request> {

		private IArgonDiskCacheable getImp(Request request, Date oLastModified)
				throws ArgonCacheException, InterruptedException {
			final Date rpAt = DateFactory.newDate(ArgonClock.tsNow());
			Thread.sleep(1000L);
			final SupplyItem oItem = m_map.get(request.qccResourceId);
			if (oItem == null) return null;
			final boolean modified = oLastModified == null || oLastModified.compareTo(oItem.getLastModified()) < 0;
			if (modified) return new Cacheable(oItem.createBinary(), rpAt, oItem.getLastModified(), oItem.getExpires());
			return new Cacheable(null, rpAt, oItem.getLastModified(), oItem.getExpires());
		}

		@Override
		public IArgonDiskCacheable getCacheable(Request request)
				throws ArgonCacheException, InterruptedException {
			return getImp(request, null);
		}

		@Override
		public IArgonDiskCacheable getCacheableConditional(Request request, Date lastModified)
				throws ArgonCacheException, InterruptedException {
			return getImp(request, lastModified);
		}

		public void put(String qccResourceId, int bcPayload, String ozLastModified, String ozExpires) {
			final SupplyItem item = new SupplyItem(bcPayload, ozLastModified, ozExpires);
			m_map.put(qccResourceId, item);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Supplier");
			ds.a("rid_item", m_map);
			return ds.s();
		}

		public Supplier() {
		}

		private final Map<String, SupplyItem> m_map = new HashMap<>();
	}

	private static class SupplyItem {

		public Binary createBinary() {
			if (m_bc < 0) return null;
			final byte[] payload = new byte[m_bc];
			for (int i = 0; i < m_bc; i++) {
				final int ib = 32 + (i % 64);
				payload[i] = (byte) ib;
			}
			return Binary.newFromTransient(payload);
		}

		public Date getExpires() {
			return m_oExpires;
		}

		public Date getLastModified() {
			return m_oLastModified;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("bytes=");
			sb.append(m_bc);
			if (m_oLastModified != null) {
				sb.append(", modified=");
				sb.append(DateFormatter.newYMDHMSFromTs(m_oLastModified.getTime()));
			}
			if (m_oExpires != null) {
				sb.append(", expires=");
				sb.append(DateFormatter.newYMDHMSFromTs(m_oExpires.getTime()));
			}
			return sb.toString();
		}

		public SupplyItem(int bc, String ozLastModified, String ozExpires) {
			m_bc = bc;
			final String oqtwLastModified = ArgonText.oqtw(ozLastModified);
			final String oqtwExpires = ArgonText.oqtw(ozExpires);
			m_oLastModified = oqtwLastModified == null ? null : DateFactory.newDateConstantFromTX(oqtwLastModified);
			m_oExpires = oqtwExpires == null ? null : DateFactory.newDateConstantFromTX(oqtwExpires);
		}
		private final int m_bc;
		private final Date m_oLastModified;
		private final Date m_oExpires;
	}
}
