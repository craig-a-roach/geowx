/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class MonitoringTable {

	private static ProcessEngine[] zptEnginesAsc(Collection<ProcessEngine> zcEngines) {
		assert zcEngines != null;
		final int count = zcEngines.size();
		final ProcessEngine[] zptAsc = zcEngines.toArray(new ProcessEngine[count]);
		Arrays.sort(zptAsc);
		return zptAsc;
	}

	private static ProcessEngine[] zptEnginesAsc(Map<BoronProcessId, ProcessEngine> zmEngines) {
		return zptEnginesAsc(zmEngines.values());
	}

	private void putUniqueLk(Map<BoronProcessId, ProcessEngine> zm, ProcessEngine neo) {
		assert zm != null;
		assert neo != null;
		final BoronProcessId bpid = neo.processId();
		final ProcessEngine oEx = zm.put(bpid, neo);
		if (oEx != null) {
			final Ds ds = Ds.invalidBecause("Non-unique process id", "Possible process leak");
			ds.a("bpid", bpid);
			ds.a("neo", neo);
			ds.a("ex", oEx);
			kc.probe.warnSoftware(ds);
		}
	}

	private ProcessEngine queryFindLk(BoronProcessId bpid) {
		final ProcessEngine oActiveEngine = m_zmActiveId_Engine.get(bpid);
		if (oActiveEngine != null) return oActiveEngine;

		final ProcessEngine oHaltedEngine = m_zmHaltedId_Engine.get(bpid);
		if (oHaltedEngine != null) return oHaltedEngine;

		return m_zmCancelledId_Engine.get(bpid);
	}

	private boolean queryIsBusyLk() {
		if (!m_zmActiveId_Engine.isEmpty()) return true;
		if (!m_zmCancelledId_Engine.isEmpty()) return true;
		return false;
	}

	private ProcessEngine removeLk(Map<BoronProcessId, ProcessEngine> zm, BoronProcessId bpid) {
		assert zm != null;
		assert bpid != null;
		return zm.remove(bpid);
	}

	private void removeLk(Map<BoronProcessId, ProcessEngine> zm, ProcessEngine engine) {
		assert zm != null;
		assert engine != null;
		zm.remove(engine.processId());
	}

	private void tranCancelActiveLk() {
		final ProcessEngine[] zptActiveAsc = zptEnginesAsc(m_zmActiveId_Engine);
		for (int i = 0; i < zptActiveAsc.length; i++) {
			final ProcessEngine activeEngine = zptActiveAsc[i];
			removeLk(m_zmActiveId_Engine, activeEngine);
			activeEngine.cancel();
			putUniqueLk(m_zmCancelledId_Engine, activeEngine);
		}
	}

	private ProcessEngine tranCancelActiveLk(BoronProcessId bpid) {
		final ProcessEngine oActiveEngine = removeLk(m_zmActiveId_Engine, bpid);
		if (oActiveEngine != null) {
			oActiveEngine.cancel();
			putUniqueLk(m_zmCancelledId_Engine, oActiveEngine);
		}
		return oActiveEngine;
	}

	private void tranHealthActiveLk(long tsNow) {
		final ProcessEngine[] zptActiveAsc = zptEnginesAsc(m_zmActiveId_Engine);
		for (int i = 0; i < zptActiveAsc.length; i++) {
			final ProcessEngine activeEngine = zptActiveAsc[i];
			if (activeEngine.isHalted()) {
				putUniqueLk(m_zmHaltedId_Engine, activeEngine);
				removeLk(m_zmActiveId_Engine, activeEngine);
			} else if (activeEngine.isOverdue(tsNow)) {
				activeEngine.cancel();
				putUniqueLk(m_zmCancelledId_Engine, activeEngine);
				removeLk(m_zmActiveId_Engine, activeEngine);
			}
		}
	}

	private void tranHealthCancelledLk() {
		final ProcessEngine[] zptCancelledAsc = zptEnginesAsc(m_zmCancelledId_Engine);
		for (int i = 0; i < zptCancelledAsc.length; i++) {
			final ProcessEngine cancelledEngine = zptCancelledAsc[i];
			if (cancelledEngine.isHalted()) {
				removeLk(m_zmCancelledId_Engine, cancelledEngine);
				m_diskController.onRemoveHalted(cancelledEngine);
			}
		}
	}

	private ProcessEngine tranRemoveHaltedLk(BoronProcessId bpid) {
		final ProcessEngine oHaltedEngine = removeLk(m_zmHaltedId_Engine, bpid);
		if (oHaltedEngine != null) {
			m_diskController.onRemoveHalted(oHaltedEngine);
		}
		return oHaltedEngine;
	}

	public void add(ProcessEngine engine) {
		if (engine == null) throw new IllegalArgumentException("object is null");
		m_rwlock.writeLock().lock();
		try {
			if (engine.isHalted()) {
				putUniqueLk(m_zmHaltedId_Engine, engine);
			} else {
				putUniqueLk(m_zmActiveId_Engine, engine);
			}
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void cancel() {
		m_rwlock.writeLock().lock();
		try {
			tranCancelActiveLk();
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public ProcessEngine find(BoronProcessId processId) {
		if (processId == null) throw new IllegalArgumentException("object is null");
		m_rwlock.readLock().lock();
		try {
			return queryFindLk(processId);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public boolean isBusy() {
		m_rwlock.readLock().lock();
		try {
			return queryIsBusyLk();
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public void onHealth(long tsNow) {
		m_rwlock.writeLock().lock();
		try {
			tranHealthActiveLk(tsNow);
			tranHealthCancelledLk();
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public ProcessEngine remove(BoronProcessId processId) {
		if (processId == null) throw new IllegalArgumentException("object is null");
		m_rwlock.writeLock().lock();
		try {
			final ProcessEngine oExActive = tranCancelActiveLk(processId);
			if (oExActive != null) return oExActive;
			return tranRemoveHaltedLk(processId);
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("zmActiveId_Engine", m_zmActiveId_Engine);
		ds.a("zmCancelledId_Engine", m_zmCancelledId_Engine);
		ds.a("zmHaltedId_Engine", m_zmHaltedId_Engine);
		return ds.s();
	}

	public MonitoringTable(KernelCfg kc, DiskController dc) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		if (dc == null) throw new IllegalArgumentException("object is null");
		this.kc = kc;
		m_diskController = dc;
	}

	final KernelCfg kc;
	private final DiskController m_diskController;
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private final Map<BoronProcessId, ProcessEngine> m_zmActiveId_Engine = new HashMap<BoronProcessId, ProcessEngine>(256);
	private final Map<BoronProcessId, ProcessEngine> m_zmCancelledId_Engine = new HashMap<BoronProcessId, ProcessEngine>(64);
	private final Map<BoronProcessId, ProcessEngine> m_zmHaltedId_Engine = new HashMap<BoronProcessId, ProcessEngine>(256);
}
