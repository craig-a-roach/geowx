/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.util.Comparator;

/**
 * @author roach
 */
class StrikeCluster {

	private static final int MinHullVertices = 10;
	private static final Comparator<Strike> XComparator = new Comparator<Strike>() {

		@Override
		public int compare(Strike lhs, Strike rhs) {
			if (lhs.x < rhs.x) return -1;
			if (lhs.x > rhs.x) return +1;
			return 0;
		}
	};

	private static Strike[] convexHull(Strike[] strikesAscX) {
		final int n = strikesAscX.length;
		if (n < MinHullVertices) return strikesAscX;
		final Strike[] upper = new Strike[n];
		upper[0] = strikesAscX[0];
		upper[1] = strikesAscX[1];
		int upperSize = 2;
		for (int i = 2; i < n; i++) {
			upper[upperSize] = strikesAscX[i];
			upperSize++;
			while (upperSize > 2 && !rightTurn(upper[upperSize - 3], upper[upperSize - 2], upper[upperSize - 1])) {
				upper[upperSize - 2] = upper[upperSize - 1];
				upperSize--;
			}
		}

		final Strike[] lower = new Strike[n];
		lower[0] = strikesAscX[n - 1];
		lower[1] = strikesAscX[n - 2];
		int lowerSize = 2;
		for (int i = n - 3; i >= 0; i--) {
			lower[lowerSize] = strikesAscX[i];
			lowerSize++;
			while (lowerSize > 2 && !rightTurn(lower[lowerSize - 3], lower[lowerSize - 2], lower[lowerSize - 1])) {
				lower[lowerSize - 2] = lower[lowerSize - 1];
				lowerSize--;
			}
		}

		final Strike[] result = new Strike[upperSize + lowerSize];
		int ri = 0;
		for (int i = 0; i < upperSize; i++) {
			result[ri] = upper[i];
			ri++;
		}
		for (int i = 0; i < lowerSize; i++) {
			result[ri] = lower[i];
			ri++;
		}
		return result;
	}

	private static boolean rightTurn(Strike a, Strike b, Strike c) {
		return ((b.x - a.x) * (c.y - a.y)) - ((b.y - a.y) * (c.x - a.x)) > 0;
	}

	public float qtyMagnitudeAverage() {
		return m_magSum / m_strikeCount;
	}

	public float qtyMagnitudeMax() {
		return m_magMax;
	}

	public float qtyMagnitudeSum() {
		return m_magSum;
	}

	public Strike[] strikeConvexHull() {
		final Strike[] strikesAscX = m_strikePolygon.newSortedVertexArray(XComparator);
		final Strike[] convexHull = convexHull(strikesAscX);
		return convexHull;
	}

	public StrikePolygon strikePolygon() {
		return m_strikePolygon;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("vertices=").append(m_strikePolygon.vertexCount());
		sb.append("strikes=").append(m_strikeCount);
		sb.append(", magSum=").append(m_magSum);
		sb.append(", magMax=").append(m_magMax);
		return sb.toString();
	}

	public StrikeCluster(StrikePolygon polygon, int strikeCount, float magSum, float magMax) {
		assert polygon != null;
		assert strikeCount > 0;
		m_strikePolygon = polygon;
		m_strikeCount = strikeCount;
		m_magSum = magSum;
		m_magMax = magMax;
	}
	private final StrikePolygon m_strikePolygon;
	private final int m_strikeCount;
	private final float m_magSum;
	private final float m_magMax;
}
