/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

public class BzeStrikeBounds {

	private static float snapLo(float v, float grid) {
		if (v < 0.0f) return snapPosHi()
	}

	private static float snapPosHi(float vp, float grid) {
		return snapPosLo(vp * (grid + 0.5f), grid);
	}

	private static float snapPosLo(float vp, float grid) {
		final int vg = (int) (vp / grid);
		return vg * grid;
	}

	public static BzeStrikeBounds newInstance(BzeStrike[] strikes) {
		assert strikes != null;
		final int strikeCount = strikes.length;
		assert strikeCount > 0;
		final BzeStrike s0 = strikes[0];
		float yB = s0.y, xL = s0.x;
		float yT = yB, xR = xL;
		for (int i = 1; i < strikeCount; i++) {
			final BzeStrike strike = strikes[i];
			final float sy = strike.y;
			final float sx = strike.x;
			if (sy < yB) {
				yB = sy;
			}
			if (sx < xL) {
				xL = sx;
			}
			if (sy > yT) {
				yT = sy;
			}
			if (sx > xR) {
				xR = sx;
			}
		}
		return new BzeStrikeBounds(yB, xL, yT, xR);
	}

	public static BzeStrikeBounds newInstance(BzeStrike[] strikes, float grid) {
		final BzeStrikeBounds b = newInstance(strikes);
		final float syB = snapLo(b.yB, grid);
		final float sxL = snapLo(b.xL, grid);
		final float syT = snapHi(b.yT, grid);
		final float sxR = snapHi(b.xR, grid);
		return new BzeStrikeBounds(syB, sxL, syT, sxR);
	}

	public boolean contains(float y, float x) {
		return y >= yB && y <= yT && x >= xL && x <= xR;
	}

	public float height() {
		return yT - yB;
	}

	public int heightGrid(float grid) {
		return (int) (height() / grid);
	}

	public boolean intersects(BzeStrikeBounds rhs) {
		return rhs.yB <= yT && rhs.yT >= yB && rhs.xL <= xR && rhs.xR >= xL;
	}

	public float maxDimension() {
		return Math.max(width(), height());
	}

	@Override
	public String toString() {
		return "yB=" + yB + ", xL=" + xL + ", yT=" + yT + ", xR=" + xR;
	}

	public float width() {
		return xR - xL;
	}

	public int widthGrid(float grid) {
		return (int) (width() / grid);
	}

	public float xM() {
		return xL + ((xR - xL) / 2.0f);
	}

	public float yM() {
		return yB + ((yT - yB) / 2.0f);
	}

	public BzeStrikeBounds(float yB, float xL, float yT, float xR) {
		this.yB = yB;
		this.xL = xL;
		this.yT = yT;
		this.xR = xR;
	}
	public final float yB;
	public final float xL;
	public final float yT;
	public final float xR;
}