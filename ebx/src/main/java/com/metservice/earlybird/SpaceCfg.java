/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.Ds;
import com.metservice.argon.management.ArgonProbeFilter;
import com.metservice.beryllium.BerylliumHttpConnectorType;

/**
 * @author roach
 */
class SpaceCfg {

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

	public int getKmlServiceMaxThreads() {
		m_rwLock.readLock().lock();
		try {
			return m_kmlServiceMaxThreads;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public int getKmlServiceMinThreads() {
		m_rwLock.readLock().lock();
		try {
			return m_kmlServiceMinThreads;
		} finally {
			m_rwLock.readLock().unlock();
		}
	}

	public BerylliumHttpConnectorType getServiceConnectorType() {
		m_rwLock.readLock().lock();
		try {
			return m_serviceConnectorType;
		} finally {
			m_rwLock.readLock().unlock();
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

	public void setKmlServiceMaxThreads(int count) {
		m_rwLock.writeLock().lock();
		try {
			m_kmlServiceMaxThreads = count;
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setKmlServiceMinThreads(int count) {
		m_rwLock.writeLock().lock();
		try {
			m_kmlServiceMinThreads = count;
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	public void setServiceConnectorType(BerylliumHttpConnectorType cxType)
			throws ArgonFormatException {
		if (cxType == null) throw new IllegalArgumentException("object is null");
		m_rwLock.writeLock().lock();
		try {
			m_serviceConnectorType = cxType;
		} finally {
			m_rwLock.writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("serviceConnectorType", m_serviceConnectorType);
		ds.a("kmlServiceMinThreads", m_kmlServiceMinThreads);
		ds.a("kmlServiceMaxThreads", m_kmlServiceMaxThreads);
		ds.a("filterCon", m_filterCon);
		ds.a("filterJmx", m_filterJmx);
		ds.a("filterLog", m_filterLog);
		return ds.ss();
	}

	public SpaceCfg() {
	}
	private final ReadWriteLock m_rwLock = new ReentrantReadWriteLock();
	private BerylliumHttpConnectorType m_serviceConnectorType = CStartupDefault.HttpConnectorType;
	private int m_kmlServiceMinThreads = CStartupDefault.KmlMinThreads;
	private int m_kmlServiceMaxThreads = CStartupDefault.KmlMaxThreads;
	private ArgonProbeFilter m_filterJmx = new ArgonProbeFilter(true, true, false, false);
	private ArgonProbeFilter m_filterLog = new ArgonProbeFilter(true, true, true, true);
	private ArgonProbeFilter m_filterCon = new ArgonProbeFilter(false, false, false, false);
}
