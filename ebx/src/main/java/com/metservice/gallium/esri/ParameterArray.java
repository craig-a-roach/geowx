/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

/**
 * @author roach
 */
class ParameterArray {

	public boolean add(double d) {
		if (m_count < m_dvals.length) {
			m_dvals[m_count] = d;
			m_count++;
			return true;
		}
		return false;
	}

	public int count() {
		return m_count;
	}

	public double select(int index) {
		return select(index, 0.0);
	}

	public double select(int index, double defaultValue) {
		if (index >= 0 && index < m_count) return m_dvals[index];
		return defaultValue;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_count; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(m_dvals[i]);
		}
		return sb.toString();
	}

	public ParameterArray(int max) {
		m_dvals = new double[max];
	}
	private final double[] m_dvals;
	private int m_count;
}
