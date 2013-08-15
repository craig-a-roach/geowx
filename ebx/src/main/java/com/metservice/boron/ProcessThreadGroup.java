/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.Ds;
import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
class ProcessThreadGroup {

	public void cancel() {
		m_stdIn.cancel();
		m_stdOut.cancel();
		m_stdErr.cancel();
		m_lockStart.lock();
		try {
			if (m_oStdInFuture != null) {
				m_oStdInFuture.cancel(true);
			}
			if (m_oStdOutFuture != null) {
				m_oStdOutFuture.cancel(true);
			}
			if (m_oStdErrFuture != null) {
				m_oStdErrFuture.cancel(true);
			}
			if (m_oLifeFuture != null) {
				m_oLifeFuture.cancel(true);
			}

		} finally {
			m_lockStart.unlock();
		}
		m_cancelled.set(true);
	}

	public boolean isOverdue(long tsNow) {
		if (m_oProcessExitTimeout == null) return false;
		final long tsExpiry = m_tsProcessStart + m_oProcessExitTimeout.sms;
		return tsExpiry <= tsNow;
	}

	public void start(ExecutorService execService)
			throws InterruptedException {
		if (execService == null) throw new IllegalArgumentException("object is null");
		m_lockStart.lockInterruptibly();
		try {
			m_oLifeFuture = execService.submit(m_life);
			m_life.awaitStart();
			m_oStdInFuture = execService.submit(m_stdIn);
			m_oStdOutFuture = execService.submit(m_stdOut);
			m_oStdErrFuture = execService.submit(m_stdErr);
			m_stdOut.awaitStart();
			m_stdErr.awaitStart();
		} finally {
			m_lockStart.unlock();
		}
		m_started.set(true);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.at8("tsProcessStart", m_tsProcessStart);
		ds.a("processExitTimeout", m_oProcessExitTimeout);
		ds.a("started", m_started);
		ds.a("cancelled", m_cancelled);
		ds.a("lifeRunnable", m_life);
		ds.a("stdOutRunnable", m_stdOut);
		ds.a("stdErrRunnable", m_stdErr);
		return ds.s();
	}

	public ProcessThreadGroup(IBoronScript script, RunnableLife life, RunnableIn stdIn, RunnableOut stdOut, RunnableOut stdErr,
			long tsStart) {
		if (script == null) throw new IllegalArgumentException("object is null");
		if (life == null) throw new IllegalArgumentException("object is null");
		if (stdIn == null) throw new IllegalArgumentException("object is null");
		if (stdOut == null) throw new IllegalArgumentException("object is null");
		if (stdErr == null) throw new IllegalArgumentException("object is null");

		m_life = life;
		m_stdIn = stdIn;
		m_stdOut = stdOut;
		m_stdErr = stdErr;
		m_tsProcessStart = tsStart;
		m_oProcessExitTimeout = script.getExitTimeout();
		m_started = new AtomicBoolean(false);
		m_cancelled = new AtomicBoolean(false);
	}

	private final RunnableLife m_life;
	private final RunnableIn m_stdIn;
	private final RunnableOut m_stdOut;
	private final RunnableOut m_stdErr;
	private final long m_tsProcessStart;
	private final Elapsed m_oProcessExitTimeout;
	private final AtomicBoolean m_started;
	private final AtomicBoolean m_cancelled;
	private final Lock m_lockStart = new ReentrantLock();
	private Future<?> m_oLifeFuture;
	private Future<?> m_oStdInFuture;
	private Future<?> m_oStdOutFuture;
	private Future<?> m_oStdErrFuture;
}
