/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class ArgonSensorId {

	public boolean equals(ArgonSensorId rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qld.equals(rhs.m_qld);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof ArgonSensorId)) return false;
		return equals((ArgonSensorId) o);
	}

	@Override
	public int hashCode() {
		return m_hc;
	}

	@Override
	public String toString() {
		return m_qtw;
	}

	public ArgonSensorId(String qncId) {
		final String oqtw = ArgonText.oqtw(qncId);
		if (oqtw == null) throw new IllegalArgumentException("empty name");
		final String oqld = ArgonText.oqLettersAndDigits(oqtw);
		if (oqld == null) throw new IllegalArgumentException("id contains no letters or digits ");
		final String qucld = oqld.toUpperCase();
		m_qtw = oqtw;
		m_qld = qucld;
		m_hc = qucld.hashCode();
	}

	private final String m_qtw;
	private final String m_qld;
	private final int m_hc;
}
