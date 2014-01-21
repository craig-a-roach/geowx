/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

import java.util.Arrays;

import com.metservice.argon.ArgonCompare;

/**
 * @author roach
 */
public class GalliumPoly implements Comparable<GalliumPoly> {

	private static final int InitCap = 40;
	private static final int CapMin = 2;
	private static final int ExpandMax = 1000;

	private static GalliumBoundingBoxF newBounds(float[] xptCoords, int card) {
		assert xptCoords != null;
		assert card >= 2;
		final GalliumBoundingBoxF.BuilderF builder = GalliumBoundingBoxF.newBuilderF();
		builder.init(xptCoords[0], xptCoords[1]);
		for (int ip = 1, iy = 2, ix = 3; ip < card; ip++, iy += 2, ix += 2) {
			builder.add(xptCoords[iy], xptCoords[ix]);
		}
		return GalliumBoundingBoxF.newInstance(builder);
	}

	public static GalliumPoly createInstance(Builder builder) {
		if (builder == null) throw new IllegalArgumentException("object is null");
		final float[] oxptCoordsImmutable = builder.ox4ptCoordsImmutable();
		if (oxptCoordsImmutable == null) return null;
		final boolean isClosed = builder.isClosed();
		return newYX(oxptCoordsImmutable, isClosed);
	}

	public static Builder newBuilder() {
		return newBuilder(InitCap);
	}

	public static Builder newBuilder(int initPointCap) {
		final int initCap = initPointCap << 1;
		return new Builder(Math.max(CapMin, initCap));
	}

	public static GalliumPoly newInstance(Builder builder) {
		if (builder == null) throw new IllegalArgumentException("object is null");
		final float[] oxptCoordsImmutable = builder.ox4ptCoordsImmutable();
		if (oxptCoordsImmutable == null) {
			final String m = "require 4 or more coords, but only " + builder.m_count;
			throw new IllegalArgumentException(m);
		}
		final boolean isClosed = builder.isClosed();
		return newYX(oxptCoordsImmutable, isClosed);
	}

	public static GalliumPoly newYX(float[] xptCoordsImmutable, boolean isClosed) {
		if (xptCoordsImmutable == null || xptCoordsImmutable.length == 0)
			throw new IllegalArgumentException("array is null or empty");
		final int len = xptCoordsImmutable.length;
		final int card = len / 2;
		final int min = isClosed ? 3 : 2;
		if (card < min) throw new IllegalArgumentException("require " + min + " or more coords, but only " + len);
		final GalliumBoundingBoxF bounds = newBounds(xptCoordsImmutable, card);
		return new GalliumPoly(xptCoordsImmutable, card, isClosed, bounds);
	}

	public GalliumBoundingBoxF bounds() {
		return m_bounds;
	}

	@Override
	public int compareTo(GalliumPoly rhs) {
		final int c0 = ArgonCompare.rev(m_card, rhs.m_card);
		if (c0 != 0) return c0;
		final int c1 = m_bounds.compareTo(rhs.m_bounds);
		if (c1 != 0) return c1;
		final int lhsC = m_x2ptCoordsYX.length;
		final int rhsC = rhs.m_x2ptCoordsYX.length;
		final int c2 = ArgonCompare.rev(lhsC, rhsC);
		if (c2 != 0) return c2;
		for (int i = 0; i < lhsC; i++) {
			final int c3 = ArgonCompare.fwd(m_x2ptCoordsYX[i], rhs.m_x2ptCoordsYX[i]);
			if (c3 != 0) return c3;
		}
		return 0;
	}

	public Polygon createPolygon() {
		if (m_isClosed) return new Polygon();
		return null;
	}

	public String describePoints() {
		final StringBuilder sb = new StringBuilder();
		for (int ip = 0, iy = 0, ix = 1; ip < m_card; ip++, iy += 2, ix += 2) {
			sb.append('(');
			sb.append(m_x2ptCoordsYX[iy]);
			sb.append(',');
			sb.append(m_x2ptCoordsYX[ix]);
			sb.append(')');
		}
		if (m_isClosed) {
			sb.append("CLOSE");
		}
		return sb.toString();
	}

	public Polygon newPolygon() {
		final Polygon oNeo = createPolygon();
		if (oNeo == null) throw new IllegalStateException("polyline not closed");
		return oNeo;
	}

	public int pointCount() {
		return m_card;
	}

	public float pointX(int pointIndex) {
		final int coordIndex = pointIndex << 1;
		return m_x2ptCoordsYX[coordIndex + 1];
	}

	public float pointY(int pointIndex) {
		final int coordIndex = pointIndex << 1;
		return m_x2ptCoordsYX[coordIndex];
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("points=").append(m_card).append(" ");
		sb.append(m_isClosed ? "closed" : "open");
		sb.append(" bounds=(").append(m_bounds).append(")");
		return sb.toString();
	}

