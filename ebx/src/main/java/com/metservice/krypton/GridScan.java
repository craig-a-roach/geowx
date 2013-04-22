/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

class GridScan {

	public int arrayIndex(int i, int j) {
		final int ii = m_iWestEast ? i : m_nx - i + 1;
		final int jj = m_jSouthNorth ? j : m_ny - j + 1;
		final int k;
		if (m_iAdjacent) {
			k = ii + ((jj - 1) * m_nx) - 1;
		} else {
			k = ((ii - 1) * m_ny) + jj - 1;
		}
		return k;
	}

	public int eastNeighbour(int i) {
		return (i % m_nx) + 1;
	}

	public double elon(double lon1, double lon2) {
		return m_iWestEast ? lon2 : lon1;
	}

	public double nlat(double lon1, double lon2) {
		return m_jSouthNorth ? lon2 : lon1;
	}

	public int northNeighbour(int j) {
		return Math.min(m_ny, j + 1);
	}

	public int nx() {
		return m_nx;
	}

	public int ny() {
		return m_ny;
	}

	public double slat(double lon1, double lon2) {
		return m_jSouthNorth ? lon1 : lon2;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("arrayFactory", m_arrayFactory);
		ds.a("i-west-east", m_iWestEast);
		ds.a("j-south-north", m_jSouthNorth);
		ds.a("i-adjacent", m_iAdjacent);
		return ds.s();
	}

	public double wlon(double lon1, double lon2) {
		return m_iWestEast ? lon1 : lon2;
	}

	public GridScan(KryptonArrayFactory arrayFactory, int scanningMode) {
		if (arrayFactory == null) throw new IllegalArgumentException("object is null");
		m_arrayFactory = arrayFactory;
		m_nx = arrayFactory.nx();
		m_ny = arrayFactory.ny();
		m_iWestEast = (scanningMode & 0x80) != 0x80;
		m_jSouthNorth = (scanningMode & 0x40) == 0x40;
		m_iAdjacent = (scanningMode & 0x20) != 0x20;
	}
	private final KryptonArrayFactory m_arrayFactory;
	private final int m_nx;
	private final int m_ny;
	private final boolean m_iWestEast;
	private final boolean m_jSouthNorth;
	private final boolean m_iAdjacent;
}