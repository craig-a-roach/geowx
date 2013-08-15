/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author roach
 */
public class NeonCallableCache {

	private void putLocked(AccessRecord accessRecord) {
		assert accessRecord != null;

		final int budget = kc.cfg.getCallableCacheLineBudget();
		final int cost = accessRecord.cost();

		Collections.sort(m_accessRecordsAscUsage);
		final int recoveryTarget = m_charge + cost - budget;
		int recovered = 0;
		final Iterator<AccessRecord> iAccessRecord = m_accessRecordsAscUsage.iterator();
		while (recovered < recoveryTarget && iAccessRecord.hasNext()) {
			final AccessRecord rec = iAccessRecord.next();
			final int rcost = rec.cost();
			m_source_AccessRecord.remove(rec.source);
			m_charge -= rcost;
			recovered += rcost;
			iAccessRecord.remove();
		}

		m_source_AccessRecord.put(accessRecord.source, accessRecord);
		m_accessRecordsAscUsage.add(accessRecord);
		m_charge += cost;
	}

	public IEsCallable newCallable(EsSource source)
			throws EsSyntaxException {
		if (source == null) throw new IllegalArgumentException("object is null");
		m_lock.lock();
		try {
			AccessRecord vAccessRecord = m_source_AccessRecord.get(source);
			if (vAccessRecord == null) {
				final IEsCallable callable = source.newCallable();
				vAccessRecord = new AccessRecord(source, callable);
				putLocked(vAccessRecord);
			} else {
				vAccessRecord.used();
			}

			return vAccessRecord.callable;

		} finally {
			m_lock.unlock();
		}
	}

	NeonCallableCache(KernelCfg kc) {
		this.kc = kc;
		m_source_AccessRecord = new HashMap<EsSource, AccessRecord>(CNeon.CallableCacheInitialCapacity);
		m_accessRecordsAscUsage = new ArrayList<AccessRecord>(CNeon.CallableCacheInitialCapacity);
	}

	final KernelCfg kc;
	private final Lock m_lock = new ReentrantLock();
	private final Map<EsSource, AccessRecord> m_source_AccessRecord;
	private final List<AccessRecord> m_accessRecordsAscUsage;
	private int m_charge = 0;

	private static class AccessRecord implements Comparable<AccessRecord> {
		public int compareTo(AccessRecord r) {
			if (m_lastAccessed < r.m_lastAccessed) return -1;
			if (m_lastAccessed > r.m_lastAccessed) return 1;
			if (m_cost < r.m_cost) return 1;
			if (m_cost > r.m_cost) return -1;
			return 0;
		}

		public int cost() {
			return m_cost;
		}

		public void used() {
			m_lastAccessed = System.currentTimeMillis();
		}

		public AccessRecord(EsSource source, IEsCallable callable) {
			assert source != null;
			assert callable != null;
			this.source = source;
			this.callable = callable;
			m_cost = source.lineCount();
			m_lastAccessed = System.currentTimeMillis();
		}
		public final EsSource source;
		public final IEsCallable callable;
		private final int m_cost;
		private long m_lastAccessed;
	}
}
