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
class ShellSessionStateConsole {

	public boolean absoluteTiming() {
		m_lock.lock();
		try {
			return m_absoluteTiming;
		} finally {
			m_lock.unlock();
		}
	}

	public void absoluteTiming(boolean enabled) {
		m_lock.lock();
		try {
			m_absoluteTiming = enabled;
		} finally {
			m_lock.unlock();
		}
	}

	public boolean listForward() {
		m_lock.lock();
		try {
			return m_listForward;
		} finally {
			m_lock.unlock();
		}
	}

	public void listForward(boolean enabled) {
		m_lock.lock();
		try {
			m_listForward = enabled;
		} finally {
			m_lock.unlock();
		}
	}

	public boolean showTrace() {
		m_lock.lock();
		try {
			return m_showTrace;
		} finally {
			m_lock.unlock();
		}
	}

	public void showTrace(boolean enabled) {
		m_lock.lock();
		try {
			m_showTrace = enabled;
		} finally {
			m_lock.unlock();
		}
	}

	public ShellSessionStateConsole() {
	}

	private final Lock m_lock = new ReentrantLock();
	private boolean m_absoluteTiming = false;
	private boolean m_listForward = false;
	private boolean m_showTrace = true;
}
