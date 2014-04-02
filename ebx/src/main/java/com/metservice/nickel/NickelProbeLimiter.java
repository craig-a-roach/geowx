/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.nickel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author roach
 */
public class NickelProbeLimiter {

	public boolean admit(String qcctwSubject) {
		if (qcctwSubject == null || qcctwSubject.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_lock.lock();
		try {
			Counter oCounter = m_subjectTable.get(qcctwSubject);
			if (oCounter == null) {
				oCounter = new Counter();
				m_subjectTable.put(qcctwSubject, oCounter);
			}
			final int neo = oCounter.increment();
			return neo <= m_reportLimit;
		} finally {
			m_lock.unlock();
		}
	}

	public int count(String qcctwSubject, boolean elided) {
		if (qcctwSubject == null || qcctwSubject.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_lock.lock();
		try {
			final Counter oCounter = m_subjectTable.get(qcctwSubject);
			final int count = oCounter == null ? 0 : oCounter.value();
			if (!elided) return count;
			if (count <= m_reportLimit) return 0;
			return count - m_reportLimit;
		} finally {
			m_lock.unlock();
		}
	}

	public int reportLimit() {
		return m_reportLimit;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final Map.Entry<String, Counter> e : m_subjectTable.entrySet()) {
			sb.append(e.getKey());
			sb.append('=');
			sb.append(e.getValue().value());
			sb.append('\n');
		}
		return sb.toString();
	}

	public List<String> zlElidedSubjectsAsc() {
		m_lock.lock();
		try {
			final List<String> zlOut = new ArrayList<String>();
			final List<String> zlAsc = new ArrayList<String>(m_subjectTable.keySet());
			Collections.sort(zlAsc);
			for (final String qcctwSubject : zlAsc) {
				final Counter vCounter = m_subjectTable.get(qcctwSubject);
				if (vCounter == null) {
					continue;
				}
				if (vCounter.value() > m_reportLimit) {
					zlOut.add(qcctwSubject);
				}
			}
			return zlOut;
		} finally {
			m_lock.unlock();
		}

	}

	public List<String> zlSubjectsAsc() {
		m_lock.lock();
		try {
			final List<String> zl = new ArrayList<String>(m_subjectTable.keySet());
			Collections.sort(zl);
			return zl;
		} finally {
			m_lock.unlock();
		}
	}

	public NickelProbeLimiter(int reportLimit) {
		m_reportLimit = reportLimit;
	}

	private final int m_reportLimit;
	private final Lock m_lock = new ReentrantLock();
	private final Map<String, Counter> m_subjectTable = new HashMap<String, Counter>();

	private static class Counter {

		public int increment() {
			m_value++;
			return m_value;
		}

		public int value() {
			return m_value;
		}

		Counter() {
			m_value = 0;
		}
		private int m_value;
	}

}
