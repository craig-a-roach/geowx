/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public abstract class BerylliumAbstractHttpTracker implements IBerylliumHttpTracker {

	private boolean awaitOutcome(Elapsed interval, boolean fireTooSlow)
			throws InterruptedException {
		assert interval != null;
		final long ms = interval.atLeast(0L);
		final boolean haveOutcome = m_outcomeLatch.await(ms, TimeUnit.MILLISECONDS);
		if (!haveOutcome && fireTooSlow) {
			final boolean exCancelled = isCancelled();
			if (!exCancelled) {
				onTooSlow(interval);
				m_cancelled.set(true);
				m_outcomeLatch.countDown();
			}
		}
		return haveOutcome;
	}

	protected final void haveOutcome() {
		m_outcomeLatch.countDown();
	}

	protected final boolean isCancelled() {
		return m_cancelled.get();
	}

	protected void onCompleteMalformedResponse()
			throws InterruptedException {
	}

	protected void onStatusUnexpected(int status)
			throws InterruptedException {
	}

	protected void onTooSlow(Elapsed expiry)
			throws InterruptedException {
	}

	protected void onUnresponsive()
			throws InterruptedException {
	}

	public final void awaitOutcome()
			throws InterruptedException {
		awaitOutcome(m_awaitInterval, true);
	}

	public final void awaitOutcome(Elapsed awaitInterval)
			throws InterruptedException {
		if (awaitInterval == null) throw new IllegalArgumentException("object is null");
		awaitOutcome(awaitInterval, true);
	}

	public final void raiseCompleteMalformedResponse()
			throws InterruptedException {
		if (!isCancelled()) {
			onCompleteMalformedResponse();
			haveOutcome();
		}
	}

	public final void raiseStatusUnexpected(int status)
			throws InterruptedException {
		if (!isCancelled()) {
			onStatusUnexpected(status);
			haveOutcome();
		}
	}

	public final void raiseUnresponsive()
			throws InterruptedException {
		if (!isCancelled()) {
			onUnresponsive();
			haveOutcome();
		}
	}

	public final boolean tryOutcome()
			throws InterruptedException {
		return awaitOutcome(m_awaitInterval, false);
	}

	public final boolean tryOutcome(Elapsed awaitInterval)
			throws InterruptedException {
		if (awaitInterval == null) throw new IllegalArgumentException("object is null");
		return awaitOutcome(awaitInterval, false);
	}

	protected BerylliumAbstractHttpTracker() {
		m_awaitInterval = Elapsed.Zero;
	}

	protected BerylliumAbstractHttpTracker(Elapsed awaitInterval) {
		if (awaitInterval == null) throw new IllegalArgumentException("object is null");
		m_awaitInterval = awaitInterval;
	}
	private final Elapsed m_awaitInterval;
	private final CountDownLatch m_outcomeLatch = new CountDownLatch(1);
	private final AtomicBoolean m_cancelled = new AtomicBoolean(false);
}
