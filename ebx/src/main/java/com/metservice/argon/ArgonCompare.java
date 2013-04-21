/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class ArgonCompare {

	public static int fwd(boolean lhs, boolean rhs) {
		return (lhs == rhs ? 0 : (rhs ? -1 : +1));
	}

	public static int fwd(double lhs, double rhs) {
		if (lhs < rhs) return -1;
		if (lhs > rhs) return +1;
		final long blhs = Double.doubleToLongBits(lhs);
		final long brhs = Double.doubleToLongBits(rhs);
		if (blhs < brhs) return -1;
		if (blhs > brhs) return +1;
		return 0;
	}

	public static int fwd(float lhs, float rhs) {
		if (lhs < rhs) return -1;
		if (lhs > rhs) return +1;
		final int blhs = Float.floatToIntBits(lhs);
		final int brhs = Float.floatToIntBits(rhs);
		if (blhs < brhs) return -1;
		if (blhs > brhs) return +1;
		return 0;
	}

	public static int fwd(int lhs, int rhs) {
		if (lhs < rhs) return -1;
		if (lhs > rhs) return +1;
		return 0;
	}

	public static int fwd(long lhs, long rhs) {
		if (lhs < rhs) return -1;
		if (lhs > rhs) return +1;
		return 0;
	}

	public static int fwd(String olhs, String orhs) {
		if (olhs == null && orhs == null) return 0;
		if (olhs == null) return -1;
		if (orhs == null) return +1;
		return olhs.compareTo(orhs);
	}

	public static <T extends Comparable<? super T>> int fwdNull(T olhs, T orhs) {
		if (olhs == null && orhs == null) return 0;
		if (olhs == null) return -1;
		if (orhs == null) return +1;
		return olhs.compareTo(orhs);
	}

	public static int rev(boolean lhs, boolean rhs) {
		return (lhs == rhs ? 0 : (rhs ? +1 : -1));
	}

	public static int rev(double lhs, double rhs) {
		if (lhs < rhs) return +1;
		if (lhs > rhs) return -1;
		final long blhs = Double.doubleToLongBits(lhs);
		final long brhs = Double.doubleToLongBits(rhs);
		if (blhs < brhs) return +1;
		if (blhs > brhs) return -1;
		return 0;
	}

	public static int rev(float lhs, float rhs) {
		if (lhs < rhs) return +1;
		if (lhs > rhs) return -1;
		final int blhs = Float.floatToIntBits(lhs);
		final int brhs = Float.floatToIntBits(rhs);
		if (blhs < brhs) return +1;
		if (blhs > brhs) return -1;
		return 0;
	}

	public static int rev(int lhs, int rhs) {
		if (lhs < rhs) return +1;
		if (lhs > rhs) return -1;
		return 0;
	}

	public static int rev(long lhs, long rhs) {
		if (lhs < rhs) return +1;
		if (lhs > rhs) return -1;
		return 0;
	}

	public static int rev(String olhs, String orhs) {
		if (olhs == null && orhs == null) return 0;
		if (olhs == null) return +1;
		if (orhs == null) return -1;
		return -olhs.compareTo(orhs);
	}

	public static <T extends Comparable<? super T>> int revNull(T olhs, T orhs) {
		if (olhs == null && orhs == null) return 0;
		if (olhs == null) return +1;
		if (orhs == null) return -1;
		return -olhs.compareTo(orhs);
	}

	public static boolean similar(double lhs, double rhs) {
		final double epsilon = Math.max(Math.ulp(lhs), Math.ulp(rhs));
		return similar(lhs, rhs, epsilon);
	}

	public static boolean similar(double lhs, double rhs, double epsilon) {
		return Math.abs(rhs - lhs) <= epsilon;
	}

	public static boolean similar(float lhs, float rhs) {
		final float epsilon = Math.max(Math.ulp(lhs), Math.ulp(rhs));
		return similar(lhs, rhs, epsilon);
	}

	public static boolean similar(float lhs, float rhs, float epsilon) {
		return Math.abs(rhs - lhs) <= epsilon;
	}

	private ArgonCompare() {
	}

}
