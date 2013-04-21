/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Ds;
import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public class ArgonExecutorServiceTerminator {

	private static String zRunnableClassNames(List<Runnable> zl) {
		assert zl != null;
		final StringBuilder runnableClasses = new StringBuilder();
		for (final Runnable runnable : zl) {
			ArgonText.append(runnableClasses, ", ", runnable.getClass().getName());
		}
		return runnableClasses.toString();
	}

	public static void awaitTermination(IArgonServiceProbe oProbe, ExecutorService xc, Elapsed interval)
			throws InterruptedException {
		if (xc == null) throw new IllegalArgumentException("object is null");
		if (interval == null) throw new IllegalArgumentException("object is null");
		xc.shutdown();
		final long msWait2 = interval.sms / 2;
		final boolean terminated = xc.awaitTermination(msWait2, TimeUnit.MILLISECONDS);
		if (terminated) {
			if (oProbe != null) {
				oProbe.infoShutdown("Executor service orderly shutdown complete");
			}
		} else {
			final List<Runnable> neverStarted = xc.shutdownNow();
			if (!neverStarted.isEmpty()) {
				if (oProbe != null) {
					final Ds g = Ds.invalidBecause("Some queued runnables not started", "Runnables discarded");
					g.a("initialWaitMs", msWait2);
					g.a("runnableClasses", zRunnableClassNames(neverStarted));
					oProbe.warnShutdown(g);
				}
			}
			final boolean terminated2 = xc.awaitTermination(msWait2, TimeUnit.MILLISECONDS);
			if (terminated2) {
				if (oProbe != null) {
					oProbe.infoShutdown("Executor service forced shutdown complete");
				}
			} else {
				if (oProbe != null) {
					final Ds g = Ds.invalidBecause("Some threads still running", "System.exit required");
					g.a("totalWaitMs", interval);
					oProbe.warnShutdown(g);
				}
			}
		}
	}

	private ArgonExecutorServiceTerminator() {
	}
}
