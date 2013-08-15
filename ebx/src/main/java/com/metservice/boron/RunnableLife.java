/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.util.concurrent.atomic.AtomicBoolean;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class RunnableLife extends ARunnable {

	@Override
	protected void doWork() {
		boolean ended = false;
		try {
			m_waiting.set(true);
			final int exitCode = m_process.waitFor();
			m_waiting.set(false);
			ended = true;
			m_productQueue.putProcessExit(exitCode);
		} catch (final InterruptedException ex) {
			m_productQueue.putProcessInterrupted();
		} catch (final RuntimeException exRT) {
			final Ds ds = Ds.triedTo("Wait for process to exit", exRT);
			ds.a("process", m_process);
			ds.a("productQueue", m_productQueue);
			kc.probe.failSoftware(ds);
			m_productQueue.putProcessExitWaitFailed();
		} finally {
			if (!ended) {
				UBoron.destroyProcess(m_process);
				m_destroyRequested.set(true);
			}
		}
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("waiting", m_waiting);
		ds.a("destroyRequested", m_destroyRequested);
		return ds.s();
	}

	public RunnableLife(KernelCfg kc, Process process, ProductQueue productQueue) {
		super(kc);
		if (process == null) throw new IllegalArgumentException("object is null");
		if (productQueue == null) throw new IllegalArgumentException("object is null");
		m_process = process;
		m_productQueue = productQueue;
		m_waiting = new AtomicBoolean(false);
		m_destroyRequested = new AtomicBoolean(false);
	}

	private final Process m_process;
	private final ProductQueue m_productQueue;
	private final AtomicBoolean m_waiting;
	private final AtomicBoolean m_destroyRequested;
}