	private GalliumPoly(float[] x2ptCoordsYX, int card, boolean isClosed, GalliumBoundingBoxF bounds) {
		assert x2ptCoordsYX != null;
		m_x2ptCoordsYX = x2ptCoordsYX;
		m_card = card;
		m_isClosed = isClosed;
		m_bounds = bounds;
	}

	private final float[] m_x2ptCoordsYX;
	private final int m_card;
	private final boolean m_isClosed;
	private final GalliumBoundingBoxF m_bounds;

	public static class Builder {

		private void ensure(int reqdGrowth) {
			final int neoCount = m_count + reqdGrowth;
			final int exCap = m_yx.length;
			if (neoCount <= exCap) return;
			final int estGrowth = (exCap * 3 / 2) - exCap;
			final int extendBy = Math.min(ExpandMax, estGrowth);
			final int neoCap = Math.max(neoCount, exCap + extendBy);
			m_yx = Arrays.copyOf(m_yx, neoCap);
		}

		private void swap(int i, int j) {
			final float tmp = m_yx[i];
			m_yx[i] = m_yx[j];
			m_yx[j] = tmp;
		}

		public void addTail(Builder tail) {
			if (tail == null) throw new IllegalArgumentException("object is null");
			final int rhsCount = tail.m_count;
			ensure(rhsCount);
			System.arraycopy(tail.m_yx, 0, m_yx, m_count, rhsCount);
			m_count += rhsCount;
		}

		public void addTail(float y, float x) {
			ensure(2);
			m_yx[m_count] = y;
			m_yx[m_count + 1] = x;
			m_count += 2;
		}

		public void insertHead(Builder head) {
			if (head == null) throw new IllegalArgumentException("object is null");
			final int lhsCount = head.m_count;
			ensure(lhsCount);
			System.arraycopy(m_yx, 0, m_yx, lhsCount, m_count);
			System.arraycopy(head.m_yx, 0, m_yx, 0, lhsCount);
			m_count += lhsCount;
		}

		public boolean isClosed() {
			return m_isClosed;
		}

		public Builder newMutableClone() {
			final Builder neo = new Builder(m_count);
			System.arraycopy(m_yx, 0, neo.m_yx, 0, m_count);
			neo.m_count = m_count;
			neo.m_isClosed = m_isClosed;
			return neo;
		}

		public float[] ox4ptCoordsImmutable() {
			if (m_count < 4) return null;
			final int cap = m_yx.length;
			if (cap == m_count) return m_yx;
			return Arrays.copyOfRange(m_yx, 0, m_count);
		}

		public void reverse() {
			final int mid = m_count >> 1;
			for (int i = 0, j = m_count - 2; i < mid; i += 2, j -= 2) {
				swap(i, j);
				swap(i + 1, j + 1);
			}
		}

		public void setClosed() {
			m_isClosed = true;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("yx");
			for (int i = 1; i <= m_count; i += 2) {
				sb.append('(').append(m_yx[i - 1]).append(',').append(m_yx[i]).append(')');
			}
			sb.append(m_isClosed ? "CLOSED" : "OPEN");
			return sb.toString();
		}

		private Builder(int initCap) {
			m_yx = new float[initCap];
		}
		private float[] m_yx;
		private int m_count;
		private boolean m_isClosed;
	}

	public class Polygon {

		public boolean contains(float y, float x) {
			if (!m_bounds.contains(y, x)) return false;
			final int n = m_x2ptCoordsYX.length;
			int ie = 0;
			int iy = n - 2, ix = n - 1;
			float headY = m_x2ptCoordsYX[iy], tailY = headY;
			float headX = m_x2ptCoordsYX[ix], tailX = headX;
			int hits = 0;
			for (iy = 0, ix = 1; ie < m_card; ie++, iy += 2, ix += 2, tailY = headY, tailX = headX) {
				headY = m_x2ptCoordsYX[iy];
				headX = m_x2ptCoordsYX[ix];
				if (headY == tailY) {
					continue;
				}
				final float leftX;
				if (headX < tailX) {
					if (x >= tailX) {
						continue;
					}
					leftX = headX;
				} else {
					if (x >= headX) {
						continue;
					}
					leftX = tailX;
				}
				final float dX, dY;
				if (headY < tailY) {
					if (y < headY || y >= tailY) {
						continue;
					}
					if (x < leftX) {
						hits++;
						continue;
					}
					dX = x - headX;
					dY = y - headY;
				} else {
					if (y < tailY || y >= headY) {
						continue;
					}
					if (x < leftX) {
						hits++;
						continue;
					}
					dX = x - tailX;
					dY = y - tailY;
				}
				final float crossX = dY / (tailY - headY) * (tailX - headX);
				if (dX < crossX) {
					hits++;
				}
			}

			return ((hits & 1) != 0);
		}

		private Polygon() {
		}
	}
}
