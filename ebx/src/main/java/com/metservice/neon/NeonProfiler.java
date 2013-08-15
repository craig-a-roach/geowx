/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
public class NeonProfiler {

	public void addSample(BerylliumSupportId sid, String qccSourcePath, ProfileSample prf) {
		final ProfilerSession oSession = findSession(sid);
		if (oSession != null) {
			oSession.addSample(qccSourcePath, prf);
		}
	}

	public void enable(BerylliumSupportId sid, String qccSourcePath, boolean enabled) {
		final ProfilerSession profilerSession;
		m_rwlock.writeLock().lock();
		try {
			ProfilerSession vProfilerSession = m_map.get(sid);
			if (vProfilerSession == null) {
				vProfilerSession = new ProfilerSession(sid);
				m_map.put(sid, vProfilerSession);
			}
			profilerSession = vProfilerSession;
		} finally {
			m_rwlock.writeLock().unlock();
		}
		profilerSession.enable(qccSourcePath, enabled);
	}

	public ProfilerSession findSession(BerylliumSupportId sid) {
		if (sid == null) throw new IllegalArgumentException("object is null");
		m_rwlock.readLock().lock();
		try {
			return m_map.get(sid);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public ProfileAggregate getAggregate(BerylliumSupportId sid, String qccSourcePath) {
		final ProfilerSession oSession = findSession(sid);
		return oSession == null ? null : oSession.getAggregate(qccSourcePath);
	}

	public boolean isEnabled(BerylliumSupportId sid, String qccSourcePath) {
		final ProfilerSession oSession = findSession(sid);
		return oSession != null && oSession.isEnabled(qccSourcePath);
	}

	public void removeSession(BerylliumSupportId sid) {
		ProfilerSession oProfilerSession = null;
		m_rwlock.writeLock().lock();
		try {
			oProfilerSession = m_map.remove(sid);
		} finally {
			m_rwlock.writeLock().unlock();
		}
		if (oProfilerSession != null) {
			oProfilerSession.removeAll();
		}
	}

	public List<BerylliumSupportId> zlSupportIdsAsc() {
		m_rwlock.readLock().lock();
		try {
			final List<BerylliumSupportId> zl = new ArrayList<BerylliumSupportId>(m_map.keySet());
			Collections.sort(zl);
			return zl;
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public NeonProfiler() {
	}
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private final Map<BerylliumSupportId, ProfilerSession> m_map = new HashMap<BerylliumSupportId, ProfilerSession>(8);
}
