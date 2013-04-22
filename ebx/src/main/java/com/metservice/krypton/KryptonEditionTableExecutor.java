/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * @author roach
 */
public class KryptonEditionTableExecutor<T extends IKryptonEditionTable<T>> {

	private void cancelFutures(List<Future<KryptonEditionResult<T>>> xlFutures) {
		for (final Future<KryptonEditionResult<T>> future : xlFutures) {
			future.cancel(true);
		}
	}

	public KryptonEditionResult<T> createResult(KryptonAbstractEditionTableFactory<T> task) {
		try {
			final long tsStart = System.currentTimeMillis();
			final KryptonEditionResult<T> rhs = task.call();
			rhs.manifestCounter().fileClockMs(System.currentTimeMillis() - tsStart);
			return rhs;
		} catch (final Exception ex) {
			probe.software("Execute callable edition table factory", "Skip task", ex);
			return null;
		}
	}

	public KryptonEditionResult<T> createResult(KryptonAbstractEditionTableFactory<T>[] tasks)
			throws InterruptedException {
		if (tasks == null) throw new IllegalArgumentException("object is null");
		if (tasks.length == 0) return null;
		if (tasks.length == 1) return createResult(tasks[0]);
		final long tsStart = System.currentTimeMillis();
		final List<Future<KryptonEditionResult<T>>> xlFutures = new ArrayList<>(tasks.length);
		for (int i = 0; i < tasks.length; i++) {
			final KryptonAbstractEditionTableFactory<T> tf = tasks[i];
			final Future<KryptonEditionResult<T>> future = m_xcs.submit(tf);
			xlFutures.add(future);
		}
		KryptonEditionResult<T> oRhs = null;
		try {
			for (int i = 0; i < tasks.length; i++) {
				final Future<KryptonEditionResult<T>> future = m_xcs.take();
				try {
					final KryptonEditionResult<T> oLhs = future.get();
					if (oLhs != null) {
						oRhs = oLhs.newSum(oRhs);
					}
				} catch (final ExecutionException ex) {
					probe.software("Execute callable edition table factory", "Skip task", ex);
				}
			}
		} finally {
			cancelFutures(xlFutures);
		}
		if (oRhs != null) {
			oRhs.manifestCounter().fileClockMs(System.currentTimeMillis() - tsStart);
		}
		return oRhs;
	}

	public KryptonEditionTableExecutor(IKryptonProbe probe, int nThreads) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		this.probe = probe;
		final ExecutorService xs = Executors.newFixedThreadPool(nThreads);
		m_xcs = new ExecutorCompletionService<>(xs);
	}
	private final IKryptonProbe probe;
	private final ExecutorCompletionService<KryptonEditionResult<T>> m_xcs;
}
