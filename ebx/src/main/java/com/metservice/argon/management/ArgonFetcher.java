/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public abstract class ArgonFetcher<Q extends IArgonFetcherQuery, R> {

	protected abstract R getResponse(Q query)
			throws Exception;

	public R fetch(Q query)
			throws ExecutionException, InterruptedException {
		if (query == null) throw new IllegalArgumentException("object is null");
		final String ztwccSignature = ArgonText.ztw(query.getSignature());
		Tracker<R> vTracker = null;
		boolean primary = true;
		m_lock.lock();
		try {
			vTracker = m_tmap.get(ztwccSignature);
			if (vTracker == null) {
				vTracker = new Tracker<>();
				m_tmap.put(ztwccSignature, vTracker);
			} else {
				primary = false;
			}
		} finally {
			m_lock.unlock();
		}

		R oResponse = null;
		Exception oException = null;
		if (vTracker != null) {
			if (primary) {
				try {
					oResponse = getResponse(query);
				} catch (final Exception ex) {
					oException = ex;
				}
				vTracker.release(oResponse, oException);
				m_lock.lock();
				try {
					m_tmap.remove(ztwccSignature);
				} finally {
					m_lock.unlock();
				}
			} else {
				vTracker.await();
				oResponse = vTracker.getResponse();
				oException = vTracker.getException();
			}
		}
		if (oException != null) throw new ExecutionException(oException);
		return oResponse;
	}

	protected ArgonFetcher() {
	}
	private final Lock m_lock = new ReentrantLock();
	private final Map<String, Tracker<R>> m_tmap = new HashMap<String, Tracker<R>>();

	private static class Tracker<R> {

		public void await()
				throws InterruptedException {
			m_latch.await();
		}

		public Exception getException() {
			return m_exception.get();
		}

		public R getResponse() {
			return m_response.get();
		}

		public void release(R oResponse, Exception oException) {
			if (oException == null) {
				m_response.set(oResponse);
			} else {
				m_response.set(null);
				m_exception.set(oException);
			}
			m_latch.countDown();
		}

		Tracker() {
		}
		private final AtomicReference<R> m_response = new AtomicReference<>();
		private final AtomicReference<Exception> m_exception = new AtomicReference<>();
		private final CountDownLatch m_latch = new CountDownLatch(1);
	}

}
