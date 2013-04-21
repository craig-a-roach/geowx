/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

import com.metservice.argon.ArgonCompare;

/**
 * @author roach
 */
public class GalliumPointD {

	public static Builder newBuilder() {
		return new Builder();
	}

	public double distance(double px, double py) {
		return Math.sqrt(distanceSq(px, py));
	}

	public double distance(GalliumPointD p) {
		return Math.sqrt(distanceSq(p));
	}

	public double distanceSq(double px, double py) {
		final double dx = px - x;
		final double dy = py - y;
		return (dx * dx) + (dy * dy);
	}

	public double distanceSq(GalliumPointD p) {
		if (p == null) throw new IllegalArgumentException("object is null");
		return distanceSq(p.x, p.y);
	}

	public boolean equals(GalliumPointD rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return x == rhs.x && y == rhs.y;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof GalliumPointD)) return false;
		return equals((GalliumPointD) o);
	}

	@Override
	public int hashCode() {
		long bits = java.lang.Double.doubleToLongBits(x);
		bits ^= java.lang.Double.doubleToLongBits(y) * 31;
		return (((int) bits) ^ ((int) (bits >> 32)));
	}

	public boolean similarTo(GalliumPointD rhs) {
		if (!ArgonCompare.similar(x, rhs.x)) return false;
		if (!ArgonCompare.similar(y, rhs.y)) return false;
		return true;
	}

	public boolean similarTo(GalliumPointD rhs, float epsilon) {
		if (!ArgonCompare.similar(x, rhs.x, epsilon)) return false;
		if (!ArgonCompare.similar(y, rhs.y, epsilon)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "x=" + x + ", y=" + y;
	}

	public GalliumPointD(Builder b) {
		if (b == null) throw new IllegalArgumentException("object is null");
		this.x = b.x;
		this.y = b.y;
	}

	public GalliumPointD(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public final double x;
	public final double y;

	public static class Builder {

		@Override
		public String toString() {
			return "x=" + x + ", y=" + y;
		}

		private Builder() {
		}
		public double x;
		public double y;
	}
}
