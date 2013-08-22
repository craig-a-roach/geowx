/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonJoiner;
import com.metservice.argon.ArgonNumber;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.ArgonSplitter;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;
import com.metservice.beryllium.BerylliumBinaryHttpPayload;
import com.metservice.beryllium.BerylliumHttpPlatformException;
import com.metservice.beryllium.CBeryllium;
import com.metservice.beryllium.TestImpMirrorProbe;

/**
 * @author roach
 */
public class TestUnit1Mirror {

	static final String WipSuffix = ".wip";
	static final int WipSuffixL = WipSuffix.length();

	private static void discover(Store a, Store b, String xa, String xb, int spCount, int dsec, boolean sysout)
			throws InterruptedException {
		final ArgonServiceId sid = new ArgonServiceId("unittest.metservice", "berylliumMirror");
		final ExecutorService xc = Executors.newCachedThreadPool();
		if (sysout) {
			System.out.println(a);
			System.out.println(b);
		}

		final TestImpMirrorProbe probeA = new TestImpMirrorProbe("A", sysout, false, true);
		final TestImpMirrorProbe probeB = new TestImpMirrorProbe("B", sysout, false, true);
		final Provider providerA = new Provider(a, spCount, 0, 0);
		final Provider providerB = new Provider(b, spCount, 0, 0);
		final BerylliumMirror.Config srccfgA = new BerylliumMirror.Config(sid, 9970, "localhost", 9971);
		srccfgA.discoverIntervalMs = dsec * CArgon.SEC_TO_MS;
		final BerylliumMirror.Config srccfgB = new BerylliumMirror.Config(sid, 9971, "localhost", 9970);
		srccfgB.discoverIntervalMs = dsec * CArgon.SEC_TO_MS;
		final BerylliumMirror bmA = BerylliumMirror.newInstance(probeA, providerA, srccfgA);
		final BerylliumMirror bmB = BerylliumMirror.newInstance(probeB, providerB, srccfgB);

		final int syncWaitSecs = dsec * spCount + 10;
		try {
			bmA.start(xc);
			bmB.start(xc);
			Assert.assertTrue("Provider A sync " + spCount, providerA.awaitSyncPointQuota(syncWaitSecs));
			Assert.assertTrue("Provider B sync" + spCount, providerB.awaitSyncPointQuota(syncWaitSecs));
		} catch (final BerylliumHttpPlatformException ex) {
			Assert.fail(ex.getMessage());
		} finally {
			probeB.notifyShutdown();
			probeA.notifyShutdown();
			bmB.shutdown();
			bmA.shutdown();
		}
		Assert.assertEquals("Transcript A", xa, providerA.transcript());
		Assert.assertEquals("Transcript B", xb, providerB.transcript());
	}

	static String committedPath(String qccWipPath) {
		assert qccWipPath != null && qccWipPath.length() > 0;
		final int neoL = qccWipPath.length() - WipSuffixL;
		if (neoL > 0 && qccWipPath.endsWith(WipSuffix)) return qccWipPath.substring(0, neoL);
		return qccWipPath;
	}

	static boolean isWip(String qccPath) {
		assert qccPath != null && qccPath.length() > 0;
		return qccPath.endsWith(WipSuffix);
	}

	static BerylliumBinaryHttpPayload newPayload(Binary content, long tsLastModified) {
		return new BerylliumBinaryHttpPayload(CBeryllium.JsonContentType, content, tsLastModified);
	}

	static BerylliumBinaryHttpPayload newPayload(int serial) {
		final String mm = ArgonNumber.intToDec2(serial % 60);
		final String t7 = "20120615T03" + mm + "Z30";
		final Date lastModified = DateFactory.newDateConstantFromTX(t7);
		final Binary content = Binary.newFromStringASCII("{mm='" + mm + "'}");
		final long tsLastModified = lastModified.getTime();
		return new BerylliumBinaryHttpPayload(CBeryllium.JsonContentType, content, tsLastModified);
	}

