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

import com.metservice.neon.NeonShell.EmitType;

/**
 * @author roach
 */
public class NeonConsole {

	private static ConsoleType findConsoleType(EmitType et) {
		switch (et) {
			case Fail:
				return ConsoleType.EmitFail;
			case Trace:
				return ConsoleType.EmitTrace;
			default:
				return null;
		}
	}

	private ConsoleSource declareSource(String qccSourcePath) {
		m_rwlock.writeLock().lock();
		try {
			ConsoleSource vSource = m_map.get(qccSourcePath);
			if (vSource == null) {
				final int quota = kc.cfg.getShellConsoleQuota();
				vSource = new ConsoleSource(qccSourcePath, quota);
				m_map.put(qccSourcePath, vSource);
			}
			return vSource;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void addEntry(String qccSourcePath, ConsoleEntry entry) {
		declareSource(qccSourcePath).addEntry(entry);
	}

	public void clear(String qccSourcePath) {
		final ConsoleSource oEx = findSource(qccSourcePath);
		if (oEx != null) {
			oEx.clear();
		}
	}

	public void failScriptRun(String qccSourcePath, String diagnostic) {
		final ConsoleEntry e = new ConsoleEntry(ConsoleType.RunFail, diagnostic);
		addEntry(qccSourcePath, e);
	}

	public ConsoleSource findSource(String qccSourcePath) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		m_rwlock.readLock().lock();
		try {
			return m_map.get(qccSourcePath);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public ConsoleEntry[] ozptEntries(String qccSourcePath, ConsoleFilter filter) {
		final ConsoleSource oEx = findSource(qccSourcePath);
		return oEx == null ? null : oEx.zptEntries(filter);
	}

	public void scriptEmit(String qccSourcePath, EmitType type, String message) {
		final ConsoleType oct = findConsoleType(type);
		if (oct != null) {
			final ConsoleEntry e = new ConsoleEntry(oct, message);
			addEntry(qccSourcePath, e);
		}
	}

	public List<String> zlqccSourcePathsAsc(boolean includeEmpty) {
		m_rwlock.readLock().lock();
		try {
			final List<String> zl = new ArrayList<String>();
			for (final String qccSourcePath : m_map.keySet()) {
				final ConsoleSource oSource = findSource(qccSourcePath);
				if (oSource != null) {
					if (includeEmpty || !oSource.isEmpty()) {
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

	public NeonConsole(KernelCfg kc) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		this.kc = kc;
	}
	final KernelCfg kc;
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private final Map<String, ConsoleSource> m_map = new HashMap<String, ConsoleSource>(8);
}
