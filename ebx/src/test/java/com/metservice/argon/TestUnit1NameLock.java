/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1NameLock {

	@Test
	public void t20_uncontended()
			throws InterruptedException {

		int c = 0;
		final ArgonNameLock lock = new ArgonNameLock();
		lock.lock("X");
		try {
			c++;
		} finally {
			lock.unlock("X");
		}
		lock.lock("Y");
		try {
			c++;
		} finally {
			lock.unlock("Y");
		}
		lock.lock("X");
		try {
			c++;
		} finally {
			lock.unlock("X");
		}
		Assert.assertEquals("XYX", 3, c);
	}

	@Test
	public void t30_clean()
			throws InterruptedException {
		final State state = new State(5);
		final ExecutorService xc = Executors.newFixedThreadPool(5);
		xc.submit(new Worker(state, "A", 1));
		xc.submit(new Worker(state, "A", 3));
		xc.submit(new Worker(state, "X", 4));
		xc.submit(new Worker(state, "A", 2));
		xc.submit(new Worker(state, "X", 2));

		Assert.assertTrue("All workers started", state.awaitStarted(10));
		final long tsStart = System.currentTimeMillis();
		Assert.assertTrue("All workers done", state.awaitDone(20));
		final long ms = System.currentTimeMillis() - tsStart;
		Assert.assertTrue("Elapsed ~6secs -1+3 (" + ms + ")", (ms > 5000 && ms < 9000));
	}

	private static class State {

		public boolean awaitDone(int secs)
				throws InterruptedException {
			return m_done.await(secs, TimeUnit.SECONDS);
		}

		public boolean awaitStarted(int secs)
				throws InterruptedException {
			return m_start.await(secs, TimeUnit.SECONDS);
		}

		public void put(String name, int secs)
				throws InterruptedException {
			m_start.countDown();
			m_start.await();
			m_nlock.lock(name);
			try {
				Thread.sleep(1000L * secs);
			} finally {
				m_nlock.unlock(name);
			}
			m_done.countDown();
		}

		public State(int workers) {
			m_start = new CountDownLatch(workers);
			m_done = new CountDownLatch(workers);
		}

		private final CountDownLatch m_start;
		private final CountDownLatch m_done;
		private final ArgonNameLock m_nlock = new ArgonNameLock();
	}

	private static class Worker implements Runnable {

		@Override
		public void run() {
			try {
				m_state.put(m_key, m_secs);
			} catch (final InterruptedException ex) {
			}
		}

		public Worker(State state, String key, int secs) {
			m_state = state;
			m_key = key;
			m_secs = secs;
		}
		private final State m_state;
		private final String m_key;
		private final int m_secs;
	}

}