	static String path(int serial, String ext, boolean isWip) {
		final String zSuffix = isWip ? WipSuffix : "";
		final String path = ArgonNumber.intToDec2(serial) + ext + zSuffix;
		return path;
	}

	static Integer serial(String qccPath) {
		assert qccPath != null && qccPath.length() > 0;
		final String[] zptParts = ArgonSplitter.zptqtwSplit(qccPath, '.');
		try {
			if (zptParts.length >= 1) return new Integer(zptParts[0]);
		} catch (final NumberFormatException ex) {
		}
		System.err.println("Cannot get serial from " + qccPath);
		return null;
	}

	@Test
	public void t10_steady()
			throws InterruptedException {
		final boolean sysout = true;
		final int spCount = 1;
		final int dsec = 999; // Disabled to ensure push testing
		final String ext = ".j.txt";

		final String xa = "onSYNC, onSAVE 08.j.txt.wip, onCOMMIT 08.j.txt.wip, onSYNC";
		final String xb = "SAVE 07.j.txt, onSYNC, SAVE 08.j.txt.wip, COMMIT 08.j.txt.wip";

		final Store a = Store.newInstance("A", ext, 5, 7);
		final Store b = Store.newInstance("B", ext, 5, 6);
		final ArgonServiceId sid = new ArgonServiceId("unittest.metservice", "berylliumMirror");
		final ExecutorService xc = Executors.newCachedThreadPool();
		final TestImpMirrorProbe probeA = new TestImpMirrorProbe("A", sysout, false, true);
		final TestImpMirrorProbe probeB = new TestImpMirrorProbe("B", sysout, false, true);
		final Provider providerA = new Provider(a, spCount, 1, 1);
		final Provider providerB = new Provider(b, spCount, 0, 0);
		final BerylliumMirror.Config srccfgA = new BerylliumMirror.Config(sid, 9970, "localhost", 9971);
		srccfgA.discoverIntervalMs = dsec * CArgon.SEC_TO_MS;
		final BerylliumMirror.Config srccfgB = new BerylliumMirror.Config(sid, 9971, "localhost", 9970);
		srccfgB.discoverIntervalMs = dsec * CArgon.SEC_TO_MS;
		final BerylliumMirror bmA = BerylliumMirror.newInstance(probeA, providerA, srccfgA);
		final BerylliumMirror bmB = BerylliumMirror.newInstance(probeB, providerB, srccfgB);

		final int syncWaitSecs = dsec * spCount + 10;
		final int saveWaitSecs = 8;
		final int commitWaitSecs = 8;
		try {
			bmA.start(xc);
			bmB.start(xc);
			Assert.assertTrue("Provider A sync " + spCount, providerA.awaitSyncPointQuota(syncWaitSecs));
			Assert.assertTrue("Provider B sync" + spCount, providerB.awaitSyncPointQuota(syncWaitSecs));
			final BerylliumBinaryHttpPayload payA8 = a.directSave(8, ext, true);
			bmA.push(new SaveTask(8, ext, true));
			Assert.assertTrue("Provider A save confirmed", providerA.awaitSaveQuota(saveWaitSecs));
			final BerylliumBinaryHttpPayload oPayB8 = b.find(8, ext, true);
			Assert.assertNotNull("Found B8", oPayB8);
			Assert.assertEquals("A8 time = B8 time", payA8.tsLastModified(), oPayB8.tsLastModified());
			a.directCommit(8, ext);
			bmA.push(new CommitTask(8, ext));
			Assert.assertTrue("Provider A commit confirmed", providerA.awaitCommitQuota(commitWaitSecs));
		} catch (final BerylliumHttpPlatformException ex) {
			Assert.fail(ex.getMessage());
		} finally {
			probeB.notifyShutdown();
			probeA.notifyShutdown();
			bmB.shutdown();
			bmA.shutdown();
		}
		Assert.assertEquals("Transcript A", xa, providerA.transcript());
		Assert.assertEquals("Transcript B", xb, providerB.transcript());
	}

