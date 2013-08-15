/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.Ds;
import com.metservice.argon.management.ArgonProbeFilter;
import com.metservice.beryllium.BerylliumHttpConnectorType;

/**
 * @author roach
 */
public class NeonSpaceCfg {

	private Pattern createPatternFromSystemProperty(String qccSysPropName) {
		if (qccSysPropName == null || qccSysPropName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		try {
			final String oz = System.getProperty(qccSysPropName);
			if (oz == null || oz.length() == 0) return null;
			return Pattern.compile(oz);
		} catch (final PatternSyntaxException ex) {
			final String erm = ex.getMessage();
			final String m = "Value of system property '" + qccSysPropName + "' is not a valid regular expression..." + erm;
			throw new IllegalStateException(m);
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

	public Pattern getAutoDebugPattern() {
		m_rwLock.readLock().lock();
		try {
			return m_oAutoDebugPattern;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public int getCallableCacheLineBudget() {
		m_rwLock.readLock().lock();
		try {
			return m_callableCacheLineBudget;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public int getShellConsoleQuota() {
		m_rwLock.readLock().lock();
		try {
			return m_shellConsoleQuota;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public boolean getShellProcess() {
		m_rwLock.readLock().lock();
		try {
			return m_shellProcess;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public BerylliumHttpConnectorType getShellSessionConnectorType() {
		m_rwLock.readLock().lock();
		try {
			return m_shellSessionConnectorType;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public int getShellSessionMaxIdleSec() {
		m_rwLock.readLock().lock();
		try {
			return m_shellSessionMaxIdleSecs;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public void setAutoDebugPattern(Pattern oPattern) {
		m_rwLock.writeLock().lock();
		try {
			m_oAutoDebugPattern = oPattern;
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setAutoDebugPatternFromSystemProperty(String qccSysPropName) {
		setAutoDebugPattern(createPatternFromSystemProperty(qccSysPropName));
	}

	public void setCallableCacheLineBudget(int lineCount) {
		m_rwLock.writeLock().lock();
		try {
			m_callableCacheLineBudget = lineCount;
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

	public void setShellConsoleQuota(int entryCount) {
		m_rwLock.writeLock().lock();
		try {
			m_shellConsoleQuota = entryCount;
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setShellProcess(boolean enabled) {
		m_rwLock.writeLock().lock();
		try {
			m_shellProcess = enabled;
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setShellSessionConnectorType(BerylliumHttpConnectorType cxType) {
		if (cxType == null) throw new IllegalArgumentException("object is null");
		m_rwLock.writeLock().lock();
		try {
			m_shellSessionConnectorType = cxType;
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setShellSessionMaxIdleSec(int secs) {
		m_rwLock.writeLock().lock();
		try {
			m_shellSessionMaxIdleSecs = secs;
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("callableCacheLineBudget", m_callableCacheLineBudget);
		ds.a("shellSessionConnectorType", m_shellSessionConnectorType);
		ds.a("shellSessionMaxIdleSec", m_shellSessionMaxIdleSecs);
		ds.a("shellConsoleQuota", m_shellConsoleQuota);
		ds.a("autoDebugPattern", m_oAutoDebugPattern);
		ds.a("shellProcess", m_shellProcess);
		ds.a("filterCon", m_filterCon);
		ds.a("filterJmx", m_filterJmx);
		ds.a("filterLog", m_filterLog);
		return ds.ss();
	}

	public NeonSpaceCfg() {
	}
	private final ReadWriteLock m_rwLock = new ReentrantReadWriteLock();
	private int m_callableCacheLineBudget = CNeon.DefaultCallableCacheLineBudget;
	private BerylliumHttpConnectorType m_shellSessionConnectorType = CNeon.DefaultShellSessionConnectorType;
	private int m_shellSessionMaxIdleSecs = CNeon.DefaultShellSessionMaxIdleSecs;
	private int m_shellConsoleQuota = CNeon.DefaultShellConsoleQuota;
	private Pattern m_oAutoDebugPattern;
	private boolean m_shellProcess = CNeon.DefaultShellProcess;
	private ArgonProbeFilter m_filterJmx = new ArgonProbeFilter(true, true, false, false);
	private ArgonProbeFilter m_filterLog = new ArgonProbeFilter(true, true, true, true);
	private ArgonProbeFilter m_filterCon = new ArgonProbeFilter(false, false, false, false);
}
