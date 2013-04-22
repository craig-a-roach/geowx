/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

/**
 * @author roach
 */
public class CobaltDimensionName implements ICobaltDimensionExpression {

	public static final CobaltDimensionName Parameter = new CobaltDimensionName("Parameter");
	public static final CobaltDimensionName Surface = new CobaltDimensionName("Surface");
	public static final CobaltDimensionName Geography = new CobaltDimensionName("Geography");
	public static final CobaltDimensionName Prognosis = new CobaltDimensionName("Prognosis");
	public static final CobaltDimensionName Member = new CobaltDimensionName("Member");
	public static final CobaltDimensionName Analysis = new CobaltDimensionName("Analysis");
	public static final CobaltDimensionName Resolution = new CobaltDimensionName("Resolution");

	private int compareName(CobaltDimensionName rhs) {
		return m_qcctw.compareTo(rhs.m_qcctw);
	}

	private int compareSet(CobaltDimensionSet rhs) {
		return -1;
	}

	@Override
	public int compareTo(ICobaltDimensionExpression rhs) {
		if (rhs instanceof CobaltDimensionName) return compareName((CobaltDimensionName) rhs);
		if (rhs instanceof CobaltDimensionSet) return compareSet((CobaltDimensionSet) rhs);
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	public boolean equals(CobaltDimensionName rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qcctw.equals(rhs.m_qcctw);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltDimensionName)) return false;
		return equals((CobaltDimensionName) o);
	}

	public String format() {
		return m_qcctw;
	}

	@Override
	public int hashCode() {
		return m_qcctw.hashCode();
	}

	@Override
	public String toString() {
		return format();
	}

	public static CobaltDimensionName newInstance(String qcctw) {
		if (qcctw == null) throw new IllegalArgumentException("object is null");
		final String zcctw = qcctw.trim();
		if (zcctw.length() == 0) throw new IllegalArgumentException("name is all whitespace");
		return new CobaltDimensionName(zcctw);
	}

	private CobaltDimensionName(String qcctw) {
		assert qcctw != null && qcctw.length() > 0;
		m_qcctw = qcctw;
	}

	private final String m_qcctw;
}