	@Test
	public void t20_discover()
			throws InterruptedException {
		final Store A = Store.newInstance("A", ".j.txt", 5, 7);
		final Store B = Store.newInstance("B", ".j.txt", 2, 4, 4);
		final String ExA = "onSYNC, onSYNC, onSYNC";
		final String ExB = "SAVE 05.j.txt, SAVE 06.j.txt, SAVE 07.j.txt, onSYNC, onSYNC, onSYNC";
		final boolean sysout = true;
		final int SyncPointCount = 3;
		final int DiscoverySecs = 7;
		discover(A, B, ExA, ExB, SyncPointCount, DiscoverySecs, sysout);
	}

	@Test
	public void t22_discover()
			throws InterruptedException {
		final Store A = Store.newInstance("A", ".j.txt", 5, 7);
		final Store B = Store.newInstance("B", ".j.txt", 5, 7, 5, 6, 7);
		final String ExA = "onSYNC, onSYNC, onSYNC";
		final String ExB = "COMMIT 05.j.txt.wip, COMMIT 06.j.txt.wip, COMMIT 07.j.txt.wip, onSYNC, onSYNC, onSYNC";
		final boolean sysout = true;
		final int SyncPointCount = 3;
		final int DiscoverySecs = 7;
		discover(A, B, ExA, ExB, SyncPointCount, DiscoverySecs, sysout);
	}

	@Test
	public void t24_discover()
			throws InterruptedException {
		final Store A = Store.newInstance("A", ".j.txt", 3, 5, 5);
		final Store B = Store.newInstance("B", ".j.txt", 2, 4, 4);
		final String ExA = "onSYNC, onSYNC, onSYNC";
		final String ExB = "COMMIT 04.j.txt.wip, SAVE 05.j.txt.wip, onSYNC, onSYNC, onSYNC";
		final boolean sysout = true;
		final int SyncPointCount = 3;
		final int DiscoverySecs = 7;
		discover(A, B, ExA, ExB, SyncPointCount, DiscoverySecs, sysout);
	}

	private static class CommitTask implements IBerylliumMirrorCommitTask {

		@Override
		public String qccPath() {
			return qccPath;
		}

		@Override
		public String toString() {
			return qccPath;
		}

		public CommitTask(int serial, String ext) {
			this.qccPath = path(serial, ext, true);
		}
		public final String qccPath;
	}

	private static class Provider implements IBerylliumMirrorProvider {

		private void addTranscript(String t) {
			assert t != null && t.length() > 0;
			m_lock.lock();
			try {
				m_transcript.add(t);
			} finally {
				m_lock.unlock();
			}
		}

		public boolean awaitCommitQuota(int secs)
				throws InterruptedException {
			return m_latchCommit.await(secs, TimeUnit.SECONDS);
		}

		public boolean awaitSaveQuota(int secs)
				throws InterruptedException {
			return m_latchSave.await(secs, TimeUnit.SECONDS);
		}

		public boolean awaitSyncPointQuota(int secs)
				throws InterruptedException {
			return m_latchSyncPoint.await(secs, TimeUnit.SECONDS);
		}

		@Override
		public void commit(String qccWipPath) {
			m_store.commit(qccWipPath);
			addTranscript("COMMIT " + qccWipPath);
		}

		@Override
		public List<String> commitPathsAsc(List<String> zlWipPathsAsc) {
			return m_store.commitPathsAsc(zlWipPathsAsc);
		}

		@Override
		public BerylliumBinaryHttpPayload createPayload(String qccPath) {
			return m_store.createPayload(qccPath);
		}

		@Override
		public List<String> discoverDemandPathsAsc(String qcctwFromPath) {
			return m_store.discoverDemandPathsAsc(qcctwFromPath);
		}

		@Override
		public String discoverHiexPath() {
			return m_store.discoverHiexPath();
		}

		@Override
		public List<String> discoverWipPathsAsc() {
			return m_store.discoverWipPathsAsc();
		}

