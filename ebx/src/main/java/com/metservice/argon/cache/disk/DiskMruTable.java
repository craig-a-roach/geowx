/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.cache.disk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Ds;
import com.metservice.argon.cache.ArgonCacheException;
import com.metservice.argon.file.ArgonFileManagement;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonDecoder;
import com.metservice.argon.json.JsonEncoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
class DiskMruTable {

	static final String p_fileName = "fn";
	static final String p_lastAccess = "la";
	static final String p_lastModified = "lm";
	static final String p_expires = "ex";
	static final String p_dcu = "u";
	static final String p_trackers = "trackers";

	public static final String CheckpointFileName = "checkpoint.json";
	public static final int CheckpointQuotaBc = 1024 * CArgon.M;

	public static final int WakePctLo = 51;
	public static final int WakePctHi = 99;
	public static final int SizeLimitLo = 1 * CArgon.K;
	public static final int PopLimitLo = 1;
	public static final int PopLimitHi = 128 * CArgon.K;
	public static final long MinLifeMsLo = 3000;

	private static final String TryDecodeCp = "Decode MRU Checkpoint";
	private static final String CsqScrub = "Wipe contents of cache";
	private static final String TryTick = "Perform MRU table resource management functions";
	private static final String CsqRetry = "Abandon this attempt then retry at scheduled frequency";
	private static final String[] NONAMES = new String[0];
	private static final int NOAUDIT = Integer.MAX_VALUE;
	private static final long TSNIL = 0L;

	private static State newState(Cfg cfg, int cap) {
		assert cfg != null;
		final boolean trace = cfg.probe.isLiveManagement();
		final File srcFile = new File(cfg.cndir, CheckpointFileName);
		final Binary oBinary = Binary.createFromFile(cfg.probe, srcFile, CheckpointQuotaBc);
		State oState = null;
		try {
			if (oBinary == null) {
				if (trace) {
					cfg.probe.liveManagement("newState.noCheckpoint", srcFile);
				}
			} else {
				final JsonObject src = JsonDecoder.Default.decodeObject(oBinary.newStringUTF8());
				oState = new State(cfg, src);
			}
		} catch (final JsonSchemaException ex) {
			final Ds ds = Ds.triedTo(TryDecodeCp, ex, CsqScrub);
			cfg.probe.warnFile(ds, srcFile);
		} catch (final ArgonFormatException ex) {
			final Ds ds = Ds.triedTo(TryDecodeCp, ex, CsqScrub);
			cfg.probe.warnFile(ds, srcFile);
		}
		if (oState == null) {
			if (trace) {
				cfg.probe.liveManagement("newState.initialise");
			}
			return new State(cfg, cap);
		}
		if (trace) {
			cfg.probe.liveManagement("newState.loadedCheckpoint");
		}
		return oState;
	}

	public static DiskMruTable newInstance(ArgonDiskMruCacheController.Config cfg) {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final long cbcLimit = Math.max(SizeLimitLo, cfg.sizeLimitBytes);
		final int cpopLimit = Math.max(PopLimitLo, Math.min(PopLimitHi, cfg.populationLimit));
		final int cpctWake = Math.max(WakePctLo, Math.min(WakePctHi, cfg.purgeWakePct));
		final int cpctGoalHi = cpctWake - 1;
		final int cpctGoal = Math.max(WakePctLo - 1, Math.min(cpctGoalHi, cfg.purgeGoalPct));
		final long kbWake = (cbcLimit * cpctWake) / 100L / CArgon.K;
		final long kbGoal = (cbcLimit * cpctGoal) / 100L / CArgon.K;
		final int popWake = (cpopLimit * cpctWake) / 100;
		final int popGoal = (cpopLimit * cpctGoal) / 100;
		final int cauditCycle = cfg.auditCycle <= 0 ? NOAUDIT : cfg.auditCycle;
		final long msLife = Math.max(MinLifeMsLo, cfg.minLifeMs);
		final Cfg mru = new Cfg(cfg.probe, cfg.cndir, kbWake, popWake, kbGoal, popGoal, cauditCycle, msLife);
		final State state = newState(mru, cpopLimit);
		return new DiskMruTable(mru, state);
	}

