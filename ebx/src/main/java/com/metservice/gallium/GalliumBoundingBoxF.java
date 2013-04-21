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
public class GalliumBoundingBoxF implements Comparable<GalliumBoundingBoxF> {

	public static GalliumBoundingBoxF createUnion(GalliumBoundingBoxF oLhs, GalliumBoundingBoxF oRhs) {
		if (oLhs == null) return oRhs;
		if (oRhs == null) return oLhs;
		return oLhs.newUnion(oRhs);
	}

	public static BuilderD newBuilderD() {
		return new BuilderD();
	}

	public static BuilderF newBuilderF() {
		return new BuilderF();
	}

	public static GalliumBoundingBoxF newCorners(float yLo, float xLo, float yHi, float xHi) {
		final float yHiClamped = yHi < yLo ? yLo : yHi;
		final float xHiClamped = xHi < xLo ? xLo : xHi;
		return new GalliumBoundingBoxF(yLo, xLo, yHiClamped, xHiClamped);
	}

	public static GalliumBoundingBoxF newDimensions(float yLo, float xLo, float height, float width) {
		return newCorners(yLo, xLo, yLo + height, xLo + width);
	}

	public static GalliumBoundingBoxF newInstance(BuilderD b) {
		if (b == null) throw new IllegalArgumentException("object is null");
		return newCorners((float) b.yLo, (float) b.xLo, (float) b.yHi, (float) b.xHi);
	}

	public static GalliumBoundingBoxF newInstance(BuilderF b) {
		if (b == null) throw new IllegalArgumentException("object is null");
		return newCorners(b.yLo, b.xLo, b.yHi, b.xHi);
	}

	@Override
	public int compareTo(GalliumBoundingBoxF rhs) {
		final int c0 = ArgonCompare.fwd(m_yLo, rhs.m_yLo);
		if (c0 != 0) return c0;
		final int c1 = ArgonCompare.fwd(m_xLo, rhs.m_xLo);
		if (c1 != 0) return c1;
		final int c2 = ArgonCompare.fwd(m_yHi, rhs.m_yHi);
		if (c2 != 0) return c2;
		final int c3 = ArgonCompare.fwd(m_xHi, rhs.m_xHi);
		return c3;
	}

	public boolean equals(GalliumBoundingBoxF rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_yLo == rhs.m_yLo && m_xLo == rhs.m_xLo && m_yHi == rhs.m_yHi && m_xHi == rhs.m_xHi;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof GalliumBoundingBoxF)) return false;
		return equals((GalliumBoundingBoxF) o);
	}

	@Override
	public int hashCode() {
		int bits = Float.floatToIntBits(m_yLo);
		bits += Float.floatToIntBits(m_xLo) * 37;
		bits += Float.floatToIntBits(m_yHi) * 43;
		bits += Float.floatToIntBits(m_xHi) * 47;
		return bits;
	}

	public float height() {
		return m_yHi - m_yLo;
	}

	public GalliumBoundingBoxF newUnion(GalliumBoundingBoxF oRhs) {
		if (oRhs == null) return this;
		final float yLo = Math.min(m_yLo, oRhs.m_yLo);
		final float xLo = Math.min(m_xLo, oRhs.m_xLo);
		final float yHi = Math.max(m_yHi, oRhs.m_yHi);
		final float xHi = Math.max(m_xHi, oRhs.m_xHi);
		return new GalliumBoundingBoxF(yLo, xLo, yHi, xHi);
	}

	public boolean similarTo(GalliumBoundingBoxF rhs) {
		if (!ArgonCompare.similar(m_yLo, rhs.m_yLo)) return false;
		if (!ArgonCompare.similar(m_xLo, rhs.m_xLo)) return false;
		if (!ArgonCompare.similar(m_yHi, rhs.m_yHi)) return false;
		if (!ArgonCompare.similar(m_xHi, rhs.m_xHi)) return false;
		return true;
	}

	public boolean similarTo(GalliumBoundingBoxF rhs, float epsilon) {
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

	public float width() {
		return m_xHi - m_xLo;
	}

	public float xHi() {
		return m_xHi;
	}

	public float xLo() {
		return m_xLo;
	}

	public float yHi() {
		return m_yHi;
	}

	public float yLo() {
		return m_yLo;
	}

	private GalliumBoundingBoxF(float yLo, float xLo, float yHi, float xHi) {
		m_yLo = yLo;
		m_xLo = xLo;
		m_yHi = yHi;
		m_xHi = xHi;
	}

	private final float m_yLo;
	private final float m_xLo;
	private final float m_yHi;
	private final float m_xHi;

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

	public static class BuilderF {

		public void add(float neoY, float neoX) {
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

		public void add(GalliumBoundingBoxF rhs) {
			if (rhs == null) throw new IllegalArgumentException("object is null");
			if (rhs.m_yLo < yLo) {
				yLo = rhs.m_yLo;
			}
			if (rhs.m_xLo < xLo) {
				xLo = rhs.m_xLo;
			}
			if (rhs.m_yHi > yHi) {
				yHi = rhs.m_yHi;
			}
			if (rhs.m_xHi > xHi) {
				xHi = rhs.m_xHi;
			}
		}

		public void init(float y, float x) {
			yLo = y;
			xLo = x;
			yHi = y;
			xHi = x;
		}

		public void init(GalliumBoundingBoxF rhs) {
			if (rhs == null) throw new IllegalArgumentException("object is null");
			yLo = rhs.m_yLo;
			xLo = rhs.m_xLo;
			yHi = rhs.m_yHi;
			xHi = rhs.m_xHi;
		}

		public void initDimensions(float y, float x, float h, float w) {
			yLo = y;
			xLo = x;
			yHi = y + h;
			xHi = x + w;
		}

		@Override
		public String toString() {
			return "yxLo=" + yLo + "," + xLo + " yxHi=" + yHi + "," + xHi;
		}

		private BuilderF() {
		}
		private float yLo = CArgon.MINF_INIT;
		private float xLo = CArgon.MINF_INIT;
		private float yHi = CArgon.MAXF_INIT;
		private float xHi = CArgon.MAXF_INIT;
	}
}
