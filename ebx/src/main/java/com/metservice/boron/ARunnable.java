/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.util.concurrent.CountDownLatch;

/**
 * @author roach
 */
abstract class ARunnable implements Runnable {

	protected abstract void doWork();

	public void awaitStart()
			throws InterruptedException {
		m_startLatch.await();
	}

	@Override
	public final void run() {
		m_startLatch.countDown();
		try {
			doWork();
		} catch (final RuntimeException exRT) {
			kc.probe.failSoftware(exRT);
		}
	}

	protected ARunnable(KernelCfg kc) {
		assert kc != null;
		this.kc = kc;
		m_startLatch = new CountDownLatch(1);
	}

	final KernelCfg kc;
	private final CountDownLatch m_startLatch;
}
