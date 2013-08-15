/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class ProcessEngine implements Comparable<ProcessEngine> {

	public void cancel() {
		if (m_oThreadGroup != null) {
			m_oThreadGroup.cancel();
		}
	}

	public int compareTo(ProcessEngine rhs) {
		return m_processId.compareTo(rhs.m_processId);
	}

	public FeedQueue feedQueue() {
		return m_feedQueue;
	}

	public boolean isHalted() {
		return m_oThreadGroup == null || m_productQueue.isHalted();
	}

	public boolean isOverdue(long tsNow) {
		return m_oThreadGroup != null && m_oThreadGroup.isOverdue(tsNow);
	}

	public BoronProcessId processId() {
		return m_processId;
	}

	public ProductQueue productQueue() {
		return m_productQueue;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("processId", m_processId);
		ds.a("threadGroup", m_oThreadGroup);
		ds.a("feedQueue", m_feedQueue);
		ds.a("productQueue", m_productQueue);
		return ds.s();
	}

	public ProcessEngine(BoronProcessId processId, FeedQueue fq, ProductQueue pq, ProcessThreadGroup optg) {
		if (fq == null) throw new IllegalArgumentException("object is null");
		if (pq == null) throw new IllegalArgumentException("object is null");
		m_processId = processId;
		m_feedQueue = fq;
		m_productQueue = pq;
		m_oThreadGroup = optg;
	}

	private final BoronProcessId m_processId;
	private final FeedQueue m_feedQueue;
	private final ProductQueue m_productQueue;
	private final ProcessThreadGroup m_oThreadGroup;
}
