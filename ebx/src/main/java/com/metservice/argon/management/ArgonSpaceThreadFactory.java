/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author roach
 */
public class ArgonSpaceThreadFactory implements ThreadFactory {

	public ExecutorService newCachedThreadPool() {
		return Executors.newCachedThreadPool(this);
	}

	@Override
	public Thread newThread(Runnable r) {
		final String tname = m_qccThreadBody + "-" + m_suffix.getAndIncrement();
		return new Thread(r, tname);
	}

	public ArgonSpaceThreadFactory(String qccThreadPrefix, IArgonSpaceId id) {
		if (qccThreadPrefix == null || qccThreadPrefix.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (id == null) throw new IllegalArgumentException("object is null");
		m_qccThreadBody = qccThreadPrefix + id.format();
	}

	public ArgonSpaceThreadFactory(String qccThreadPrefix, String zccId) {
		if (qccThreadPrefix == null || qccThreadPrefix.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (zccId == null) throw new IllegalArgumentException("object is null");
		m_qccThreadBody = qccThreadPrefix + (zccId.length() == 0 ? "" : "-" + zccId);
	}

	private final String m_qccThreadBody;
	private final AtomicInteger m_suffix = new AtomicInteger(1);
}
