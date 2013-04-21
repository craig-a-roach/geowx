/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class ArgonTimedown {

	public boolean isExpired() {
		return m_tsExpire <= System.currentTimeMillis();
	}

	public long msRemaining() {
		return Math.max(0L, m_tsExpire - System.currentTimeMillis());
	}

	@Override
	public String toString() {
		return "[" + msRemaining() + "]ms";
	}

	public ArgonTimedown(long msQuota) {
		m_tsExpire = System.currentTimeMillis() + msQuota;
	}
	private final long m_tsExpire;
}
