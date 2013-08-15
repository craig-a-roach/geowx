/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author roach
 */
class ProfilerSessionSource {

	public void addSample(ProfileSample prf) {
		if (prf == null) throw new IllegalArgumentException("object is null");
		m_lockState.lock();
		try {
			if (m_oAggregate == null) {
				m_oAggregate = ProfileAggregate.newInitial(prf);
			} else {
				m_oAggregate = m_oAggregate.newAggregate(prf);
			}
		} finally {
			m_lockState.unlock();
		}
	}

	public ProfileAggregate getAggregate() {
		m_lockState.lock();
		try {
			return m_oAggregate;
		} finally {
			m_lockState.unlock();
		}
	}

	public ProfilerSessionSource(String qccSourcePath) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		this.qccSourcePath = qccSourcePath;
	}

	public final String qccSourcePath;
	private final Lock m_lockState = new ReentrantLock();
	private ProfileAggregate m_oAggregate;
}
