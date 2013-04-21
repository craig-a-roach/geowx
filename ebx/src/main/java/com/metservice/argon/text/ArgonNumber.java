/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.text;

import com.metservice.argon.CArgon;

/**
 * @author roach
 */
public class ArgonNumber {

	private static void padLeft(StringBuilder dst, String zVal, int width, char fill) {
		final int pad = width - zVal.length();
		for (int i = 0; i < pad; i++) {
			dst.append(fill);
		}
		dst.append(zVal);
	}

	private static void spaceLeft(StringBuilder dst, String zVal, int width) {
		padLeft(dst, zVal, width, ' ');
	}

	private static String spaceLeftDec(float val, int width) {
		final StringBuilder sb = new StringBuilder(width);
		spaceLeftDec(sb, val, width);
		return sb.toString();
	}

	private static void spaceLeftDec(StringBuilder dst, float val, int width) {
		spaceLeft(dst, Float.toString(val), width);
	}

	private static void zeroLeft(StringBuilder dst, String zVal, int width) {
		padLeft(dst, zVal, width, '0');
	}

	private static String zeroLeftB36(long val, int width) {
		final StringBuilder sb = new StringBuilder(width);
		zeroLeftB36(sb, val, width);
		return sb.toString();
	}

	private static void zeroLeftB36(StringBuilder dst, long val, int width) {
		zeroLeft(dst, Long.toString(val, 36), width);
	}

	private static String zeroLeftDec(int val, int width) {
		final StringBuilder sb = new StringBuilder(width);
		zeroLeftDec(sb, val, width);
		return sb.toString();
	}

	private static String zeroLeftDec(long val, int width) {
		final StringBuilder sb = new StringBuilder(width);
		zeroLeftDec(sb, val, width);
		return sb.toString();
	}

	private static void zeroLeftDec(StringBuilder dst, int val, int width) {
		final boolean sgn = val < 0;
		if (sgn) {
			dst.append('-');
			zeroLeft(dst, Integer.toString(-val), width - 1);
		} else {
			zeroLeft(dst, Integer.toString(val), width);
		}
	}

	private static void zeroLeftDec(StringBuilder dst, long val, int width) {
		final boolean sgn = val < 0L;
		if (sgn) {
			dst.append('-');
			zeroLeft(dst, Long.toString(-val), width - 1);
		} else {
			zeroLeft(dst, Long.toString(val), width);
		}
	}

	private static String zeroLeftHex(int val, int width) {
		final StringBuilder sb = new StringBuilder(width);
		zeroLeftHex(sb, val, width);
		return sb.toString();
	}

	private static String zeroLeftHex(long val, int width) {
		final StringBuilder sb = new StringBuilder(width);
		zeroLeftHex(sb, val, width);
		return sb.toString();
	}

	private static void zeroLeftHex(StringBuilder dst, int val, int width) {
		zeroLeft(dst, Integer.toHexString(val), width);
	}

	private static void zeroLeftHex(StringBuilder dst, long val, int width) {
		zeroLeft(dst, Long.toHexString(val), width);
	}

	public static String floatToDec(float val, int width) {
		return spaceLeftDec(val, width);
	}

	public static String intToDec(int val, int width) {
		return zeroLeftDec(val, width);
	}

	public static String intToDec2(int val) {
		return zeroLeftDec(val, 2);
	}

	public static void intToDec2(StringBuilder dst, int val) {
		zeroLeftDec(dst, val, 2);
	}

	public static String intToDec3(int val) {
		return zeroLeftDec(val, 3);
	}

	public static void intToDec3(StringBuilder dst, int val) {
		zeroLeftDec(dst, val, 3);
	}

	public static String intToDec4(int val) {
		return zeroLeftDec(val, 4);
	}

	public static void intToDec4(StringBuilder dst, int val) {
		zeroLeftDec(dst, val, 4);
	}

	public static String intToDec5(int val) {
		return zeroLeftDec(val, 5);
	}

	public static void intToDec5(StringBuilder dst, int val) {
		zeroLeftDec(dst, val, 5);
	}

	public static String intToHex(int val, int width) {
		return zeroLeftHex(val, width);
	}

	public static String intToHexFull(int val) {
		return zeroLeftHex(val, 8);
	}

	public static String longToB36Full(long val) {
		return zeroLeftB36(val, 7);
	}

	public static String longToDec(long val, int width) {
		return zeroLeftDec(val, width);
	}

	public static String longToHex(long val, int width) {
		return zeroLeftHex(val, width);
	}

	public static String longToHexFull(long val) {
		return zeroLeftHex(val, 16);
	}

	public static String minuteOfDayToHM(int mod) {
		return minuteOfDayToHM(mod, null);
	}

	public static String minuteOfDayToHM(int mod, String ozHM) {
		int mr = Math.max(0, mod) % CArgon.MIN_PER_DAY;
		final int h24 = mr / CArgon.MIN_PER_HR;
		mr -= (h24 * CArgon.MIN_PER_HR);
		final StringBuilder sb = new StringBuilder();
		sb.append(ArgonNumber.intToDec2(h24));
		if (ozHM != null) {
			sb.append(ozHM);
		}
		sb.append(ArgonNumber.intToDec2(mr));
		return sb.toString();
	}

	public static String minuteOfDayToHMpunc(int mod) {
		return minuteOfDayToHM(mod, ":");
	}

	public static String secondOfDay(int sod) {
		return secondOfDayToHMS(sod, null, null);
	}

	public static String secondOfDayToHMS(int sod, String ozHM, String ozMS) {
		int sr = Math.max(0, sod) % CArgon.SEC_PER_DAY;
		final int h24 = sr / CArgon.SEC_PER_HR;
		sr -= (h24 * CArgon.SEC_PER_HR);
		final int m = sr / CArgon.SEC_PER_MIN;
		sr -= (m * CArgon.SEC_PER_MIN);
		final StringBuilder sb = new StringBuilder();
		sb.append(ArgonNumber.intToDec2(h24));
		if (ozHM != null) {
			sb.append(ozHM);
		}
		sb.append(ArgonNumber.intToDec2(m));
		if (ozHM != null) {
			sb.append(ozHM);
		}
		sb.append(ArgonNumber.intToDec2(sr));
		return sb.toString();
	}

	public static String secondOfDayToHMSpunc(int sod) {
		return secondOfDayToHMS(sod, ":", ".");
	}

}
