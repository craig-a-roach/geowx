/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonArray {

	private static final int DumpLimit = 10;

	private String dump(int limit) {
		final StringBuilder sb = new StringBuilder();
		final int len = m_values.length;
		final int count = Math.min(limit, len);
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(m_values[i]);
		}
		if (limit < len) {
			sb.append("...");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return dump(DumpLimit);
	}

	public float value(int arrayIndex) {
		if (arrayIndex < 0 || arrayIndex > m_values.length) return Float.NaN;
		return m_values[arrayIndex];
	}

	public KryptonArray(float[] values) {
		if (values == null) throw new IllegalArgumentException("object is null");
		m_values = values;
	}
	private final float[] m_values;
}
