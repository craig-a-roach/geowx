/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.collection.DynamicArray;

/**
 * @author roach
 */
class ConsoleSource {

	public static final int RolloverThresholdPct = 10;

	public void addEntry(ConsoleEntry entry) {
		if (entry == null) throw new IllegalArgumentException("object is null");
		m_lockState.lock();
		try {
			m_daEntry.push(entry);
			m_daEntry.applyQuota(quota, RolloverThresholdPct);
		} finally {
			m_lockState.unlock();
		}
	}

	public void clear() {
		m_lockState.lock();
		try {
			m_daEntry.clear();
		} finally {
			m_lockState.unlock();
		}
	}

	public boolean isEmpty() {
		m_lockState.lock();
		try {
			return m_daEntry.isEmpty();
		} finally {
			m_lockState.unlock();
		}
	}

	public ConsoleEntry[] zptEntries(ConsoleFilter filter) {
		m_lockState.lock();
		try {
			m_daEntry.ensure();
			final int count = m_daEntry.count;
			final ConsoleEntry[] zp = new ConsoleEntry[count];
			int w = 0;
			for (int i = 0; i < count; i++) {
				final ConsoleEntry e = m_daEntry.array[i];
				if (filter.match(e)) {
					zp[w] = e;
					w++;
				}
			}
			if (w == zp.length) return zp;
			final ConsoleEntry[] zpt = new ConsoleEntry[w];
			System.arraycopy(zp, 0, zpt, 0, w);
			return zpt;
		} finally {
			m_lockState.unlock();
		}
	}

	public ConsoleSource(String qccSourcePath, int quota) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		this.qccSourcePath = qccSourcePath;
		this.quota = Math.max(1, quota);
		m_daEntry = new DynamicArray<ConsoleEntry>() {

			@Override
			public ConsoleEntry[] newArray(int cap) {
				return new ConsoleEntry[cap];
			}
		};
	}

	public final String qccSourcePath;
	public final int quota;
	private final Lock m_lockState = new ReentrantLock();
	private final DynamicArray<ConsoleEntry> m_daEntry;
}
