/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

class StrikeBounds {

	public static StrikeBounds newInstance(Strike[] strikes) {
		assert strikes != null;
		final int strikeCount = strikes.length;
		assert strikeCount > 0;
		final Strike s0 = strikes[0];
		float yB = s0.y, xL = s0.x;
		float yT = yB, xR = xL;
		for (int i = 1; i < strikeCount; i++) {
			final Strike strike = strikes[i];
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
		return new StrikeBounds(yB, xL, yT, xR);
	}

	public boolean contains(float y, float x) {
		return y >= yB && y <= yT && x >= xL && x <= xR;
	}

	public float height() {
		return yT - yB;
	}

	public boolean intersects(StrikeBounds rhs) {
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

	public float xM() {
		return xL + ((xR - xL) / 2.0f);
	}

	public float yM() {
		return yB + ((yT - yB) / 2.0f);
	}

	public StrikeBounds(float yB, float xL, float yT, float xR) {
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