/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.metservice.argon.Elapsed;
import com.metservice.argon.ElapsedFactory;

/**
 * @author roach
 */
public class BoronProductIterator {

	private static final String MsgExhausted = "Iterator has already reached end; test hasNext() before calling next()";

	private List<BoronFeedUnit> put(List<BoronFeedUnit> zlFeeds, Elapsed oTimeout)
			throws BoronApiException, InterruptedException {
		return m_space.putProcessFeeds(m_processId, zlFeeds, oTimeout);
	}

	public void cancelProcess() {
		m_space.removeProcess(m_processId);
	}

	public boolean hasNext() {
		return m_hasNext.get();
	}

	public IBoronProduct next()
			throws BoronApiException, InterruptedException {
		return next(null);
	}

	public IBoronProduct next(Elapsed oTimeout)
			throws BoronApiException, InterruptedException {
		if (!m_hasNext.get()) throw new BoronApiException(MsgExhausted);
		final IBoronProduct next = m_space.takeProcessProduct(m_processId, oTimeout);
		if (next.isTerminal()) {
			m_hasNext.set(false);
		}
		return next;
	}

	public IBoronProduct next(long duration, TimeUnit unit)
			throws BoronApiException, InterruptedException {
		return next(ElapsedFactory.newElapsed(duration, unit));
	}

	public List<IBoronProduct> nextPrompt()
			throws BoronApiException, InterruptedException {
		return nextPrompt(null);
	}

	public List<IBoronProduct> nextPrompt(Elapsed oTimeout)
			throws BoronApiException, InterruptedException {
		if (!m_hasNext.get()) throw new BoronApiException(MsgExhausted);
		final List<IBoronProduct> xl = new ArrayList<IBoronProduct>();
		boolean more = true;
		while (more) {
			final IBoronProduct next = m_space.takeProcessProduct(m_processId, oTimeout);
			xl.add(next);
			if (next.isTerminal()) {
				m_hasNext.set(false);
				more = false;
			} else {
				if (next instanceof BoronProductStreamLine) {
					final BoronProductStreamLine streamLine = (BoronProductStreamLine) next;
					if (streamLine.isPrompt()) {
						more = false;
					}
				} else {
					more = false;
				}
			}
		}
		return xl;
	}

	public List<IBoronProduct> nextPrompt(long duration, TimeUnit unit)
			throws BoronApiException, InterruptedException {
		final Elapsed timeout = ElapsedFactory.newElapsed(duration, unit);
		return nextPrompt(timeout);
	}

	public BoronProcessId processId() {
		return m_processId;
	}

	public List<BoronFeedUnit> put(List<BoronFeedUnit> zlFeeds)
			throws BoronApiException, InterruptedException {
		return put(zlFeeds, null);
	}

	public List<BoronFeedUnit> put(List<BoronFeedUnit> zlFeeds, long duration, TimeUnit unit)
			throws BoronApiException, InterruptedException {
		if (zlFeeds == null) throw new IllegalArgumentException("object is null");
		if (unit == null) throw new IllegalArgumentException("object is null");
		final Elapsed timeout = ElapsedFactory.newElapsed(duration, unit);
		return put(zlFeeds, timeout);
	}

	@Override
	public String toString() {
		return "hasNext=" + m_hasNext;
	}

	public BoronProductIterator(BoronSpace space, BoronProcessId processId) {
		if (space == null) throw new IllegalArgumentException("object is null");
		if (processId == null) throw new IllegalArgumentException("object is null");
		m_space = space;
		m_processId = processId;
		m_hasNext = new AtomicBoolean(true);
	}

	private final BoronSpace m_space;
	private final BoronProcessId m_processId;
	private final AtomicBoolean m_hasNext;
}