	private void audit() {
		final String[] zptDiskFileNamesAsc = zptDiskFileNamesAsc();
		List<String> zlUnreferencedAsc = Collections.emptyList();
		if (zptDiskFileNamesAsc.length > 0) {
			m_lockState.lock();
			try {
				zlUnreferencedAsc = m_state.zlUnreferencedAsc(zptDiskFileNamesAsc);
			} finally {
				m_lockState.unlock();
			}
		}
		final int unrefCount = zlUnreferencedAsc.size();
		for (int i = 0; i < unrefCount; i++) {
			final String qccFileName = zlUnreferencedAsc.get(i);
			m_lockState.lock();
			try {
				m_state.reclaimUnreferenced(qccFileName);
			} finally {
				m_lockState.unlock();
			}
		}
		if (cfg.probe.isLiveManagement()) {
			if (zlUnreferencedAsc.isEmpty()) {
				cfg.probe.liveManagement("audit.clean");
			} else {
				final Ds ds = Ds.report("State");
				ds.a("unreferenced", zlUnreferencedAsc);
				cfg.probe.liveManagement("audit.delete", ds);
			}
		}
	}

	private void checkpoint() {
		final boolean trace = cfg.probe.isLiveManagement();
		final JsonObject cp = newCheckpointJson();
		if (trace) {
			cfg.probe.liveManagement("checkpoint.snapshot", cp);
		}
		final Binary out = Binary.newFromStringUTF8(JsonEncoder.Default.encode(cp));
		final File destFile = new File(cfg.cndir, CheckpointFileName);
		final long tsStart = System.currentTimeMillis();
		out.save(cfg.probe, destFile, false);
		if (trace) {
			final Ds ds = Ds.report("State");
			ds.a("path", destFile);
			ds.a("byteCount", out.byteCount());
			ds.a("ms", (System.currentTimeMillis() - tsStart));
			cfg.probe.liveManagement("checkpoint.saved", ds);
		}
	}

	private JsonObject newCheckpointJson() {
		m_lockState.lock();
		try {
			return m_state.newJsonCheckpoint();
		} finally {
			m_lockState.unlock();
		}
	}

	private List<String> newPurgeAgenda(long tsNow) {
		m_lockState.lock();
		try {
			final boolean isRequired = m_state.isPurgeRequired();
			if (isRequired) return m_state.newPurgeFileNamesAscAge(tsNow);
			return Collections.emptyList();
		} finally {
			m_lockState.unlock();
		}
	}

	private void purge(List<String> zlAgenda) {
		assert zlAgenda != null;
		final int agendaCount = zlAgenda.size();
		for (int i = 0; i < agendaCount; i++) {
			final String qccFileName = zlAgenda.get(i);
			m_lockState.lock();
			try {
				m_state.purge(qccFileName);
			} finally {
				m_lockState.unlock();
			}
		}
	}

	private void purge(long tsNow) {
		final boolean trace = cfg.probe.isLiveManagement();
		final List<String> zlAgenda = newPurgeAgenda(tsNow);
		final long kbPre = m_state.kbActual();
		if (zlAgenda.isEmpty()) {
			if (trace) {
				final Ds ds = Ds.report("State");
				ds.a("kB", kbPre);
				cfg.probe.liveManagement("purge.nil", ds);
			}
		} else {
			if (trace) {
				final Ds ds = Ds.report("StatePre");
				ds.a("kB", kbPre);
				ds.a("agenda", zlAgenda);
				cfg.probe.liveManagement("purge.agenda", ds);
			}
			purge(zlAgenda);
			m_checkpointDue.set(true);
			if (trace) {
				final long kbPost = m_state.kbActual();
				final Ds ds = Ds.report("StatePost");
				ds.a("kB", kbPost);
				cfg.probe.liveManagement("purge.reclaim", ds);
			}
		}
	}

