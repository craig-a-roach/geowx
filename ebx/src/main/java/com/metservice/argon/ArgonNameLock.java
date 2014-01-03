/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author roach
 */
public class ArgonNameLock {

	private CountDownLatch getLatch(String zcctwName) {
		m_lock.lock();
		try {
			final CountDownLatch oLatch = m_map.get(zcctwName);
			if (oLatch == null) {
				m_map.put(zcctwName, new CountDownLatch(1));
			}
			return oLatch;
		} finally {
			m_lock.unlock();
		}
	}

	private CountDownLatch removeLatch(String zcctwName) {
		m_lock.lock();
		try {
			return m_map.remove(zcctwName);
		} finally {
			m_lock.unlock();
		}
	}

	public void lock(String ozccName)
			throws InterruptedException {
		final String ztwName = ArgonText.ztw(ozccName);
		boolean acquired = false;
		while (!acquired) {
			final CountDownLatch oLatch = getLatch(ztwName);
			if (oLatch == null) {
				acquired = true;
			} else {
				oLatch.await();
			}
		}
	}

	public void unlock(String ozccName) {
		final String zcctwName = ArgonText.ztw(ozccName);
		final CountDownLatch oLatch = removeLatch(zcctwName);
		if (oLatch != null) {
			oLatch.countDown();
		}
	}

	public ArgonNameLock() {
		this(16);
	}

	public ArgonNameLock(int initialCap) {
		m_map = new HashMap<String, CountDownLatch>(initialCap);
	}

	private final Lock m_lock = new ReentrantLock();
	private final Map<String, CountDownLatch> m_map;

}