		@Override
		public void onCommitComplete(IBerylliumMirrorCommitTask task) {
			m_latchCommit.countDown();
			addTranscript("onCOMMIT " + task.qccPath());
		}

		@Override
		public void onHttpRetry() {
			m_httpRetryCount.incrementAndGet();
		}

		@Override
		public void onSaveComplete(IBerylliumMirrorSaveTask task) {
			m_latchSave.countDown();
			addTranscript("onSAVE " + task.qccPath());
		}

		@Override
		public void onSynchronizationPoint() {
			m_latchSyncPoint.countDown();
			addTranscript("onSYNC");
		}

		@Override
		public void save(String qccPath, Binary content, long tsLastModified) {
			m_store.save(qccPath, content, tsLastModified);
			addTranscript("SAVE " + qccPath);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Provider");
			ds.a("id", m_store.id);
			ds.a("transcript", m_transcript);
			ds.a("httpRetryCount", m_httpRetryCount);
			return ds.s();
		}

		public String transcript() {
			m_lock.lock();
			try {
				return ArgonJoiner.zComma(m_transcript);
			} finally {
				m_lock.unlock();
			}
		}

		public Provider(Store store, int syncPoints, int saves, int commits) {
			if (store == null) throw new IllegalArgumentException("object is null");
			m_store = store;
			m_latchSyncPoint = new CountDownLatch(syncPoints);
			m_latchSave = new CountDownLatch(saves);
			m_latchCommit = new CountDownLatch(commits);
		}
		private final Store m_store;
		private final CountDownLatch m_latchSyncPoint;
		private final CountDownLatch m_latchSave;
		private final CountDownLatch m_latchCommit;
		private final AtomicInteger m_httpRetryCount = new AtomicInteger();
		private final Lock m_lock = new ReentrantLock();
		private final List<String> m_transcript = new ArrayList<String>();
	}

	private static class SaveTask implements IBerylliumMirrorSaveTask {

		@Override
		public BerylliumBinaryHttpPayload createPayload(IArgonFileProbe probe) {
			return newPayload(serial);
		}

		@Override
		public String qccPath() {
			return qccPath;
		}

		@Override
		public String toString() {
			return qccPath;
		}

		public SaveTask(int serial, String ext, boolean isWip) {
			this.serial = serial;
			this.qccPath = path(serial, ext, isWip);
		}
		public final int serial;
		public final String qccPath;
	}

	private static class Store {

		public static Store newInstance(String id, String ext, int serialStart, int serialLast, Integer... serialWips) {
			if (id == null || id.length() == 0) throw new IllegalArgumentException("string is null or empty");
			if (serialWips == null) throw new IllegalArgumentException("object is null");
			final Map<String, BerylliumBinaryHttpPayload> map = new HashMap<String, BerylliumBinaryHttpPayload>();
			final Set<String> zsWipPaths = new HashSet<String>();
			for (int iw = 0; iw < serialWips.length; iw++) {
				final int wipSerial = serialWips[iw].intValue();
				final String qccPath = path(wipSerial, ext, true);
				zsWipPaths.add(qccPath);
			}
			for (int serial = serialStart; serial <= serialLast; serial++) {
				final String wipPath = path(serial, ext, true);
				final boolean isWip = zsWipPaths.contains(wipPath);
				final String qccPath = path(serial, ext, isWip);
				map.put(qccPath, newPayload(serial));
			}
			final int nextSerial = serialLast + 1;
			return new Store(id, map, nextSerial, zsWipPaths);
		}

		public void commit(String qccWipPath) {
			if (qccWipPath == null || qccWipPath.length() == 0)
				throw new IllegalArgumentException("string is null or empty");
			m_rwlock.writeLock().lock();
			try {
				final String commitedPath = committedPath(qccWipPath);
				final Integer oSerial = serial(commitedPath);
				if (oSerial == null) return;
				final BerylliumBinaryHttpPayload oPayload = m_mapPath_Payload.remove(qccWipPath);
				if (oPayload != null) {
					m_mapPath_Payload.put(commitedPath, oPayload);
				}
				m_zsWipPath.remove(qccWipPath);
			} finally {
				m_rwlock.writeLock().unlock();
			}
		}

