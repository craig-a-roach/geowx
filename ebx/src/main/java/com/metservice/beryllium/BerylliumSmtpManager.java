/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonCompare;
import com.metservice.argon.ArgonText;
import com.metservice.argon.CArgon;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.Ds;
import com.metservice.argon.ElapsedFormatter;
import com.metservice.argon.HashCoder;
import com.metservice.argon.net.ArgonPlatform;

/**
 * @author roach
 */
public class BerylliumSmtpManager {

	private static final String ThreadPrefix = "beryllium-smtp";
	private static final long MinTimerPeriodMs = 1000L;
	private static final long MinTimerDelayMs = 1000L;
	private static final int InitCap = 8;
	private static final int EnvelopeCap = 256;
	private static final long TsNone = -1L;

	private static String address(String qtwHost, String oqtwUser) {
		assert qtwHost != null && qtwHost.length() > 0;
		final String qtwUser = oqtwUser == null ? ArgonPlatform.qcctwUserName() : oqtwUser;
		return qtwUser.toLowerCase() + "@" + qtwHost.toLowerCase();
	}

	public static Config newConfig(IBerylliumSmtpProbe probe, String ozccThreadSuffix) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		return new Config(probe, ozccThreadSuffix);
	}

	public static BerylliumSmtpManager newInstance(Config cfg) {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final long msTimerPeriod = Math.max(MinTimerPeriodMs, cfg.msTimerTick);
		final long msTimerDelay = Math.max(MinTimerDelayMs, cfg.msTimerDelay);
		final String qlcDefaultAddressFrom = qlcDefaultAddressFrom(cfg);
		final Timer timer = new Timer(cfg.qccThreadName, true);
		final BerylliumSmtpManager neo = new BerylliumSmtpManager(cfg, timer, qlcDefaultAddressFrom);
		final RunRateTask rateTask = new RunRateTask(neo);
		final Thread hook = new Thread(new RunShutdown(neo));
		Runtime.getRuntime().addShutdownHook(hook);
		timer.scheduleAtFixedRate(rateTask, msTimerDelay, msTimerPeriod);
		return neo;
	}

	public static String qlcDefaultAddressFrom(Config cfg) {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final String oqtwHost = ArgonText.oqtw(cfg.oztwDefaultFromHost);
		if (oqtwHost == null) return ArgonPlatform.qlcLocalEmailAddress();
		final String oqtwUser = ArgonText.oqtw(cfg.oztwDefaultFromUser);
		return address(oqtwHost, oqtwUser);
	}

	private void drain() {
		final long tsNow = ArgonClock.tsNow();
		final List<String> zlIdsAsc = m_pool.zlIdsAsc();
		for (final String qccId : zlIdsAsc) {
			final Tracker oTracker = m_monitor.findTracker(qccId);
			if (oTracker != null && oTracker.isThrottling()) {
				final BerylliumSmtpConnection oCx = m_pool.ensureConnection(qccId);
				if (oCx != null) {
					oTracker.tick(tsNow, oCx, true);
				}
			}
		}
	}

	void autoShutdown() {
		m_timer.cancel();
		drain();
		m_pool.closeAllConnections();
	}

	Pool pool() {
		return m_pool;
	}

	void tick(long tsNow) {
		final List<String> zlIdsAsc = m_pool.zlIdsAsc();
		for (final String qccId : zlIdsAsc) {
			final Tracker oTracker = m_monitor.findTracker(qccId);
			if (oTracker != null) {
				final BerylliumSmtpConnection oCx = m_pool.ensureConnection(qccId);
				if (oCx != null) {
					oTracker.tick(tsNow, oCx, false);
				}
			}
		}
	}

	public BerylliumSmtpConnection ensureConnection(String qccConnectionId) {
		return m_pool.ensureConnection(qccConnectionId);
	}

	public BerylliumSmtpConnection findConnection(String qccConnectionId) {
		return m_pool.findConnection(qccConnectionId);
	}

	public String qlcDefaultAddressFrom() {
		return m_qlcDefaultAddressFrom;
	}

	public void register(String qccConnectionId, BerylliumSmtpUrl url, String zcctwPassword, boolean secure) {
		m_pool.register(qccConnectionId, url, zcctwPassword, secure);
	}

	public String reportConnectionStatus() {
		return m_pool.toString();
	}

	public String reportConnectionStatus(String qccConnectionId) {
		return m_pool.reportConnectionStatus(qccConnectionId);
	}

	public void send(String qccConnectionId, BerylliumSmtpEnvelope envelope, IBerylliumSmtpContent content,
			BerylliumSmtpRatePolicy policy) {
		if (qccConnectionId == null || qccConnectionId.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (envelope == null) throw new IllegalArgumentException("object is null");
		if (content == null) throw new IllegalArgumentException("object is null");
		if (policy == null) throw new IllegalArgumentException("object is null");
		final long tsNow = ArgonClock.tsNow();
		final Tracker tracker = m_monitor.declareTracker(qccConnectionId);
		tracker.send(envelope, content, policy, tsNow);
	}

	public void shutdown() {
		autoShutdown();
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("pool", m_pool);
		ds.a("qlcDefaultAddressFrom", m_qlcDefaultAddressFrom);
		ds.a("monitor", m_monitor);
		return ds.s();
	}

	private BerylliumSmtpManager(Config cfg, Timer timer, String qlcDefaultAddressFrom) {
		assert cfg != null;
		assert timer != null;
		this.probe = cfg.probe;
		m_timer = timer;
		m_qlcDefaultAddressFrom = qlcDefaultAddressFrom;
		m_pool = new Pool();
		m_monitor = new Monitor();
	}
	final IBerylliumSmtpProbe probe;
	private final Timer m_timer;
	private final String m_qlcDefaultAddressFrom;
	private final Pool m_pool;
	private final Monitor m_monitor;

	private static class Credential {

		public boolean equals(BerylliumSmtpManager.Credential rhs) {
			if (rhs == this) return true;
			if (rhs == null) return false;
			return url.equals(rhs.url) && zcctwPassword.equals(rhs.zcctwPassword) && secure == rhs.secure;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o == null || !(o instanceof BerylliumSmtpManager.Credential)) return false;
			return equals((BerylliumSmtpManager.Credential) o);
		}

		@Override
		public int hashCode() {
			return HashCoder.fields3(url, zcctwPassword, secure);
		}

		@Override
		public String toString() {
			return url.credential(zcctwPassword) + (secure ? "  SECURE" : "");
		}

		public Credential(BerylliumSmtpUrl url, String zcctwPassword, boolean secure) {
			assert url != null;
			assert zcctwPassword != null;
			this.url = url;
			this.zcctwPassword = zcctwPassword;
			this.secure = secure;
		}
		public final BerylliumSmtpUrl url;
		public final String zcctwPassword;
		public final boolean secure;
	}

	private class Monitor {

		public Tracker declareTracker(String qccId) {
			if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
			m_rwlock.writeLock().lock();
			try {
				Tracker vTracker = m_mapId_Tracker.get(qccId);
				if (vTracker == null) {
					vTracker = new Tracker(qccId);
					m_mapId_Tracker.put(qccId, vTracker);
				}
				return vTracker;
			} finally {
				m_rwlock.writeLock().unlock();
			}
		}

		public Tracker findTracker(String qccId) {
			if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
			m_rwlock.readLock().lock();
			try {
				return m_mapId_Tracker.get(qccId);
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Monitor");
			ds.a("Id_Tracker", m_mapId_Tracker);
			return ds.s();
		}

		public Monitor() {
		}
		private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
		private final Map<String, Tracker> m_mapId_Tracker = new HashMap<String, Tracker>(InitCap);
	}

	private class Pool {

		private List<BerylliumSmtpConnection> zlConnectionsAscLk() {
			final List<BerylliumSmtpConnection> zlAsc = new ArrayList<BerylliumSmtpConnection>(m_mapUrl_Connection.values());
			Collections.sort(zlAsc);
			return zlAsc;
		}

		private List<String> zlIdsAscLk() {
			final List<String> zlAsc = new ArrayList<String>(m_mapId_Credential.keySet());
			Collections.sort(zlAsc);
			return zlAsc;
		}

		public void closeAllConnections() {
			m_rwlock.writeLock().lock();
			try {
				final List<BerylliumSmtpConnection> zlAsc = zlConnectionsAscLk();
				for (final BerylliumSmtpConnection cx : zlAsc) {
					cx.close(probe);
				}
				m_mapUrl_Connection.clear();
			} finally {
				m_rwlock.writeLock().unlock();
			}
		}

		public BerylliumSmtpConnection ensureConnection(String qccId) {
			if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
			m_rwlock.writeLock().lock();
			try {
				BerylliumSmtpConnection oCx = null;
				final Credential oExCr = m_mapId_Credential.get(qccId);
				if (oExCr != null) {
					final BerylliumSmtpConnection oExCx = m_mapUrl_Connection.get(oExCr.url);
					if (oExCx == null || !oExCx.isConnected()) {
						final BerylliumSmtpConnection oNeoCx = BerylliumSmtpConnectionFactory.createConnection(probe,
								oExCr.url, oExCr.zcctwPassword, oExCr.secure);
						if (oNeoCx != null) {
							m_mapUrl_Connection.put(oExCr.url, oNeoCx);
							oCx = oNeoCx;
						}
					} else {
						oCx = oExCx;
					}
				}
				return oCx;
			} finally {
				m_rwlock.writeLock().unlock();
			}
		}

		public BerylliumSmtpConnection findConnection(String qccId) {
			if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
			m_rwlock.readLock().lock();
			try {
				final Credential oExCr = m_mapId_Credential.get(qccId);
				return oExCr == null ? null : m_mapUrl_Connection.get(oExCr.url);
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		public void register(String qccId, BerylliumSmtpUrl url, String zcctwPassword, boolean secure) {
			if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
			if (url == null) throw new IllegalArgumentException("object is null");
			if (zcctwPassword == null) throw new IllegalArgumentException("object is null");
			final Credential neoCredential = new Credential(url, zcctwPassword, secure);
			m_rwlock.writeLock().lock();
			try {
				final Credential oExCr = m_mapId_Credential.get(qccId);
				final boolean delta = oExCr != null && !oExCr.equals(neoCredential);
				if (oExCr != null && delta) {
					final BerylliumSmtpConnection oExCx = m_mapUrl_Connection.remove(oExCr.url);
					if (oExCx != null) {
						oExCx.close(probe);
					}
					m_mapId_Credential.remove(qccId);
				}
				if (oExCr == null || delta) {
					m_mapId_Credential.put(qccId, neoCredential);
				}
			} finally {
				m_rwlock.writeLock().unlock();
			}
		}

		public String reportConnectionStatus(String qccId) {
			if (qccId == null || qccId.length() == 0) throw new IllegalArgumentException("string is null or empty");
			m_rwlock.readLock().lock();
			try {
				final Credential oExCr = m_mapId_Credential.get(qccId);
				if (oExCr == null) return "UNREGISTERED";
				final BerylliumSmtpConnection oCx = m_mapUrl_Connection.get(oExCr.url);
				return (oCx == null) ? (oExCr + " REGISTERED") : oCx.toString();
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Pool");
			ds.a("Id_Credential", m_mapId_Credential);
			ds.a("Url_Connection", m_mapUrl_Connection);
			return ds.s();
		}

		public List<String> zlIdsAsc() {
			m_rwlock.readLock().lock();
			try {
				return zlIdsAscLk();
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		public Pool() {
			m_mapId_Credential = new HashMap<String, Credential>(InitCap);
			m_mapUrl_Connection = new HashMap<BerylliumSmtpUrl, BerylliumSmtpConnection>(InitCap);
		}
		private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
		private final Map<String, Credential> m_mapId_Credential;
		private final Map<BerylliumSmtpUrl, BerylliumSmtpConnection> m_mapUrl_Connection;
	}// Pool

	private static class RunRateTask extends TimerTask {

		@Override
		public void run() {
			m_self.tick(ArgonClock.tsNow());
		}

		public RunRateTask(BerylliumSmtpManager self) {
			assert self != null;
			m_self = self;
		}
		private final BerylliumSmtpManager m_self;
	}// ExpireTask

	private static class RunShutdown implements Runnable {

		@Override
		public void run() {
			m_self.autoShutdown();
		}

		public RunShutdown(BerylliumSmtpManager self) {
			assert self != null;
			m_self = self;
		}
		private final BerylliumSmtpManager m_self;
	}// Shutdown

	private class Tracker {

		private TrackerBox declareBoxLk(BerylliumSmtpEnvelope envelope) {
			assert envelope != null;
			TrackerBox vBox = m_mapEnv_Tracker.get(envelope);
			if (vBox == null) {
				vBox = new TrackerBox(envelope);
				m_mapEnv_Tracker.put(envelope, vBox);
			}
			return vBox;
		}

		private BerylliumSmtpConnection ensureConnection() {
			return pool().ensureConnection(qccId);
		}

		private List<TrackerBox> zlBoxesAscLk() {
			final List<TrackerBox> zlAsc = new ArrayList<TrackerBox>(m_mapEnv_Tracker.values());
			Collections.sort(zlAsc);
			return zlAsc;
		}

		public boolean isThrottling() {
			m_rwlock.readLock().lock();
			try {
				final List<TrackerBox> zlBoxesAsc = zlBoxesAscLk();
				for (final TrackerBox trackerBox : zlBoxesAsc) {
					if (trackerBox.isThrottling()) return true;
				}
				return false;
			} finally {
				m_rwlock.readLock().unlock();
			}
		}

		public void send(BerylliumSmtpEnvelope envelope, IBerylliumSmtpContent content, BerylliumSmtpRatePolicy policy,
				long tsNow) {
			assert envelope != null;
			assert content != null;
			assert policy != null;
			m_rwlock.writeLock().lock();
			try {
				final TrackerBox vBox = declareBoxLk(envelope);
				vBox.add(content, policy, tsNow);
				final BerylliumSmtpConnection oCx = ensureConnection();
				if (oCx != null) {
					vBox.flush(tsNow, oCx);
				}
			} finally {
				m_rwlock.writeLock().unlock();
			}
		}

		public void tick(long tsNow, BerylliumSmtpConnection cx, boolean forced) {
			final List<BerylliumSmtpEnvelope> purgeList = new ArrayList<BerylliumSmtpEnvelope>();
			m_rwlock.readLock().lock();
			try {
				final List<TrackerBox> zlBoxesAsc = zlBoxesAscLk();
				for (final TrackerBox trackerBox : zlBoxesAsc) {
					trackerBox.tick(tsNow, cx, purgeList, forced);
				}
			} finally {
				m_rwlock.readLock().unlock();
			}
			if (!purgeList.isEmpty()) {
				m_rwlock.writeLock().lock();
				try {
					for (final BerylliumSmtpEnvelope envelope : purgeList) {
						m_mapEnv_Tracker.remove(envelope);
					}
				} finally {
					m_rwlock.writeLock().unlock();
				}
			}
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Tracker");
			ds.a("Env_Tracker", m_mapEnv_Tracker);
			return ds.s();
		}

		public Tracker(String qccId) {
			assert qccId != null && qccId.length() > 0;
			this.qccId = qccId;
			m_mapEnv_Tracker = new HashMap<BerylliumSmtpEnvelope, TrackerBox>(EnvelopeCap);
		}
		final String qccId;
		private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
		private final Map<BerylliumSmtpEnvelope, TrackerBox> m_mapEnv_Tracker;
	}// Tracker

	private class TrackerBox implements Comparable<TrackerBox> {

		private void autoAbateLk(long tsNow, BerylliumSmtpConnection oCx) {
			if (m_oFlushed == null || m_oPending != null || m_oPolicy == null || oCx == null) return;
			final long msMinSendInterval = m_oPolicy.msMinSendInterval();
			final long msMaxSendInterval = m_oPolicy.msMaxSendInterval();
			if (msMinSendInterval == msMaxSendInterval) {
				m_oFlushed = null;
			} else {
				final boolean abateDue;
				if (m_tsLastFlush == TsNone) {
					abateDue = false;
				} else {
					final long tsNotBefore = m_tsLastFlush + msMaxSendInterval;
					abateDue = tsNow >= tsNotBefore;
				}
				if (abateDue) {
					final BerylliumSmtpBorder oBorder = createAbateBorder();
					final boolean sent = oCx.send(probe, envelope, m_oFlushed, oBorder);
					if (sent) {
						m_oFlushed = null;
					}
				}
			}
		}

		private BerylliumSmtpBorder createAbateBorder() {
			final String ols = oDate(m_tsLastFlush);
			if (m_oPolicy == null || ols == null) return null;
			final List<String[]> head = new ArrayList<String[]>();
			pair(head, "Automatic abatement advisory:", "Last message with this subject and recipient list was sent at "
					+ ols);
			final String e = ElapsedFormatter.formatMixedUnits(m_oPolicy.msMaxSendInterval());
			pair(head, "Abatement messages sent after:", e);
			return BerylliumSmtpBorder.newInstance("Abated", head, null);
		}

		private BerylliumSmtpBorder createThrottlingBorder() {
			final String olm = oDate(m_tsLastModified);
			final String ols = oDate(m_tsLastFlush);
			if (m_oPolicy == null || olm == null || ols == null) return null;
			if (m_updateCount == 0) return null;
			final List<String[]> foot = new ArrayList<String[]>();
			pair(foot, "Message rate throttling:", "Elided " + m_updateCount
					+ " items with the same subject and recipient list; most recently at " + olm);
			pair(foot, "Last sent message at:", ols);
			final String e = ElapsedFormatter.formatMixedUnits(m_oPolicy.msMinSendInterval());
			pair(foot, "Max allowable rate:", "one message every " + e);
			return BerylliumSmtpBorder.newInstance(null, null, foot);
		}

		private void flushLk(long tsNow, BerylliumSmtpConnection oCx, boolean forced) {
			if (m_oPending == null || m_oPolicy == null || oCx == null) return;
			final boolean flushDue;
			if (forced) {
				flushDue = true;
			} else {
				if (m_tsLastFlush == TsNone) {
					flushDue = true;
				} else {
					final long msMinSendInterval = m_oPolicy.msMinSendInterval();
					final long tsNotBefore = m_tsLastFlush + msMinSendInterval;
					flushDue = tsNow >= tsNotBefore;
				}
			}
			if (flushDue) {
				final BerylliumSmtpBorder oBorder = createThrottlingBorder();
				final boolean sent = oCx.send(probe, envelope, m_oPending, oBorder);
				if (sent) {
					m_oFlushed = m_oPending;
					m_oPending = null;
					m_updateCount = 0;
					m_tsLastFlush = tsNow;
				}
			}
		}

		private boolean isPurgeableLk(long tsNow) {
			if (m_oPolicy == null) return true;
			if (m_oPending != null) return false;
			if (m_oFlushed != null) return false;
			final long msMaxSendInterval = m_oPolicy.msMaxSendInterval();
			final long tsNotBefore = m_tsLastFlush + msMaxSendInterval;
			return tsNow >= tsNotBefore;
		}

		private String oDate(long ts) {
			return ts == TsNone ? null : DateFormatter.newYMDHMSFromTs(ts);
		}

		private void pair(List<String[]> list, String label, String value) {
			list.add(new String[] { label, value });
		}

		public void add(IBerylliumSmtpContent content, BerylliumSmtpRatePolicy policy, long tsNow) {
			assert content != null;
			assert policy != null;
			m_lock.lock();
			try {
				m_tsLastModified = tsNow;
				if (m_oPending == null) {
					m_updateCount = 0;
				} else {
					m_updateCount++;
				}
				m_oPolicy = policy;
				m_oPending = content;
			} finally {
				m_lock.unlock();
			}
		}

		@Override
		public int compareTo(TrackerBox rhs) {
			return ArgonCompare.fwd(m_tsLastFlush, rhs.m_tsLastFlush);
		}

		public void flush(long tsNow, BerylliumSmtpConnection oCx) {
			m_lock.lock();
			try {
				flushLk(tsNow, oCx, false);
			} finally {
				m_lock.unlock();
			}
		}

		public boolean isThrottling() {
			m_lock.lock();
			try {
				return m_oPending != null && m_oPolicy != null;
			} finally {
				m_lock.unlock();
			}
		}

		public void tick(long tsNow, BerylliumSmtpConnection cx, List<BerylliumSmtpEnvelope> purgeList, boolean forced) {
			assert cx != null;
			assert purgeList != null;
			m_lock.lock();
			try {
				flushLk(tsNow, cx, forced);
				autoAbateLk(tsNow, cx);
				if (isPurgeableLk(tsNow)) {
					purgeList.add(envelope);
				}
			} finally {
				m_lock.unlock();
			}
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("TrackerBox");
			ds.a("lastModified", oDate(m_tsLastModified));
			ds.a("lastFlush", oDate(m_tsLastFlush));
			ds.a("updateCount", m_updateCount);
			ds.a("havePending", (m_oPending != null));
			ds.a("haveFlushed", (m_oFlushed != null));
			ds.a("havePolicy", (m_oPolicy != null));
			return ds.s();
		}

		public TrackerBox(BerylliumSmtpEnvelope envelope) {
			assert envelope != null;
			this.envelope = envelope;
			m_tsLastModified = TsNone;
			m_tsLastFlush = TsNone;
		}

		final BerylliumSmtpEnvelope envelope;
		private final Lock m_lock = new ReentrantLock();
		private long m_tsLastModified;
		private long m_tsLastFlush;
		private int m_updateCount;
		private IBerylliumSmtpContent m_oPending;
		private IBerylliumSmtpContent m_oFlushed;
		private BerylliumSmtpRatePolicy m_oPolicy;
	}

	public static class Config {

		@Override
		public String toString() {
			final Ds ds = Ds.o("BerylliumSmtpManager.Config");
			ds.a("msTimerTick", msTimerTick);
			ds.a("msTimerDelay", msTimerDelay);
			ds.a("threadName", qccThreadName);
			ds.a("defaultFromHost", oztwDefaultFromHost);
			ds.a("defaultFromUser", oztwDefaultFromUser);
			return ds.s();
		}

		Config(IBerylliumSmtpProbe probe, String ozccThreadSuffix) {
			assert probe != null;
			this.probe = probe;
			final String oqtwThreadSuffix = ArgonText.oqtw(ozccThreadSuffix);
			this.qccThreadName = ThreadPrefix + (oqtwThreadSuffix == null ? "" : ("-" + oqtwThreadSuffix));
		}
		public final IBerylliumSmtpProbe probe;
		public final String qccThreadName;
		public int msTimerTick = 30 * CArgon.SEC_TO_MS;
		public int msTimerDelay = 15 * CArgon.SEC_TO_MS;
		public String oztwDefaultFromHost;
		public String oztwDefaultFromUser;
	}// Config
}