	private String[] zptDiskFileNamesAsc() {
		final String[] ozpt = cfg.cndir.list();
		if (ozpt == null || ozpt.length == 0) return NONAMES;
		Arrays.sort(ozpt);
		return ozpt;
	}

	public Descriptor findDescriptor(String qccFileName, long tsNow) {
		if (qccFileName == null || qccFileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_lockState.lock();
		try {
			m_checkpointDue.set(true);
			final Tracker oTracker = m_state.findTracker(qccFileName, tsNow);
			return (oTracker == null) ? null : oTracker.newDescriptor();
		} finally {
			m_lockState.unlock();
		}
	}

	public Descriptor newDescriptor(String qccFileName, Dcu dcu, Date oLastModified, Date oExpires, long tsNow)
			throws ArgonCacheException {
		if (qccFileName == null || qccFileName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (dcu == null) throw new IllegalArgumentException("object is null");
		m_lockState.lock();
		try {
			m_checkpointDue.set(true);
			if (cfg.auditCycle != NOAUDIT) {
				m_auditCounter.incrementAndGet();
			}
			final long kbEx = m_state.kbActual();
			final Tracker tracker = m_state.putTracker(qccFileName, dcu, oLastModified, oExpires, tsNow);
			final long kbDelta = m_state.kbActual() - kbEx;
			if (kbDelta > 0L) {
				m_purgeDue.set(true);
			}
			return tracker.newDescriptor();
		} finally {
			m_lockState.unlock();
		}
	}

	public SensorSnapshot newSensorSnapshot() {
		m_lockState.lock();
		try {
			return m_state.newSensorSnapshot();
		} finally {
			m_lockState.unlock();
		}
	}

	public void tick(long tsNow) {
		try {
			if (m_purgeDue.getAndSet(false)) {
				purge(tsNow);
			}
			if (m_checkpointDue.getAndSet(false)) {
				checkpoint();
			}
			if (m_auditCounter.compareAndSet(cfg.auditCycle, 0)) {
				audit();
			}
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo(TryTick, ex, CsqRetry);
			ds.a("purgeDue", m_purgeDue);
			ds.a("checkpointDue", m_checkpointDue);
			ds.a("auditCounter", m_auditCounter);
			cfg.probe.failSoftware(ds);
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o("DiskMruTable");
		ds.a("purgeDue", m_purgeDue.get());
		ds.a("checkpointDue", m_checkpointDue.get());
		ds.a("auditCounter", m_auditCounter.get());
		ds.a("state", m_state);
		return ds.s();
	}

	public DiskMruTable(Cfg cfg, State state) {
		assert cfg != null;
		assert state != null;
		this.cfg = cfg;
		m_state = state;
	}
	private final Cfg cfg;
	private final AtomicBoolean m_purgeDue = new AtomicBoolean();
	private final AtomicBoolean m_checkpointDue = new AtomicBoolean();
	private final AtomicInteger m_auditCounter = new AtomicInteger();
	private final Lock m_lockState = new ReentrantLock();
	private final State m_state;

	private static class Cfg {

		Cfg(IArgonDiskMruCacheProbe probe, File cndir, long kbWake, int popWake, long kbGoal, int popGoal, int auditCycle,
				long msLife) {
			assert probe != null;
			assert cndir != null;
			this.probe = probe;
			this.cndir = cndir;
			this.kbWake = kbWake;
			this.popWake = popWake;
			this.kbGoal = kbGoal;
			this.popGoal = popGoal;
			this.auditCycle = auditCycle;
			this.msLife = msLife;
		}
		final IArgonDiskMruCacheProbe probe;
		final File cndir;
		final long kbWake;
		final int popWake;
		final long kbGoal;
		final int popGoal;
		final int auditCycle;
		final long msLife;
	}

	private static class State {

		private File newFile(String qccFileName) {
			return new File(cfg.cndir, qccFileName);
		}

		private List<String> newPurgeFileNamesAscAge(long tsNow) {
			final boolean trace = cfg.probe.isLiveManagement();
			final int popCacheFileActual = m_mapFileName_Tracker.size();
			final int popReclaim = popCacheFileActual - cfg.popGoal;
			final long kbReclaim = m_kbActual - cfg.kbGoal;
			if (popReclaim <= 0 && kbReclaim <= 0) return Collections.emptyList();
			final List<String> zlNames = new ArrayList<>(Math.max(64, popReclaim));
			final List<Tracker> zlTrackersAsc = new ArrayList<>(m_mapFileName_Tracker.values());
			Collections.sort(zlTrackersAsc);
			final int trackerCount = zlTrackersAsc.size();
			long kbNeo = m_kbActual;
			int popNeo = popCacheFileActual;
			boolean ageLimited = false;
			for (int i = 0; i < trackerCount && !ageLimited && (kbNeo > cfg.kbGoal || popNeo > cfg.popGoal); i++) {
				final Tracker tracker = zlTrackersAsc.get(i);
				final long msAge = tsNow - tracker.tsLastAccess();
				if (msAge < cfg.msLife) {
					ageLimited = true;
					if (trace) {
						cfg.probe.liveManagement("purge.ageLimited");
					}
				} else {
					zlNames.add(tracker.qccFileName());
					tracker.purgeMark();
					kbNeo -= tracker.kbFile();
					popNeo--;
				}
			}
			return zlNames;
		}

		public Tracker findTracker(String qccFileName, long tsNow) {
			final Tracker oTracker = m_mapFileName_Tracker.get(qccFileName);
			if (oTracker != null) {
				oTracker.registerAccess(tsNow);
			}
			return oTracker;
		}

		public boolean isPurgeRequired() {
			final int popCacheFileActual = m_mapFileName_Tracker.size();
			return (m_kbActual > cfg.kbWake) || (popCacheFileActual > cfg.popWake);
		}

		public long kbActual() {
			return m_kbActual;
		}

		public JsonObject newJsonCheckpoint() {
			final List<Tracker> zlTrackersAsc = new ArrayList<>(m_mapFileName_Tracker.values());
			Collections.sort(zlTrackersAsc);
			final int trackerCount = zlTrackersAsc.size();
			final JsonArray array = JsonArray.newMutable(trackerCount);
			for (int i = 0; i < trackerCount; i++) {
				final Tracker tracker = zlTrackersAsc.get(i);
				final JsonObject t = JsonObject.newMutable();
				tracker.save(t);
				array.add(t);
			}
			final JsonObject neo = JsonObject.newMutable();
			neo.put(p_trackers, array);
			return neo;
		}

		public SensorSnapshot newSensorSnapshot() {
			return new SensorSnapshot(m_kbActual, m_mapFileName_Tracker.size());
		}

		public void purge(String qccFileName) {
			final boolean trace = cfg.probe.isLiveManagement();
			final Tracker oEx = m_mapFileName_Tracker.get(qccFileName);
			if (oEx == null) {
				if (trace) {
					cfg.probe.liveManagement("purge.file.missingTracker", qccFileName);
				}
			} else {
				if (oEx.exists()) {
					if (oEx.isPurgeSafe()) {
						final File target = newFile(qccFileName);
						ArgonFileManagement.deleteFile(cfg.probe, target);
						if (target.exists()) {
							if (trace) {
								cfg.probe.liveManagement("purge.file.trackerRetained", oEx);
							}
						} else {
							m_mapFileName_Tracker.remove(qccFileName);
							m_kbActual -= oEx.kbFile();
						}
					} else {
						if (trace) {
							cfg.probe.liveManagement("purge.file.trackerNotPurgeSafe", oEx);
						}
					}
				} else {
					m_mapFileName_Tracker.remove(qccFileName);
				}
			}
		}

		public Tracker putTracker(String qccFileName, Dcu dcu, Date oLastModified, Date oExpires, long tsNow) {
			Tracker vTracker = m_mapFileName_Tracker.get(qccFileName);
			if (vTracker == null) {
				vTracker = new Tracker(qccFileName, dcu, oLastModified, oExpires, tsNow);
				m_mapFileName_Tracker.put(qccFileName, vTracker);
			} else {
				m_kbActual -= vTracker.kbFile();
				vTracker.registerReload(dcu, oLastModified, oExpires, tsNow);
			}
			m_kbActual += vTracker.kbFile();
			return vTracker;
		}

		public void reclaimUnreferenced(String qccFileName) {
			if (!m_mapFileName_Tracker.containsKey(qccFileName)) {
				final File target = new File(cfg.cndir, qccFileName);
				ArgonFileManagement.deleteFile(cfg.probe, target);
			}
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("DiskMruTable.State");
			ds.a("kBActual", m_kbActual);
			ds.a("map", m_mapFileName_Tracker);
			return ds.s();
		}

		public List<String> zlUnreferencedAsc(String[] zptDiskFileNamesAsc) {
			final List<String> zl = new ArrayList<>();
			final int cc = zptDiskFileNamesAsc.length;
			for (int i = 0; i < cc; i++) {
				final String qccFileName = zptDiskFileNamesAsc[i];
				if (!qccFileName.equals(CheckpointFileName)) {
					if (!m_mapFileName_Tracker.containsKey(qccFileName)) {
						zl.add(qccFileName);
					}
				}
			}
			return zl;
		}

		public State(Cfg cfg, int cap) {
			this.cfg = cfg;
			m_mapFileName_Tracker = new HashMap<>(cap);
		}

		public State(Cfg cfg, JsonObject src) throws JsonSchemaException {
			this.cfg = cfg;
			final JsonArray array = src.accessor(p_trackers).datumArray();
			final int trackerCount = array.jsonMemberCount();
			m_mapFileName_Tracker = new HashMap<>(trackerCount);
			for (int i = 0; i < trackerCount; i++) {
				final Tracker tracker = new Tracker(array.accessor(i).datumObject());
				m_mapFileName_Tracker.put(tracker.qccFileName(), tracker);
				m_kbActual += tracker.kbFile();
			}
		}
		private final Cfg cfg;
		private final Map<String, Tracker> m_mapFileName_Tracker;
		private long m_kbActual;
	}

	private static class Tracker implements Comparable<Tracker> {

		private static Date oLastModified(long tsnLastModified) {
			return tsnLastModified == TSNIL ? null : DateFactory.newDate(tsnLastModified);
		}

		private static long tsExpires(Date oExpires, long tsNow) {
			return oExpires == null ? tsNow : oExpires.getTime();
		}

		private static long tsnLastModified(Dcu dcu, Date oLastModified) {
			return oLastModified == null || !dcu.exists() ? TSNIL : oLastModified.getTime();
		}

		@Override
		public int compareTo(Tracker rhs) {
			final int c0 = ArgonCompare.fwd(m_tsLastAccess, rhs.m_tsLastAccess);
			if (c0 != 0) return c0;
			final int c1 = ArgonCompare.rev(m_dcu, rhs.m_dcu);
			if (c1 != 0) return c1;
			final int c2 = m_qccFileName.compareTo(rhs.m_qccFileName);
			return c2;
		}

		public boolean exists() {
			return Dcu.exists(m_dcu);
		}

		public boolean isPurgeSafe() {
			return m_purgeMarked;
		}

		public int kbFile() {
			return Dcu.kbUsage(m_dcu);
		}

		public Descriptor newDescriptor() {
			final boolean exists = Dcu.exists(m_dcu);
			final Date oLastModified = oLastModified(m_tsnLastModified);
			return new Descriptor(m_qccFileName, oLastModified, m_tsLastAccess, m_tsExpires, exists);
		}

		public void purgeMark() {
			m_purgeMarked = true;
		}

		public String qccFileName() {
			return m_qccFileName;
		}

		public void registerAccess(long tsNow) {
			if (tsNow > m_tsLastAccess) {
				m_tsLastAccess = tsNow;
				m_purgeMarked = false;
			}
		}

		public void registerReload(Dcu dcu, Date oLastModified, Date oExpires, long tsNow) {
			m_dcu = dcu.toInteger();
			m_tsLastAccess = tsNow;
			m_tsnLastModified = tsnLastModified(dcu, oLastModified);
			m_tsExpires = tsExpires(oExpires, tsNow);
			m_purgeMarked = false;
		}

		public void save(JsonObject dst) {
			dst.putString(p_fileName, m_qccFileName);
			dst.putTime(p_lastAccess, m_tsLastAccess);
			dst.putInteger(p_dcu, m_dcu);
			dst.putTime(p_lastModified, m_tsnLastModified);
			dst.putTime(p_expires, m_tsExpires);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("DiskMruTable.Tracker");
			ds.a("fileName", m_qccFileName);
			ds.at8("lastAccess", m_tsLastAccess);
			ds.a("dcu", m_dcu);
			if (m_tsnLastModified != TSNIL) {
				ds.at8("lastModified", m_tsnLastModified);
			}
			ds.at8("expires", m_tsExpires);
			ds.a("purgeMarked", m_purgeMarked);
			return ds.s();
		}

		public long tsLastAccess() {
			return m_tsLastAccess;
		}

		public Tracker(JsonObject src) throws JsonSchemaException {
			if (src == null) throw new IllegalArgumentException("object is null");
			m_qccFileName = src.accessor(p_fileName).datumQtwString();
			m_tsLastAccess = src.accessor(p_lastAccess).datumTs();
			m_dcu = src.accessor(p_dcu).datumInteger();
			m_tsnLastModified = src.accessor(p_lastModified).datumTs();
			m_tsExpires = src.accessor(p_expires).datumTs();
		}

		public Tracker(String qccFileName, Dcu dcu, Date oLastModified, Date oExpires, long tsNow) {
			assert qccFileName != null && qccFileName.length() > 0;
			m_qccFileName = qccFileName;
			m_tsLastAccess = tsNow;
			m_dcu = dcu.toInteger();
			m_tsnLastModified = tsnLastModified(dcu, oLastModified);
			m_tsExpires = tsExpires(oExpires, tsNow);
		}
		private final String m_qccFileName;
		private long m_tsLastAccess;
		private int m_dcu;
		private long m_tsnLastModified;
		private long m_tsExpires;
		private boolean m_purgeMarked;
	}

	static class SensorSnapshot {

		@Override
		public String toString() {
			final Ds ds = Ds.o("DiskMruTable.SensorSnapshot");
			ds.a("dcuActual", dcuActual);
			ds.a("popActual", popActual);
			return ds.s();
		}

		public SensorSnapshot(long dcuActual, int popActual) {
			this.dcuActual = dcuActual;
			this.popActual = popActual;
		}
		public final long dcuActual;
		public final int popActual;
	}

	public static class Descriptor {

		@Override
		public String toString() {
			final Ds ds = Ds.o("DiskMruTable.Descriptor");
			ds.a("fileName", qccFileName);
			ds.at8("lastModified", oLastModified);
			ds.at8("lastAccess", tsLastAccess);
			ds.at8("expires", tsExpires);
			ds.a("exists", exists);
			return ds.s();
		}

		private Descriptor(String qccFileName, Date oLastModified, long tsLastAccess, long tsExpires, boolean exists) {
			assert qccFileName != null && qccFileName.length() > 0;
			this.qccFileName = qccFileName;
			this.oLastModified = oLastModified;
			this.tsLastAccess = tsLastAccess;
			this.tsExpires = tsExpires;
			this.exists = exists;
		}
		public final String qccFileName;
		public final Date oLastModified;
		public final long tsLastAccess;
		public final long tsExpires;
		public final boolean exists;
	}

}
