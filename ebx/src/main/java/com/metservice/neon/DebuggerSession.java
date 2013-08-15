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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
class DebuggerSession {

	private DebuggerSessionSource declareSource(String qccSourcePath) {
		m_rwlock.writeLock().lock();
		try {
			DebuggerSessionSource vSource = m_map.get(qccSourcePath);
			if (vSource == null) {
				vSource = new DebuggerSessionSource(qccSourcePath);
				m_map.put(qccSourcePath, vSource);
			}
			return vSource;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	private boolean matchesAutoPattern(String qccSourcePath) {
		assert qccSourcePath != null && qccSourcePath.length() > 0;
		final Pattern oAutoPattern = m_autoPattern.get();
		return oAutoPattern != null && oAutoPattern.matcher(qccSourcePath).matches();
	}

	private void removeSource(String qccSourcePath) {
		assert qccSourcePath != null && qccSourcePath.length() > 0;
		m_rwlock.writeLock().lock();
		try {
			final DebuggerSessionSource oSource = m_map.remove(qccSourcePath);
			if (oSource != null) {
				oSource.resume();
			}
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void apply(String qccSourcePath, DebugCommand command)
			throws InterruptedException {
		final DebuggerSessionSource oEx = findSource(qccSourcePath);
		if (oEx != null) {
			oEx.apply(command);
		}
	}

	public void attach(String qccSourcePath) {
		final DebuggerSessionSource oSource = findSource(qccSourcePath);
		if (oSource == null && matchesAutoPattern(qccSourcePath)) {
			declareSource(qccSourcePath);
		}
	}

	public void enable(String qccSourcePath, boolean enabled) {
		if (enabled) {
			declareSource(qccSourcePath);
		} else {
			removeSource(qccSourcePath);
		}
	}

	public DebuggerSessionSource findSource(String qccSourcePath) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		m_rwlock.readLock().lock();
		try {
			return m_map.get(qccSourcePath);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public Pattern getAutoPattern() {
		return m_autoPattern.get();
	}

	public DebugState getState(String qccSourcePath) {
		final DebuggerSessionSource oEx = findSource(qccSourcePath);
		return oEx == null ? null : oEx.getState();
	}

	public boolean isEnabled(String qccSourcePath) {
		return findSource(qccSourcePath) != null;
	}

	public void pushState(String qccSourcePath, DebugState state)
			throws InterruptedException {
		final DebuggerSessionSource oEx = findSource(qccSourcePath);
		if (oEx != null) {
			oEx.pushState(state);
		}
	}

	public void removeAll() {
		final List<String> zlqccSourcePathsAsc = zlqccSourcePathsAsc(true);
		for (final String qccSourcePath : zlqccSourcePathsAsc) {
			removeSource(qccSourcePath);
		}
	}

	public void resume(String qccSourcePath) {
		final DebuggerSessionSource oEx = findSource(qccSourcePath);
		if (oEx != null) {
			oEx.resume();
		}
	}

	public void setAutoPattern(Pattern oPattern) {
		m_autoPattern.set(oPattern);
	}

	public void start(String qccSourcePath) {
		final DebuggerSessionSource oEx = findSource(qccSourcePath);
		if (oEx != null) {
			oEx.start();
		}
	}

	public List<String> zlqccSourcePathsAsc(boolean includeInactive) {
		m_rwlock.readLock().lock();
		try {
			final List<String> zl = new ArrayList<String>();
			for (final String qccSourcePath : m_map.keySet()) {
				final DebuggerSessionSource oSource = findSource(qccSourcePath);
				if (oSource != null) {
					final boolean active = oSource.getState() != null;
					if (includeInactive || active) {
						zl.add(qccSourcePath);
					}
				}
			}
			Collections.sort(zl);
			return zl;
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public DebuggerSession(BerylliumSupportId idSupport, Pattern oAutoPattern) {
		if (idSupport == null) throw new IllegalArgumentException("object is null");
		this.idSupport = idSupport;
		m_autoPattern = new AtomicReference<Pattern>(oAutoPattern);
	}
	public final BerylliumSupportId idSupport;
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private final Map<String, DebuggerSessionSource> m_map = new HashMap<String, DebuggerSessionSource>(8);
	private final AtomicReference<Pattern> m_autoPattern;
}
