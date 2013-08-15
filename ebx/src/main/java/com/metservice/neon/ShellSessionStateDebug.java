/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author roach
 */
class ShellSessionStateDebug {

	private WatchSet selectWatchSet(WatchMethod wm) {
		if (wm == null) throw new IllegalArgumentException("object is null");
		switch (wm) {
			case toString:
				return m_watchSetString;
			case Xml:
				return m_watchSetXml;
			default:
				throw new IllegalArgumentException("invalid watch method>" + wm + "<");
		}
	}

	public void breakpointAdd(int lineNo) {
		m_lock.lock();
		try {
			m_breakpointLines = m_breakpointLines.add(lineNo);
		} finally {
			m_lock.unlock();
		}
	}

	public EsBreakpointLines breakpointLines() {
		m_lock.lock();
		try {
			return m_breakpointLines;
		} finally {
			m_lock.unlock();
		}
	}

	public void breakpointRemove(int lineNo) {
		m_lock.lock();
		try {
			m_breakpointLines = m_breakpointLines.remove(lineNo);
		} finally {
			m_lock.unlock();
		}
	}

	public int codeReveal() {
		m_lock.lock();
		try {
			return m_codeReveal;
		} finally {
			m_lock.unlock();
		}
	}

	public void codeReveal(int value) {
		m_lock.lock();
		try {
			m_codeReveal = value;
		} finally {
			m_lock.unlock();
		}
	}

	public int operandShowDepth() {
		m_lock.lock();
		try {
			return m_operandShowDepth;
		} finally {
			m_lock.unlock();
		}
	}

	public void operandShowDepth(int value) {
		m_lock.lock();
		try {
			m_operandShowDepth = value;
		} finally {
			m_lock.unlock();
		}
	}

	public void watchAdd(WatchMethod wm, String qccPropertyName) {
		m_lock.lock();
		try {
			selectWatchSet(wm).watchAdd(qccPropertyName);
		} finally {
			m_lock.unlock();
		}
	}

	public void watchRemove(WatchMethod wm, String qccPropertyName) {
		m_lock.lock();
		try {
			selectWatchSet(wm).watchRemove(qccPropertyName);
		} finally {
			m_lock.unlock();
		}
	}

	public Set<String> watchSubtract(WatchMethod wm, Set<String> zsPropertyNames) {
		m_lock.lock();
		try {
			return selectWatchSet(wm).watchSubtract(zsPropertyNames);
		} finally {
			m_lock.unlock();
		}
	}

	public List<String> zlWatchAsc(WatchMethod wm) {
		m_lock.lock();
		try {
			return selectWatchSet(wm).zlWatchAsc();
		} finally {
			m_lock.unlock();
		}
	}

	public ShellSessionStateDebug() {
	}

	private final Lock m_lock = new ReentrantLock();
	private int m_operandShowDepth = 1;
	private int m_codeReveal = 16;
	private final WatchSet m_watchSetString = new WatchSet();
	private final WatchSet m_watchSetXml = new WatchSet();
	private EsBreakpointLines m_breakpointLines = EsBreakpointLines.None;

	private static class WatchSet {

		public void watchAdd(String qccPropertyName) {
			if (qccPropertyName == null || qccPropertyName.length() == 0)
				throw new IllegalArgumentException("string is null or empty");
			m_zsWatch.add(qccPropertyName);
		}

		public void watchRemove(String qccPropertyName) {
			if (qccPropertyName == null || qccPropertyName.length() == 0)
				throw new IllegalArgumentException("string is null or empty");
			m_zsWatch.remove(qccPropertyName);
		}

		public Set<String> watchSubtract(Set<String> zsPropertyNames) {
			if (zsPropertyNames == null) throw new IllegalArgumentException("object is null");
			final Set<String> result = new HashSet<String>(zsPropertyNames);
			result.removeAll(m_zsWatch);
			return result;
		}

		public List<String> zlWatchAsc() {
			final List<String> zl = new ArrayList<String>(m_zsWatch);
			Collections.sort(zl);
			return zl;
		}
		public WatchSet() {
		}
		private final Set<String> m_zsWatch = new HashSet<String>();
	}
}
