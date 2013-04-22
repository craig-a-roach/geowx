/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.HashCoder;

/**
 * @author roach
 */
public class CobaltDimensionSet implements ICobaltDimensionExpression {

	public static final CobaltDimensionSet Parameter = newUnary(CobaltDimensionName.Parameter);
	public static final CobaltDimensionSet Surface = newUnary(CobaltDimensionName.Surface);
	public static final CobaltDimensionSet Geography = newUnary(CobaltDimensionName.Geography);
	public static final CobaltDimensionSet Prognosis = newUnary(CobaltDimensionName.Prognosis);
	public static final CobaltDimensionSet Member = newUnary(CobaltDimensionName.Member);
	public static final CobaltDimensionSet Analysis = newUnary(CobaltDimensionName.Analysis);
	public static final CobaltDimensionSet Resolution = newUnary(CobaltDimensionName.Resolution);

	private int compareName(CobaltDimensionName rhs) {
		return 1;
	}

	private int compareSet(CobaltDimensionSet rhs) {
		final int llen = m_xptAsc.length;
		final int rlen = rhs.m_xptAsc.length;
		if (llen < rlen) return -1;
		if (llen > rlen) return +1;
		for (int i = 0; i < llen; i++) {
			final ICobaltDimensionExpression lexpr = m_xptAsc[i];
			final ICobaltDimensionExpression rexpr = rhs.m_xptAsc[i];
			final int cname = lexpr.compareTo(rexpr);
			if (cname != 0) return cname;
		}
		return 0;
	}

	void addTo(KmlFeatureDescription kfd) {
		kfd.addText(format());
	}

	public int cardinality() {
		return m_xptAsc.length;
	}

	@Override
	public int compareTo(ICobaltDimensionExpression rhs) {
		if (rhs instanceof CobaltDimensionSet) return compareSet((CobaltDimensionSet) rhs);
		if (rhs instanceof CobaltDimensionName) return compareName((CobaltDimensionName) rhs);
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	public boolean equals(CobaltDimensionSet rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		if (m_hashCode != rhs.m_hashCode) return false;
		if (m_xptAsc.length != rhs.m_xptAsc.length) return false;
		for (int i = 0; i < m_xptAsc.length; i++) {
			final ICobaltDimensionExpression lexpr = m_xptAsc[i];
			final ICobaltDimensionExpression rexpr = rhs.m_xptAsc[i];
			if (!lexpr.equals(rexpr)) return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltDimensionSet)) return false;
		return equals((CobaltDimensionSet) o);
	}

	@Override
	public String format() {
		final StringBuilder sb = new StringBuilder();
		final int cardinality = m_xptAsc.length;
		if (cardinality == 1) return m_xptAsc[0].format();
		sb.append('{');
		for (int i = 0; i < cardinality; i++) {
			if (i > 0) {
				sb.append('*');
			}
			sb.append(m_xptAsc[i].format());
		}
		sb.append('}');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return m_hashCode;
	}

	public boolean isUnaryName() {
		return m_xptAsc.length == 1 && (m_xptAsc[0] instanceof CobaltDimensionName);
	}

	@Override
	public String toString() {
		return format();
	}

	public ICobaltDimensionExpression[] xptExpressionAsc() {
		return m_xptAsc;
	}

	static CobaltDimensionSet newInstance(ICobaltDimensionExpression[] xptAsc) {
		if (xptAsc == null) throw new IllegalArgumentException("object is null");
		final int cardinality = xptAsc.length;
		if (cardinality == 0) throw new IllegalArgumentException("zero cardinality");
		return new CobaltDimensionSet(xptAsc);
	}

	public static CobaltDimensionSet newUnary(CobaltDimensionName name) {
		if (name == null) throw new IllegalArgumentException("object is null");
		final CobaltDimensionName[] xptAsc = { name };
		return new CobaltDimensionSet(xptAsc);
	}

	private CobaltDimensionSet(ICobaltDimensionExpression[] xptAsc) {
		assert xptAsc != null;
		m_xptAsc = xptAsc;
		int hc = HashCoder.INIT;
		for (int i = 0; i < xptAsc.length; i++) {
			hc = HashCoder.and(hc, xptAsc[i]);
		}
		m_hashCode = hc;
	}
	private final ICobaltDimensionExpression[] m_xptAsc;
	private final int m_hashCode;
}
