/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author roach
 */
public abstract class ArgonRunnable implements Runnable {

	private static final String CsqRunFail = "Run failed; runnable will return, thread will return to pool";

	protected static final long tsNow() {
		return ArgonClock.tsNow();
	}

	protected abstract IArgonRunProbe getRunProbe();

	protected final boolean keepRunning() {
		return !m_endRequested.get();
	}

	protected abstract void runImp()
			throws InterruptedException;

	protected final void yield() {
		yield(10L);
	}

	protected final void yield(long ms) {
		try {
			Thread.sleep(Math.max(0L, ms));
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	public final void awaitEnd() {
		final Thread oThread = m_selfThread.get();
		if (oThread == null) return;
		try {
			m_endedLatch.await();
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	public final void awaitStartup()
			throws InterruptedException {
		m_startupLatch.await();
	}

	public final void executeAwait(ExecutorService xc)
			throws InterruptedException {
		if (xc == null) throw new IllegalArgumentException("object is null");
		xc.execute(this);
		awaitStartup();
	}

	public final Thread getThread() {
		return m_selfThread.get();
	}

	public void interruptThread() {
		final Thread oThread = m_selfThread.get();
		if (oThread != null) {
			oThread.interrupt();
		}
	}

	public final boolean isThreadAlive() {
		final Thread oThread = m_selfThread.get();
		return oThread != null && oThread.isAlive();
	}

	public final void requestEnd() {
		m_endRequested.set(true);
	}

	public final void requestEndInterrupt() {
		m_endRequested.set(true);
		interruptThread();
	}

	@Override
	public final void run() {
		m_selfThread.set(Thread.currentThread());
		m_startupLatch.countDown();
		try {
			runImp();
		} catch (final RuntimeException ex) {
			final IArgonRunProbe oProbe = getRunProbe();
			if (oProbe != null) {
				final Ds ds = Ds.triedTo("Run thread", ex, CsqRunFail);
				ds.a("thread", Thread.currentThread().getName());
				ds.a("self", toString());
				oProbe.failSoftware(ds);
			}
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		m_endedLatch.countDown();
	}

	protected ArgonRunnable() {
	}
	private final CountDownLatch m_startupLatch = new CountDownLatch(1);
	private final CountDownLatch m_endedLatch = new CountDownLatch(1);
	private final AtomicBoolean m_endRequested = new AtomicBoolean(false);
	private final AtomicReference<Thread> m_selfThread = new AtomicReference<Thread>();
}
