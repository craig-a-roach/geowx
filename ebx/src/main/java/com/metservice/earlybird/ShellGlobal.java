/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
class ShellGlobal {

	public boolean awaitShutdown(long msAwait)
			throws InterruptedException {
		return m_shutdownLatch.await(msAwait, TimeUnit.MILLISECONDS);
	}

	public void notifyShutdown() {
		m_shutdownLatch.countDown();
	}

	public String oqtwStatusBar() {
		return m_statusBar.get();
	}

	public void setStatusBar(String ozMessage) {
		m_statusBar.set(ArgonText.oqtw(ozMessage));
	}

	public ShellGlobal(KernelCfg kc) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		this.kc = kc;
	}
	final KernelCfg kc;
	private final CountDownLatch m_shutdownLatch = new CountDownLatch(1);
	private final AtomicReference<String> m_statusBar = new AtomicReference<>();
}
