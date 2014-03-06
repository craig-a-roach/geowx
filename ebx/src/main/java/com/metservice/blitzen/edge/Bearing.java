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

	private int indexAdjMinus() {
		return m_index == 7 ? 0 : m_index + 1;
	}

	private int indexAdjPlus() {
		return m_index == 0 ? 7 : m_index - 1;
	}

	public Bearing adjacentMinus() {
		return Paths[indexAdjMinus()];
	}

	public Bearing adjacentPlus() {
		return Paths[indexAdjPlus()];
	}

	public boolean isAdjacent(Bearing rhs) {
		return m_index == indexAdjMinus() || m_index == indexAdjPlus();
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