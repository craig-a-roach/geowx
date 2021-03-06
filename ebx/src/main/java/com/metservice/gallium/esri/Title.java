/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
class Title implements Comparable<Title> {

	public static Title newInstance(String ncTitle) {
		if (ncTitle == null) throw new IllegalArgumentException("object is null");
		final String oqnctw = ArgonText.oqtw(ncTitle);
		if (oqnctw == null) throw new IllegalArgumentException("title is all whitespace");
		final int slen = oqnctw.length();
		final String oqword = slen == 1 ? oqnctw : ArgonText.oqLettersAndDigits(oqnctw);
		if (oqword == null) throw new IllegalArgumentException("title contains no letters or digits");
		return new Title(oqnctw, oqword.toUpperCase());
	}

	public static String oquctwKey(String oz) {
		final String oqcctw = ArgonText.oqtw(oz);
		return oqcctw == null ? null : oqcctw.toUpperCase();
	}

	@Override
	public int compareTo(Title rhs) {
		return m_quctw.compareTo(rhs.m_quctw);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof Title)) return false;
		return equals((Title) o);
	}

	public boolean equals(Title rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_quctw.equals(rhs.m_quctw);
	}

	@Override
	public int hashCode() {
		return m_quctw.hashCode();
	}

	public String quctwKey() {
		return m_quctw;
	}

	@Override
	public String toString() {
		return m_qnctw;
	}

	private Title(String qnctw, String quctw) {
		assert qnctw != null && qnctw.length() > 0;
		assert quctw != null && quctw.length() > 0;
		m_qnctw = qnctw;
		m_quctw = quctw;
	}
	private final String m_qnctw;
	private final String m_quctw;
}
