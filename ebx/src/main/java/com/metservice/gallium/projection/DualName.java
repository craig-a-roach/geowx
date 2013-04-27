/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
class DualName implements Comparable<DualName> {

	public static DualName newInstance(String qccName) {
		if (qccName == null) throw new IllegalArgumentException("object is null");
		final String oqcctwFull = ArgonText.oqtw(qccName);
		if (oqcctwFull == null) throw new IllegalArgumentException("empty name");
		return new DualName(oqcctwFull, null);
	}

	public static DualName newInstance(String qccFull, String ozccShort) {
		if (qccFull == null) throw new IllegalArgumentException("object is null");
		final String oqcctwFull = ArgonText.oqtw(qccFull);
		if (oqcctwFull == null) throw new IllegalArgumentException("empty full name");
		final String oqcctwShort = ArgonText.oqtw(ozccShort);
		return new DualName(oqcctwFull, oqcctwShort);
	}

	@Override
	public int compareTo(DualName rhs) {
		return m_qcctwFull.compareTo(rhs.m_qcctwFull);
	}

	public boolean equals(DualName rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qcctwFull.equals(rhs.m_qcctwFull);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof DualName)) return false;
		return equals((DualName) o);
	}

	public boolean hasDistinctShortName() {
		return m_oqcctwShort != null;
	}

	@Override
	public int hashCode() {
		return m_qcctwFull.hashCode();
	}

	public String qcctwFullName() {
		return m_qcctwFull;
	}

	public String qcctwShortName() {
		return m_oqcctwShort == null ? m_qcctwFull : m_oqcctwShort;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_qcctwFull);
		if (m_oqcctwShort != null) {
			sb.append("(").append(m_oqcctwShort).append(")");
		}
		return sb.toString();
	}

	private DualName(String qcctwFull, String oqcctwShort) {
		assert qcctwFull != null && qcctwFull.length() > 0;
		m_qcctwFull = qcctwFull;
		m_oqcctwShort = oqcctwShort;
	}
	private final String m_qcctwFull;
	private final String m_oqcctwShort;
}
