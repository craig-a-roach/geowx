/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.Ds;
import com.metservice.argon.management.ArgonProbeFilter;

/**
 * @author roach
 */
public class BoronSpaceCfg {

	public void addInterpreter(IBoronScriptInterpreter si) {
		if (si == null) throw new IllegalArgumentException("object is null");
		m_rwLock.writeLock().lock();
		try {
			m_zmId_Interpreter.put(si.id(), si);
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void clearInterpreter(BoronInterpreterId id) {
		if (id == null) throw new IllegalArgumentException("object is null");
		m_rwLock.writeLock().lock();
		try {
			m_zmId_Interpreter.remove(id);
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public ArgonProbeFilter filterCon() {
		m_rwLock.readLock().lock();
		try {
			return m_filterCon;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public ArgonProbeFilter filterJmx() {
		m_rwLock.readLock().lock();
		try {
			return m_filterJmx;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public ArgonProbeFilter filterLog() {
		m_rwLock.readLock().lock();
		try {
			return m_filterLog;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public IBoronScriptInterpreter findInterpreter(BoronInterpreterId interpreterId) {
		if (interpreterId == null) throw new IllegalArgumentException("object is null");
		m_rwLock.writeLock().lock();
		try {
			return m_zmId_Interpreter.get(interpreterId);
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public String getBase() {
		m_rwLock.readLock().lock();
		try {
			return m_ozBase;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public int getCooldownSecs() {
		m_rwLock.readLock().lock();
		try {
			return m_cooldownSecs;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public int getWorkHistoryDepth() {
		m_rwLock.readLock().lock();
		try {
			return m_workHistoryDepth;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public boolean isWinOS() {
		return m_isWinOS;
	}

	public void setBase(String ozPath) {
		m_rwLock.writeLock().lock();
		try {
			m_ozBase = ozPath;
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setCooldownSecs(int secs) {
		m_rwLock.writeLock().lock();
		try {
			m_cooldownSecs = Math.max(0, secs);
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setFilterPatternConsole(String ozFilterPattern)
			throws ArgonApiException {
		m_rwLock.writeLock().lock();
		try {
			m_filterCon = new ArgonProbeFilter(ozFilterPattern);
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setFilterPatternJmx(String ozFilterPattern)
			throws ArgonApiException {
		m_rwLock.writeLock().lock();
		try {
			m_filterJmx = new ArgonProbeFilter(ozFilterPattern);
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setFilterPatternLog(String ozFilterPattern)
			throws ArgonApiException {
		m_rwLock.writeLock().lock();
		try {
			m_filterLog = new ArgonProbeFilter(ozFilterPattern);
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setWorkHistoryDepth(int depth) {
		m_rwLock.writeLock().lock();
		try {
			m_workHistoryDepth = Math.max(0, depth);
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("base", m_ozBase);
		ds.a("workHistoryDepth", m_workHistoryDepth);
		ds.a("cooldownSecs", m_cooldownSecs);
		ds.a("filterCon", m_filterCon);
		ds.a("filterJmx", m_filterJmx);
		ds.a("filterLog", m_filterLog);
		return ds.s();
	}

	public BoronSpaceCfg() {
		m_zmId_Interpreter = new HashMap<BoronInterpreterId, IBoronScriptInterpreter>(16);
		final String idOsVer = UBoron.detectOsNameVersion();
		final IBoronScriptInterpreter[] Intrinsics;
		if (idOsVer.startsWith(UBoron.OSFAMILY_WIN)) {
			Intrinsics = new IBoronScriptInterpreter[] { new BoronInterpreterWinCmdDefault() };
			m_isWinOS = true;
		} else {
			Intrinsics = new IBoronScriptInterpreter[] { new BoronInterpreterBashDefault(),
					new BoronInterpreterPythonDefault() };
			m_isWinOS = false;
		}

		for (final IBoronScriptInterpreter si : Intrinsics) {
			m_zmId_Interpreter.put(si.id(), si);
		}
	}
	private final boolean m_isWinOS;
	private final ReadWriteLock m_rwLock = new ReentrantReadWriteLock();
	private String m_ozBase;
	private int m_cooldownSecs = CBoron.DefaultCooldownSecs;
	private int m_workHistoryDepth = CBoron.DefaultWorkHistoryDepth;
	private final Map<BoronInterpreterId, IBoronScriptInterpreter> m_zmId_Interpreter;
	private ArgonProbeFilter m_filterJmx = new ArgonProbeFilter(true, true, false, false);
	private ArgonProbeFilter m_filterLog = new ArgonProbeFilter(true, true, true, false);
	private ArgonProbeFilter m_filterCon = new ArgonProbeFilter(false, false, false, false);
}
