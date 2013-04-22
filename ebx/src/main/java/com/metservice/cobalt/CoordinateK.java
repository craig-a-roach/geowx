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
class CoordinateK implements Comparable<CoordinateK> {

	@Override
	public int compareTo(CoordinateK rhs) {
		final int clhs = m_coordinates.length;
		final int crhs = rhs.m_coordinates.length;
		if (clhs < crhs) return -1;
		if (clhs > crhs) return +1;
		for (int i = 0; i < clhs; i++) {
			final ICobaltCoordinate coordLhs = m_coordinates[i];
			final ICobaltCoordinate coordRhs = rhs.m_coordinates[i];
			final int cmp = coordLhs.compareTo(coordRhs);
			if (cmp != 0) return cmp;
		}
		return 0;
	}

	public <C extends ICobaltCoordinate> C coordinate(int index, Class<C> cclass) {
		return cclass.cast(m_coordinates[index]);
	}

	public boolean equals(CoordinateK rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		final int clhs = m_coordinates.length;
		final int crhs = rhs.m_coordinates.length;
		if (clhs != crhs) return false;
		for (int i = 0; i < clhs; i++) {
			final ICobaltCoordinate coordLhs = m_coordinates[i];
			final ICobaltCoordinate coordRhs = rhs.m_coordinates[i];
			if (!coordLhs.equals(coordRhs)) return false;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CoordinateK)) return false;
		return equals((CoordinateK) o);
	}

	@Override
	public int hashCode() {
		return m_hc;
	}

	public String show() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_coordinates.length; i++) {
			if (i > 0) {
				sb.append(" ");
			}
			sb.append(m_coordinates[i].show());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	public CoordinateK(ICobaltCoordinate... coordinates) {
		if (coordinates == null || coordinates.length == 0) throw new IllegalArgumentException("array is null or empty");
		this.m_coordinates = coordinates;
		int hc = HashCoder.INIT;
		for (int i = 0; i < coordinates.length; i++) {
			hc = HashCoder.and(hc, coordinates[i]);
		}
		m_hc = hc;
	}
	private final ICobaltCoordinate[] m_coordinates;
	private final int m_hc;
}
