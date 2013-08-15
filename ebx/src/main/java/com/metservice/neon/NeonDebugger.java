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
import java.util.regex.Pattern;

import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
public class NeonDebugger {

	private DebuggerSession declareSession(BerylliumSupportId sid) {
		assert sid != null;
		m_rwlock.writeLock().lock();
		try {
			DebuggerSession vSession = m_map.get(sid);
			if (vSession == null) {
				vSession = new DebuggerSession(sid, m_oAutoPattern);
				m_map.put(sid, vSession);
			}
			return vSession;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	private boolean matchesAutoPattern(String qccSourcePath) {
		return m_oAutoPattern != null && m_oAutoPattern.matcher(qccSourcePath).matches();
	}

	public void apply(BerylliumSupportId sid, String qccSourcePath, DebugCommand command)
			throws InterruptedException {
		final DebuggerSession oSession = findSession(sid);
		if (oSession != null) {
			oSession.apply(qccSourcePath, command);
		}
	}

	public void attach(BerylliumSupportId sid, String qccSourcePath) {
		if (sid == null) throw new IllegalArgumentException("object is null");
		DebuggerSession oSession = findSession(sid);
		if (oSession == null && matchesAutoPattern(qccSourcePath)) {
			oSession = declareSession(sid);
		}
		if (oSession != null) {
			oSession.attach(qccSourcePath);
		}
	}

	public void enable(BerylliumSupportId sid, String qccSourcePath, boolean enabled) {
		final DebuggerSession oSession = enabled ? declareSession(sid) : findSession(sid);
		if (oSession != null) {
			oSession.enable(qccSourcePath, enabled);
		}
	}

	public DebuggerSession findSession(BerylliumSupportId sid) {
		if (sid == null) throw new IllegalArgumentException("object is null");
		m_rwlock.readLock().lock();
		try {
			return m_map.get(sid);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public Pattern getAutoPattern(BerylliumSupportId sid) {
		final DebuggerSession oSession = findSession(sid);
		return oSession == null ? m_oAutoPattern : oSession.getAutoPattern();
	}

	public DebugState getState(BerylliumSupportId sid, String qccSourcePath) {
		final DebuggerSession oSession = findSession(sid);
		return oSession == null ? null : oSession.getState(qccSourcePath);
	}

	public boolean isEnabled(BerylliumSupportId sid, String qccSourcePath) {
		final DebuggerSession oSession = findSession(sid);
		return oSession != null && oSession.isEnabled(qccSourcePath);
	}

	public void pushState(BerylliumSupportId sid, String qccSourcePath, DebugState state)
			throws InterruptedException {
		final DebuggerSession oSession = findSession(sid);
		if (oSession != null) {
			oSession.pushState(qccSourcePath, state);
		}
	}

	public void removeSession(BerylliumSupportId sid) {
		DebuggerSession oDebuggerSession = null;
		m_rwlock.writeLock().lock();
		try {
			oDebuggerSession = m_map.remove(sid);
		} finally {
			m_rwlock.writeLock().unlock();
		}
		if (oDebuggerSession != null) {
			oDebuggerSession.removeAll();
		}
	}

	public void resume(BerylliumSupportId sid, String qccSourcePath) {
		final DebuggerSession oSession = findSession(sid);
		if (oSession != null) {
			oSession.resume(qccSourcePath);
		}
	}

	public void setAutoPattern(BerylliumSupportId sid, Pattern oPattern) {
		declareSession(sid).setAutoPattern(oPattern);
	}

	public void start(BerylliumSupportId sid, String qccSourcePath) {
		final DebuggerSession oSession = findSession(sid);
		if (oSession != null) {
			oSession.start(qccSourcePath);
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

	public NeonDebugger(KernelCfg kc) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		m_oAutoPattern = kc.cfg.getAutoDebugPattern();
	}
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private final Map<BerylliumSupportId, DebuggerSession> m_map = new HashMap<BerylliumSupportId, DebuggerSession>(8);
	private final Pattern m_oAutoPattern;
}
