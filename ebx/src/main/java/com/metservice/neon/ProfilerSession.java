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
class ProfilerSession {

	public void addSample(String qccSourcePath, ProfileSample prf) {
		final ProfilerSessionSource oEx = findSource(qccSourcePath);
		if (oEx != null) {
			oEx.addSample(prf);
		}
	}

	public void enable(String qccSourcePath, boolean enabled) {
		m_rwlock.writeLock().lock();
		try {
			if (enabled) {
				if (!m_map.containsKey(qccSourcePath)) {
					final ProfilerSessionSource neo = new ProfilerSessionSource(qccSourcePath);
					m_map.put(qccSourcePath, neo);
				}
			} else {
				m_map.remove(qccSourcePath);
			}
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public ProfilerSessionSource findSource(String qccSourcePath) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		m_rwlock.readLock().lock();
		try {
			return m_map.get(qccSourcePath);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public ProfileAggregate getAggregate(String qccSourcePath) {
		final ProfilerSessionSource oEx = findSource(qccSourcePath);
		return oEx == null ? null : oEx.getAggregate();
	}

	public boolean isEnabled(String qccSourcePath) {
		m_rwlock.readLock().lock();
		try {
			return m_map.containsKey(qccSourcePath);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public void removeAll() {
		m_rwlock.writeLock().lock();
		try {
			m_map.clear();
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public List<String> zlqccSourcePathsAsc() {
		m_rwlock.readLock().lock();
		try {
			final List<String> zl = new ArrayList<String>(m_map.keySet());
			Collections.sort(zl);
			return zl;
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public ProfilerSession(BerylliumSupportId idSupport) {
		if (idSupport == null) throw new IllegalArgumentException("object is null");
		this.idSupport = idSupport;
	}
	public final BerylliumSupportId idSupport;
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private final Map<String, ProfilerSessionSource> m_map = new HashMap<String, ProfilerSessionSource>(8);
}
