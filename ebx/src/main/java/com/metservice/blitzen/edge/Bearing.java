/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

enum Bearing {

	N(0, 0, 1), NE(1, 1, 1), E(2, 1, 0), SE(3, 1, -1), S(4, 0, -1), SW(5, -1, -1), W(6, -1, 0), NW(7, -1, 1), STAY(8, 0, 0);

	private static final Bearing[] Paths = { N, NE, E, SE, S, SW, W, NW };
	public static final Bearing[] Polar = { N, NE, E, SE, S };

	public static Bearing select(int dx, int dy) {
		if (dx == 0) {
			if (dy == 0) return STAY;
			return dy < 0 ? S : N;
		}
		if (dx < 0) {
			if (dy == 0) return W;
			return dy < 0 ? SW : NW;
		}
		if (dy == 0) return E;
		return dy < 0 ? SE : NE;
	}

	public boolean isAdjacent(Bearing rhs, boolean orthogonal) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		final int offset = orthogonal ? 2 : 1;
		final int rhsIndex = rhs.m_index;
		final int lhsLo = (m_index + 8 - offset) % 8;
		final int lhsHi = (m_index + 8 + offset) % 8;
		if (lhsLo > lhsHi) return rhsIndex >= lhsLo || rhsIndex <= lhsHi;
		return rhsIndex >= lhsLo && rhsIndex <= lhsHi;
	}

	public Bearing path(int index) {
		final int neo = (m_index + index + 5) % 8;
		return Paths[neo];
	}

	private Bearing(int index, int dx, int dy) {
		m_index = index;
		this.dx = dx;
		this.dy = dy;
	}
	private final int m_index;
	public final int dx;
	public final int dy;
}