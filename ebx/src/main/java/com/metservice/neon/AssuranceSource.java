/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class AssuranceSource {

	private static final String StateUnavailable = "Unavailable";
	private static final String StateRunning = "\u27a8Running";
	private static final String StateCancelled = "Cancelled";
	private static final String StateError = "Error";

	public String qState() {
		m_rwlock.readLock().lock();
		try {
			if (m_oFuture == null) return StateUnavailable;
			if (m_oFuture.isCancelled()) return StateCancelled;
			if (!m_oFuture.isDone()) return StateRunning;
			try {
				final AssuranceRunReport report = m_oFuture.get();
				return report.qFlag() + report.qState();
			} catch (final InterruptedException ex) {
				return StateCancelled;
			} catch (final ExecutionException ex) {
				return StateError;
			}
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public void setRunningState(Future<AssuranceRunReport> future) {
		if (future == null) throw new IllegalArgumentException("object is null");
		m_rwlock.writeLock().lock();
		try {
			m_oFuture = future;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void setUnavailableState() {
		m_rwlock.writeLock().lock();
		try {
			m_oFuture = null;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		return qState();
	}

	public String ztwReport() {
		m_rwlock.readLock().lock();
		try {
			if (m_oFuture == null || m_oFuture.isCancelled() || !m_oFuture.isDone()) return "";
			try {
				final AssuranceRunReport report = m_oFuture.get();
				return report.ztwReport();
			} catch (final InterruptedException ex) {
				return "";
			} catch (final ExecutionException ex) {
				return Ds.format(ex);
			}

		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public AssuranceSource(String qccSourcePath) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		this.qccSourcePath = qccSourcePath;
	}
	public final String qccSourcePath;
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private Future<AssuranceRunReport> m_oFuture;
}
