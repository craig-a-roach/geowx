/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.argon.ArgonNumber;

/**
 * @author roach
 */
public class TestUnit1Local {

	public static void main(String[] args) {
		final TestUnit1Local test = new TestUnit1Local();
		try {
			test.t50();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Test
	public void t50()
			throws Exception {
		final TestHelpPeer director = new TestHelpPeer("director", 9001);
		final TestHelpPeer agent = new TestHelpPeer("agent", 9010);
		final TestImpExchanger xda = new TestImpExchanger(director, true, true);
		final TestImpExchanger xad = new TestImpExchanger(agent, true, true);
		final TestImpHostPort hpd = new TestImpHostPort("localhost", director.listenPort);
		final TestImpHostPort hpa = new TestImpHostPort("localhost", agent.listenPort);
		director.start();
		agent.start();
		try {
			final ExecutorService xc = Executors.newCachedThreadPool();
			final PulseWorker wax = new PulseWorker(xad, hpd, "ax", 20, 100);
			final PulseWorker way = new PulseWorker(xad, hpd, "ay", 40, 300);
			final PulseWorker wdx = new PulseWorker(xda, hpa, "dx", 10, 200);
			final PulseWorker wdy = new PulseWorker(xda, hpa, "dy", 30, 400);
			final Future<?> fax = xc.submit(wax);
			final Future<?> fay = xc.submit(way);
			final Future<?> fdx = xc.submit(wdx);
			final Future<?> fdy = xc.submit(wdy);
			fax.get(wax.estSecs(), TimeUnit.SECONDS);
			fay.get(way.estSecs(), TimeUnit.SECONDS);
			fdx.get(wdx.estSecs(), TimeUnit.SECONDS);
			fdy.get(wdy.estSecs(), TimeUnit.SECONDS);
			xc.shutdown();
		} finally {
			agent.stop();
			director.stop();
		}
		Assert.assertEquals(0, xad.probe.countFail());
		Assert.assertEquals(0, xad.probe.countWarn());
		Assert.assertEquals(0, xda.probe.countFail());
		Assert.assertEquals(0, xda.probe.countWarn());
	}

	private static class PulseWorker implements Runnable {

		public int estSecs() {
			return (count * (500 + msPause)) / 1000;
		}

		@Override
		public void run() {
			try {
				for (int r = 0; r < count; r++) {
					final BerylliumPath uri = BerylliumPath.newAbsolute("json", key, ArgonNumber.intToDec5(r));
					final TestImpJsonTracker tracker = exchanger.sendPulse(dest, uri, r);
					Assert.assertTrue("Pulse received 5s", tracker.await(5));
					Assert.assertEquals("Pulse" + r, "{}", tracker.qResponse());
					Thread.sleep(msPause);
				}
			} catch (final InterruptedException ex) {
			}
			System.out.println("Worker " + key + " done");
		}

		public PulseWorker(TestImpExchanger exchanger, TestImpHostPort dest, String key, int count, int msPause) {
			this.exchanger = exchanger;
			this.dest = dest;
			this.key = key;
			this.count = count;
			this.msPause = msPause;
		}
		private final TestImpExchanger exchanger;
		private final TestImpHostPort dest;
		private final String key;
		private final int count;
		private final int msPause;
	}

}
