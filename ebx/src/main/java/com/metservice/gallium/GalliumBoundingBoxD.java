/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.CArgon;

/**
 * @author roach
 */
public class GalliumBoundingBoxD implements Comparable<GalliumBoundingBoxD> {

	public static GalliumBoundingBoxD createUnion(GalliumBoundingBoxD oLhs, GalliumBoundingBoxD oRhs) {
		if (oLhs == null) return oRhs;
		if (oRhs == null) return oLhs;
		return oLhs.newUnion(oRhs);
	}

	public static BuilderD newBuilderD() {
		return new BuilderD();
	}

	public static GalliumBoundingBoxD newCorners(double yLo, double xLo, double yHi, double xHi) {
		final double yHiClamped = yHi < yLo ? yLo : yHi;
		final double xHiClamped = xHi < xLo ? xLo : xHi;
		return new GalliumBoundingBoxD(yLo, xLo, yHiClamped, xHiClamped);
	}

	public static GalliumBoundingBoxD newDimensions(double yLo, double xLo, double height, double width) {
		return newCorners(yLo, xLo, yLo + height, xLo + width);
	}

	public static GalliumBoundingBoxD newInstance(BuilderD b) {
		if (b == null) throw new IllegalArgumentException("object is null");
		return newCorners(b.yLo, b.xLo, b.yHi, b.xHi);
	}

	@Override
	public int compareTo(GalliumBoundingBoxD rhs) {
		final int c0 = ArgonCompare.fwd(m_yLo, rhs.m_yLo);
		if (c0 != 0) return c0;
		final int c1 = ArgonCompare.fwd(m_xLo, rhs.m_xLo);
		if (c1 != 0) return c1;
		final int c2 = ArgonCompare.fwd(m_yHi, rhs.m_yHi);
		if (c2 != 0) return c2;
		final int c3 = ArgonCompare.fwd(m_xHi, rhs.m_xHi);
		return c3;
	}

	public boolean equals(GalliumBoundingBoxD rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_yLo == rhs.m_yLo && m_xLo == rhs.m_xLo && m_yHi == rhs.m_yHi && m_xHi == rhs.m_xHi;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof GalliumBoundingBoxD)) return false;
		return equals((GalliumBoundingBoxD) o);
	}

	@Override
	public int hashCode() {
		long bits = Double.doubleToLongBits(m_yLo);
		bits += java.lang.Double.doubleToLongBits(m_xLo) * 37;
		bits += java.lang.Double.doubleToLongBits(m_yHi) * 43;
		bits += java.lang.Double.doubleToLongBits(m_xHi) * 47;
		return (((int) bits) ^ ((int) (bits >> 32)));
	}

	public double height() {
		return m_yHi - m_yLo;
	}

	public boolean intersects(GalliumBoundingBoxD rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		return (rhs.m_xHi > m_xLo && rhs.m_yHi > m_yLo && rhs.m_xLo < m_xHi && rhs.m_yHi > m_yLo);
	}

	public GalliumBoundingBoxD newUnion(GalliumBoundingBoxD oRhs) {
		if (oRhs == null) return this;
		final double yLo = Math.min(m_yLo, oRhs.m_yLo);
		final double xLo = Math.min(m_xLo, oRhs.m_xLo);
		final double yHi = Math.max(m_yHi, oRhs.m_yHi);
		final double xHi = Math.max(m_xHi, oRhs.m_xHi);
		return new GalliumBoundingBoxD(yLo, xLo, yHi, xHi);
	}

	public boolean similarTo(GalliumBoundingBoxD rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (!ArgonCompare.similar(m_yLo, rhs.m_yLo)) return false;
		if (!ArgonCompare.similar(m_xLo, rhs.m_xLo)) return false;
		if (!ArgonCompare.similar(m_yHi, rhs.m_yHi)) return false;
		if (!ArgonCompare.similar(m_xHi, rhs.m_xHi)) return false;
		return true;
	}

	public boolean similarTo(GalliumBoundingBoxD rhs, float epsilon) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (!ArgonCompare.similar(m_yLo, rhs.m_yLo, epsilon)) return false;
		if (!ArgonCompare.similar(m_xLo, rhs.m_xLo, epsilon)) return false;
		if (!ArgonCompare.similar(m_yHi, rhs.m_yHi, epsilon)) return false;
		if (!ArgonCompare.similar(m_xHi, rhs.m_xHi, epsilon)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "yxLo=" + m_yLo + "," + m_xLo + " yxHi=" + m_yHi + "," + m_xHi;
	}

	public double width() {
		return m_xHi - m_xLo;
	}

	public double xHi() {
		return m_xHi;
	}

	public double xLo() {
		return m_xLo;
	}

	public double yHi() {
		return m_yHi;
	}

	public double yLo() {
		return m_yLo;
	}

	private GalliumBoundingBoxD(double yLo, double xLo, double yHi, double xHi) {
		m_yLo = yLo;
		m_xLo = xLo;
		m_yHi = yHi;
		m_xHi = xHi;
	}

	private final double m_yLo;
	private final double m_xLo;
	private final double m_yHi;
	private final double m_xHi;

	public static class BuilderD {

		public void add(double neoY, double neoX) {
			if (neoY < yLo) {
				yLo = neoY;
			}
			if (neoX < xLo) {
				xLo = neoX;
			}
			if (neoY > yHi) {
				yHi = neoY;
			}
			if (neoX > xHi) {
				xHi = neoX;
			}
		}

		public void init(double y, double x) {
			yLo = y;
			xLo = x;
			yHi = y;
			xHi = x;
		}

		public void initDimensions(double y, double x, double h, double w) {
			yLo = y;
			xLo = x;
			yHi = y + h;
			xHi = x + w;
		}

		@Override
		public String toString() {
			return "yxLo=" + yLo + "," + xLo + " yxHi=" + yHi + "," + xHi;
		}

		private BuilderD() {
		}
		private double yLo = CArgon.MIND_INIT;
		private double xLo = CArgon.MIND_INIT;
		private double yHi = CArgon.MAXD_INIT;
		private double xHi = CArgon.MAXD_INIT;
	}
}