		public List<String> commitPathsAsc(List<String> zlWipPathsAsc) {
			if (zlWipPathsAsc == null) throw new IllegalArgumentException("object is null");
			m_rwlock.readLock().lock();
			try {
				final List<String> zl = new ArrayList<String>();
				for (final String qccWipPath : zlWipPathsAsc) {
					final String commitedPath = committedPath(qccWipPath);
					if (m_mapPath_Payload.containsKey(commitedPath)) {
						zl.add(qccWipPath);
					}
				}
				return zl;
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		public BerylliumBinaryHttpPayload createPayload(String qccPath) {
			m_rwlock.readLock().lock();
			try {
				return m_mapPath_Payload.get(qccPath);
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		public void directCommit(int serial, String ext) {
			commit(path(serial, ext, true));
		}

		public BerylliumBinaryHttpPayload directSave(int serial, String ext, boolean isWip) {
			final String qccPath = path(serial, ext, isWip);
			final BerylliumBinaryHttpPayload payload = newPayload(serial);
			m_rwlock.writeLock().lock();
			try {
				m_mapPath_Payload.put(qccPath, payload);
			} finally {
				m_rwlock.writeLock().unlock();
			}
			return payload;
		}

		public List<String> discoverDemandPathsAsc(String qcctwFromPath) {
			m_rwlock.readLock().lock();
			try {
				final List<String> zl = new ArrayList<String>();
				for (final String path : m_mapPath_Payload.keySet()) {
					final int cmp = path.compareTo(qcctwFromPath);
					if (cmp >= 0) {
						zl.add(path);
					}
				}
				Collections.sort(zl);
				return zl;
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		public String discoverHiexPath() {
			return ArgonNumber.intToDec2(m_nextSerial);
		}

		public List<String> discoverWipPathsAsc() {
			m_rwlock.readLock().lock();
			try {
				final List<String> zl = new ArrayList<String>(m_zsWipPath);
				Collections.sort(zl);
				return zl;
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		public BerylliumBinaryHttpPayload find(int serial, String ext, boolean isWip) {
			final String qccPath = path(serial, ext, isWip);
			m_rwlock.readLock().lock();
			try {
				return m_mapPath_Payload.get(qccPath);
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		public void save(String qccPath, Binary content, long tsLastModified) {
			m_rwlock.writeLock().lock();
			try {
				final Integer oSerial = serial(qccPath);
				if (oSerial != null) {
					m_mapPath_Payload.put(qccPath, newPayload(content, tsLastModified));
					final int neoNextValue = oSerial.intValue() + 1;
					m_nextSerial = Math.max(m_nextSerial, neoNextValue);
					final boolean isWip = isWip(qccPath);
					if (isWip) {
						m_zsWipPath.add(qccPath);
					}
				}
			} finally {
				m_rwlock.writeLock().unlock();
			}
		}

		@Override
		public String toString() {
			final List<String> pathsAsc = new ArrayList<String>(m_mapPath_Payload.keySet());
			Collections.sort(pathsAsc);
			return id + ":" + ArgonJoiner.zComma(pathsAsc);
		}

		private Store(String id, Map<String, BerylliumBinaryHttpPayload> map, int nextSerial, Set<String> zsWipPath) {
			assert id != null && id.length() > 0;
			assert map != null;
			assert zsWipPath != null;
			this.id = id;
			m_mapPath_Payload = map;
			m_nextSerial = nextSerial;
			m_zsWipPath = zsWipPath;
		}
		public final String id;
		private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
		private final Map<String, BerylliumBinaryHttpPayload> m_mapPath_Payload;
		private int m_nextSerial;
		private final Set<String> m_zsWipPath;
	}

}
