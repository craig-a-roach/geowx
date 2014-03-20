/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
abstract class AbstractStrikePolyline {

	abstract boolean isClosed();

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("xy");
		final int count = m_xy.length;
		for (int ix = 0, iy = 1; ix < count; ix += 2, iy += 2) {
			sb.append('(').append(m_xy[ix]).append(',').append(m_xy[iy]).append(')');
		}
		sb.append(isClosed() ? "CLOSED" : "OPEN");
		return sb.toString();
	}

	public int vertexCount() {
		return m_xy.length >> 1;
	}

	public float x(int vertexIndex) {
		return m_xy[vertexIndex << 1];
	}

	public float y(int vertexIndex) {
		return m_xy[(vertexIndex << 1) + 1];
	}

	public AbstractStrikePolyline(float[] xyPairs) {
		if (xyPairs == null || xyPairs.length < 4) throw new IllegalArgumentException("array is null");
		if (xyPairs.length < 4) throw new IllegalArgumentException("array is  malformed: " + xyPairs.length);
		m_xy = xyPairs;
	}
	private final float[] m_xy;
}
