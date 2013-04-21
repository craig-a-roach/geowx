/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.management;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public class ArgonServiceSuite {

	private static String serviceName(IArgonService svc, int i, int c) {
		final String ord = Integer.toString(i + 1) + " of " + c;
		final String oqtwName = ArgonText.oqtw(svc.name());
		return oqtwName == null ? ord : oqtwName + " : " + ord;
	}

	public static ArgonServiceSuite newCachedThreadPool(IArgonServiceProbe oProbe, ArgonSpaceThreadFactory stf) {
		if (stf == null) throw new IllegalArgumentException("object is null");
		final ExecutorService xc = stf.newCachedThreadPool();
		return new ArgonServiceSuite(oProbe, xc);
	}

	public void endServices(Elapsed awaitExecutorService)
			throws InterruptedException {
		if (awaitExecutorService == null) throw new IllegalArgumentException("object is null");
		m_rwlock.readLock().lock();
		try {
			final int c = m_zlServices.size();
			for (int i = c - 1; i >= 0; i--) {
				final IArgonService svc = m_zlServices.get(i);
				final String serviceName = serviceName(svc, i, c);
				if (m_oProbe != null) {
					m_oProbe.infoShutdown(serviceName + " begin");
				}
				svc.serviceEnd();
				if (m_oProbe != null) {
					m_oProbe.infoShutdown(serviceName + " done");
				}
			}
		} finally {
			m_rwlock.readLock().unlock();
		}
		ArgonExecutorServiceTerminator.awaitTermination(m_oProbe, m_xc, awaitExecutorService);
	}

	public <S extends IArgonService> S register(S service) {
		if (service == null) throw new IllegalArgumentException("object is null");
		m_rwlock.writeLock().lock();
		try {
			m_zlServices.add(service);
			return service;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	public void startServices()
			throws ArgonPlatformException, InterruptedException {
		final int c = m_zlServices.size();
		for (int i = 0; i < c; i++) {
			final IArgonService svc = m_zlServices.get(i);
			final String serviceName = serviceName(svc, i, c);
			if (m_oProbe != null) {
				m_oProbe.infoStartup(serviceName + " begin");
			}
			svc.serviceStart(m_xc);
			if (m_oProbe != null) {
				m_oProbe.infoStartup(serviceName + " done");
			}
		}
	}

	private ArgonServiceSuite(IArgonServiceProbe oProbe, ExecutorService xc) {
		if (xc == null) throw new IllegalArgumentException("object is null");
		m_oProbe = oProbe;
		m_xc = xc;
	}
	private final IArgonServiceProbe m_oProbe;
	private final ExecutorService m_xc;
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	final List<IArgonService> m_zlServices = new ArrayList<IArgonService>(32);
}
