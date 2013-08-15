/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.util.concurrent.atomic.AtomicBoolean;

import com.metservice.argon.ArgonClock;

/**
 * @author roach
 */
abstract class ACyclicRunnable extends ARunnable {

	private void yield(long tsCycleStart) {
		if (m_shutdown.get()) return;
		if (m_msMaxYield <= 0L) return;
		final long msSinceStart = ArgonClock.tsNow() - tsCycleStart;
		final long msSleep = m_msMaxYield - msSinceStart;
		if (msSleep <= 0L) return;
		try {
			Thread.sleep(msSleep);
		} catch (final InterruptedException exIR) {
			Thread.currentThread().interrupt();
		}
	}

	protected abstract void doCycle(long tsNow);

	@Override
	protected final void doWork() {
		while (!m_shutdown.get()) {
			final long tsCycleStart = ArgonClock.tsNow();
			doCycle(tsCycleStart);
			yield(tsCycleStart);
		}
	}

	public void end() {
		m_shutdown.set(true);
	}

	protected ACyclicRunnable(KernelCfg kc, int msMaxYield) {
		super(kc);
		m_msMaxYield = msMaxYield;
		m_shutdown = new AtomicBoolean(false);
	}
	private final int m_msMaxYield;
	private final AtomicBoolean m_shutdown;
}
