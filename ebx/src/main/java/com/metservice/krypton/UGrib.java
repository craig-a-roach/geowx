/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.Calendar;
import java.util.TimeZone;

import com.metservice.argon.CArgon;
import com.metservice.argon.DateFormatter;

/**
 * @author roach
 */
class UGrib {

	public static final int SectionG1LengthBc = 3;
	public static final int SectionG2LengthBc = 4;
	public static final int SectionG2LengthNumberBc = 5;
	public static final int INT_UNDEFINED = -9999;
	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

	public static final float BDS1_UNDEFINED_LOW = 9.9989e20f;
	public static final float BDS1_UNDEFINED_HIGH = 9.9991e20f;

	private static final long MUL_SECOND = 1000L;
	private static final long MUL_MINUTE = 60 * MUL_SECOND;
	private static final long MUL_HOUR = 60 * MUL_MINUTE;
	private static final long MUL_DAY = 24 * MUL_HOUR;
	private static final double LOG2 = Math.log(2.0);
	private static final double TWOPOW24 = Math.pow(2, 24);

	private static final double FP_IBM_SAd = 16.0;
	private static final int FP_IBM_SA24 = 6;
	private static final int FP_IBM_SB = 64;
	private static final long MASK7S = 0x7F00000000000000L;
	private static final long MASK6 = 0xFF000000000000L;

	private static final long MASK5 = 0xFF0000000000L;
	private static final long MASK4 = 0xFF00000000L;
	private static final long MASK3L = 0xFF000000L;
	private static final long MASK2L = 0xFF0000L;
	private static final long MASK1L = 0xFF00L;
	private static final long MASK0L = 0xFFL;
	private static final int MASK3S = 0x7F000000;
	private static final int MASK2 = 0xFF0000;
	private static final int MASK2S = 0x7F0000;
	private static final int MASK1 = 0xFF00;
	private static final int MASK1S = 0x7F00;
	private static final int MASK0 = 0xFF;
	private static final int MASK0S = 0x7F;

	public static int bcBodyG2(int bcSection) {
		return bcSection - SectionG2LengthNumberBc;
	}

	public static int biOctetG1(int octet) {
		return octet - SectionG1LengthBc - 1;
	}

	public static int biOctetG2(int octet) {
		return octet - SectionG2LengthNumberBc - 1;
	}

	public static char char1(byte[] buffer, int pos) {
		return (char) (buffer[pos] & 0xFF);
	}

	public static double descale(int scaleFactor, int scaledValue) {
		return ((scaleFactor == 0) || (scaledValue == 0)) ? scaledValue : scaledValue * Math.pow(10, -scaleFactor);
	}

	public static double double2(byte[] buffer, int pos, double divider) {
		final short x = shortu2(buffer, pos);
		if (x == -1) return INT_UNDEFINED;
		return (x / divider);
	}

	public static double double2OctetG1(byte[] section, int octetStart, double divider) {
		return double2(section, biOctetG1(octetStart), divider);
	}

	public static double double4(byte[] buffer, int pos, double divider) {
		final int x = UGrib.int3(buffer, pos);
		if (x == -1) return INT_UNDEFINED;
		return (x / divider);
	}

	public static float float4IBM(byte[] buffer, int pos) {
		final int a = buffer[pos + 0] & 0xFF;
		final int b = buffer[pos + 1] & 0xFF;
		final int c = buffer[pos + 2] & 0xFF;
		final int d = buffer[pos + 3] & 0xFF;
		return float4IBM(a, b, c, d);
	}

	public static byte[] float4IBM(byte[] buffer, int pos, float value) {
		int a = 0;
		int b = 0;
		int c = 0;
		int d = 0;
		if (value != 0.0f) {
			final boolean neg = value < 0.0f;
			final float avalue = neg ? -value : value;
			final int sbit = neg ? 0x80 : 0x00;
			final double dexp = (Math.log(avalue) / LOG2 * 0.25) + 1.0 + FP_IBM_SB;
			final int exp = (int) dexp;
			final double dfrac = avalue / Math.pow(FP_IBM_SAd, exp - FP_IBM_SB);
			final int frac = (int) (TWOPOW24 * dfrac);
			a = sbit | exp;
			b = (frac & MASK2) >> 16;
			c = (frac & MASK1) >> 8;
			d = (frac & MASK0);
		}
		buffer[pos + 0] = (byte) a;
		buffer[pos + 1] = (byte) b;
		buffer[pos + 2] = (byte) c;
		buffer[pos + 3] = (byte) d;
		return buffer;
	}

	public static float float4IBM(int a, int b, int c, int d) {
		final int sbit = (a & 0x80) >> 7;
		final int exp = (a & 0x7F);
		final int frac = b << 16 | c << 8 | d;
		if (sbit == 0 && exp == 0 && frac == 0) return 0.0f;
		final double sm = (sbit == 0) ? 1.0 : -1.0;
		final double df4 = sm * frac * Math.pow(FP_IBM_SAd, exp - FP_IBM_SB - FP_IBM_SA24);
		return (float) df4;
	}

	public static float float4IEEE(byte[] buffer, int pos) {
		final int a = buffer[pos + 0] & 0xFF;
		final int b = buffer[pos + 1] & 0xFF;
		final int c = buffer[pos + 2] & 0xFF;
		final int d = buffer[pos + 3] & 0xFF;
		return float4IEEE(a, b, c, d);
	}

	public static float float4IEEE(int a, int b, int c, int d) {
		final int x = (a << 24) + (b << 16) + (c << 8) + d;
		return Float.intBitsToFloat(x);
	}

	public static float float4OctetG1(byte[] section, int octetStart) {
		return float4IBM(section, biOctetG1(octetStart));
	}

	public static byte[] float4OctetG1(byte[] section, int octetStart, float value) {
		return float4IBM(section, biOctetG1(octetStart), value);
	}

	public static float float4OctetG2(byte[] section, int octetStart) {
		return float4IEEE(section, biOctetG2(octetStart));
	}

	public static boolean hasMoreOctetsG1(byte[] section, int octetPos) {
		final int bi = biOctetG1(octetPos);
		return bi < section.length;
	}

	public static boolean hasMoreOctetsG2(byte[] section, int octetPos) {
		final int bi = biOctetG2(octetPos);
		return bi < section.length;
	}

	public static boolean hasOctetsG1(byte[] section, int octetStart, int count) {
		final int bi = biOctetG1(octetStart);
		final int biEnd = bi + count;
		return biEnd <= section.length;
	}

	public static int int2(byte[] buffer, int pos) {
		final int a = buffer[pos + 0] & 0xFF;
		final int b = buffer[pos + 1] & 0xFF;
		return int2(a, b);
	}

	public static byte[] int2(byte[] buffer, int pos, int value) {
		int a = 0xFF;
		int b = 0xFF;
		if (value != INT_UNDEFINED) {
			final boolean neg = value < 0;
			final int avalue = neg ? -value : value;
			final int sbit = neg ? 0x80 : 0x00;
			final int hi = (avalue & MASK1S) >> 8;
			a = sbit | hi;
			b = avalue & MASK0;
		}
		buffer[pos + 0] = (byte) a;
		buffer[pos + 1] = (byte) b;
		return buffer;
	}

	public static int int2(int a, int b) {
		if (a == 0xFF && b == 0xFF) return INT_UNDEFINED;
		final boolean neg = (a & 0x80) == 0x80;
		final int u = (a & MASK0S) << 8 | b;
		return neg ? -u : u;
	}

	public static int int2OctetG1(byte[] section, int octetStart) {
		return int2(section, biOctetG1(octetStart));
	}

	public static byte[] int2OctetG1(byte[] section, int octetStart, int value) {
		return int2(section, biOctetG1(octetStart), value);
	}

	public static int int2OctetG2(byte[] section, int octetStart) {
		return int2(section, biOctetG2(octetStart));
	}

	public static int int3(byte[] buffer, int pos) {
		final int a = buffer[pos + 0] & 0xFF;
		final int b = buffer[pos + 1] & 0xFF;
		final int c = buffer[pos + 2] & 0xFF;
		return int3(a, b, c);
	}

	public static byte[] int3(byte[] buffer, int pos, int value) {
		int a = 0xFF;
		int b = 0xFF;
		int c = 0xFF;
		if (value != INT_UNDEFINED) {
			final boolean neg = value < 0;
			final int avalue = neg ? -value : value;
			final int sbit = neg ? 0x80 : 0x00;
			final int hi = (avalue & MASK2S) >> 16;
			a = sbit | hi;
			b = (avalue & MASK1) >> 8;
			c = avalue & MASK0;
		}
		buffer[pos + 0] = (byte) a;
		buffer[pos + 1] = (byte) b;
		buffer[pos + 2] = (byte) c;
		return buffer;
	}

	public static int int3(int a, int b, int c) {
		if (a == 0xFF && b == 0xFF && c == 0xFF) return INT_UNDEFINED;
		final boolean neg = (a & 0x80) == 0x80;
		final int u = (a & MASK0S) << 16 | b << 8 | c;
		return neg ? -u : u;
	}

	public static int int3OctetG1(byte[] section, int octetStart) {
		return int3(section, biOctetG1(octetStart));
	}

	public static byte[] int3OctetG1(byte[] section, int octetStart, int value) {
		return int3(section, biOctetG1(octetStart), value);
	}

	public static int int4(byte[] buffer, int pos) {
		final int a = buffer[pos + 0] & 0xFF;
		final int b = buffer[pos + 1] & 0xFF;
		final int c = buffer[pos + 2] & 0xFF;
		final int d = buffer[pos + 3] & 0xFF;
		return int4(a, b, c, d);
	}

	public static byte[] int4(byte[] buffer, int pos, int value) {
		int a = 0xFF;
		int b = 0xFF;
		int c = 0xFF;
		int d = 0xFF;
		if (value != INT_UNDEFINED) {
			final boolean neg = value < 0;
			final int avalue = neg ? -value : value;
			final int sbit = neg ? 0x80 : 0x00;
			final int hi = (avalue & MASK3S) >> 24;
			a = sbit | hi;
			b = (avalue & MASK2) >> 16;
			c = (avalue & MASK1) >> 8;
			d = avalue & MASK0;
		}
		buffer[pos + 0] = (byte) a;
		buffer[pos + 1] = (byte) b;
		buffer[pos + 2] = (byte) c;
		buffer[pos + 3] = (byte) d;
		return buffer;
	}

	public static int int4(int a, int b, int c, int d) {
		if (a == 0xFF && b == 0xFF && c == 0xFF && d == 0xFF) return INT_UNDEFINED;
		final boolean neg = (a & 0x80) == 0x80;
		final int u = (a & MASK0S) << 24 | b << 16 | c << 8 | d;
		return neg ? -u : u;
	}

	public static byte[] int4OctetG1(byte[] section, int octetStart, int value) {
		return int4(section, biOctetG1(octetStart), value);
	}

	public static int int4OctetG2(byte[] section, int octetStart) {
		return int4(section, biOctetG2(octetStart));
	}

	public static int intu1(byte[] buffer, int pos) {
		return buffer[pos] & 0xFF;
	}

	public static byte[] intu1(byte[] buffer, int pos, int value) {
		final int avalue = value < 0 ? -value : value;
		final int a = avalue & MASK0;
		buffer[pos] = (byte) a;
		return buffer;
	}

	public static int intu1OctetG1(byte[] section, int octet) {
		return intu1(section, biOctetG1(octet));
	}

	public static byte[] intu1OctetG1(byte[] section, int octetStart, int value) {
		return intu1(section, biOctetG1(octetStart), value);
	}

	public static int intu1OctetG2(byte[] section, int octet) {
		return intu1(section, biOctetG2(octet));
	}

	public static int intu2(byte[] buffer, int pos) {
		final int a = buffer[pos + 0] & 0xFF;
		final int b = buffer[pos + 1] & 0xFF;
		return intu2(a, b);
	}

	public static byte[] intu2(byte[] buffer, int pos, int value) {
		final int avalue = value < 0 ? -value : value;
		final int a = (avalue & MASK1) >> 8;
		final int b = avalue & MASK0;
		buffer[pos + 0] = (byte) a;
		buffer[pos + 1] = (byte) b;
		return buffer;
	}

	public static int intu2(int a, int b) {
		return ((a << 8) + b);
	}

	public static int intu2OctetG1(byte[] section, int octet) {
		return intu2(section, biOctetG1(octet));
	}

	public static byte[] intu2OctetG1(byte[] section, int octetStart, int value) {
		return intu2(section, biOctetG1(octetStart), value);
	}

	public static int intu3(byte[] buffer, int pos) {
		final int b0 = buffer[pos + 0] & 0xFF;
		final int b1 = buffer[pos + 1] & 0xFF;
		final int b2 = buffer[pos + 2] & 0xFF;
		return ((b0 << 16) + (b1 << 8) + b2);
	}

	public static byte[] intu3(byte[] buffer, int pos, int value) {
		final int avalue = value < 0 ? -value : value;
		final int a = (avalue & MASK2) >> 16;
		final int b = (avalue & MASK1) >> 8;
		final int c = avalue & MASK0;
		buffer[pos + 0] = (byte) a;
		buffer[pos + 1] = (byte) b;
		buffer[pos + 2] = (byte) c;
		return buffer;
	}

	public static int intu3(int a, int b, int c) {
		return ((a << 16) + (b << 8) + c);
	}

	public static int intu3OctetG1(byte[] section, int octet) {
		return intu3(section, biOctetG1(octet));
	}

	public static byte[] intu3OctetG1(byte[] section, int octetStart, int value) {
		return intu3(section, biOctetG1(octetStart), value);
	}

	public static boolean isBdsUndefined(float x) {
		return Float.isNaN(x) || (x >= BDS1_UNDEFINED_LOW && x <= BDS1_UNDEFINED_HIGH);
	}

	public static long long8(byte[] buffer, int pos) {
		final int a = buffer[pos + 0] & 0xFF;
		final int b = buffer[pos + 1] & 0xFF;
		final int c = buffer[pos + 2] & 0xFF;
		final int d = buffer[pos + 3] & 0xFF;
		final int e = buffer[pos + 4] & 0xFF;
		final int f = buffer[pos + 5] & 0xFF;
		final int g = buffer[pos + 6] & 0xFF;
		final int h = buffer[pos + 7] & 0xFF;
		return long8(a, b, c, d, e, f, g, h);
	}

	public static byte[] long8(byte[] buffer, int pos, long value) {
		long a = 0xFF;
		long b = 0xFF;
		long c = 0xFF;
		long d = 0xFF;
		long e = 0xFF;
		long f = 0xFF;
		long g = 0xFF;
		long h = 0xFF;
		if (value != INT_UNDEFINED) {
			final boolean neg = value < 0;
			final long avalue = neg ? -value : value;
			final long sbit = neg ? 0x80 : 0x00;
			final long hi = (avalue & MASK7S) >> 56;
			a = sbit | hi;
			b = (avalue & MASK6) >> 48;
			c = (avalue & MASK5) >> 40;
			d = (avalue & MASK4) >> 32;
			e = (avalue & MASK3L) >> 24;
			f = (avalue & MASK2L) >> 16;
			g = (avalue & MASK1L) >> 8;
			h = avalue & MASK0L;
		}
		buffer[pos + 0] = (byte) a;
		buffer[pos + 1] = (byte) b;
		buffer[pos + 2] = (byte) c;
		buffer[pos + 3] = (byte) d;
		buffer[pos + 4] = (byte) e;
		buffer[pos + 5] = (byte) f;
		buffer[pos + 6] = (byte) g;
		buffer[pos + 7] = (byte) h;
		return buffer;
	}

	public static long long8(int a, int b, int c, int d, int e, int f, int g, int h) {
		if (a == 0xFF && b == 0xFF && c == 0xFF && d == 0xFF && e == 0xFF && f == 0xFF && g == 0xFF && h == 0xFF)
			return INT_UNDEFINED;
		final boolean neg = (a & 0x80) == 0x80;
		long u = (((long) a) & MASK0S) << 56;
		u |= ((long) b) << 48;
		u |= ((long) c) << 40;
		u |= ((long) d) << 32;
		u |= e << 24;
		u |= f << 16;
		u |= g << 8;
		u |= h;
		return neg ? -u : u;
	}

	public static byte[] long8OctetG1(byte[] section, int octetStart, long value) {
		return long8(section, biOctetG1(octetStart), value);
	}

	public static short shortu1(byte[] buffer, int pos) {
		return (short) (buffer[pos] & 0xFF);
	}

	public static short shortu1hi(byte[] buffer, int pos) {
		return (short) (buffer[pos] & 0xF0);
	}

	public static short shortu1lo(byte[] buffer, int pos) {
		return (short) (buffer[pos] & 0x0F);
	}

	public static short shortu1OctetG1(byte[] section, int octet) {
		return shortu1(section, biOctetG1(octet));
	}

	public static short shortu1OctetG2(byte[] section, int octet) {
		return shortu1(section, biOctetG2(octet));
	}

	public static short shortu1OctetHiG1(byte[] section, int octet) {
		return shortu1hi(section, biOctetG1(octet));
	}

	public static short shortu1OctetLoG1(byte[] section, int octet) {
		return shortu1lo(section, biOctetG1(octet));
	}

	public static short shortu2(byte[] buffer, int pos) {
		final int a = buffer[pos + 0] & 0xFF;
		final int b = buffer[pos + 1] & 0xFF;
		return (short) intu2(a, b);
	}

	public static short shortu2OctetG1(byte[] section, int octetStart) {
		return shortu2(section, biOctetG1(octetStart));
	}

	public static long smsG1(String source, short fUnit, int count)
			throws KryptonCodeException {
		switch (fUnit) {
			case 0:
				return MUL_MINUTE * count;
			case 1:
				return MUL_HOUR * count;
			case 2:
			case 6:
				return MUL_DAY * count;
			case 10:
				return 3 * MUL_HOUR * count;
			case 11:
				return 6 * MUL_HOUR * count;
			case 12:
				return 12 * MUL_HOUR * count;
			case 254:
				return MUL_SECOND * count;
			default: {
				final String m = "Time unit " + fUnit + " not supported (GRIB1 Code Table 4)";
				throw new KryptonCodeException(source, m);
			}
		}
	}

	public static long smsG2(String source, short fUnit, int count)
			throws KryptonCodeException {
		switch (fUnit) {
			case 0:
				return MUL_MINUTE * count;
			case 1:
				return MUL_HOUR * count;
			case 2:
				return MUL_DAY * count;
			case 10:
				return 3 * MUL_HOUR * count;
			case 11:
				return 6 * MUL_HOUR * count;
			case 12:
				return 12 * MUL_HOUR * count;
			case 254:
				return MUL_SECOND * count;
			default: {
				final String m = "Time unit " + fUnit + " not supported (GRIB2 Code Table 4.4)";
				throw new KryptonCodeException(source, m);
			}
		}
	}

	public static int ssecsG1(String source, short fUnit, int count)
			throws KryptonCodeException {
		final long ssecs = smsG1(source, fUnit, count) / MUL_SECOND;
		if (ssecs < Integer.MIN_VALUE || ssecs > Integer.MAX_VALUE) {
			final String m = "Invalid interval; count=" + count + ", unit=" + fUnit;
			throw new KryptonCodeException(source, m);
		}
		return (int) ssecs;
	}

	public static int ssecsG2(String source, short fUnit, int count)
			throws KryptonCodeException {
		final long ssecs = smsG2(source, fUnit, count) / MUL_SECOND;
		if (ssecs < Integer.MIN_VALUE || ssecs > Integer.MAX_VALUE) {
			final String m = "Invalid interval; count=" + count + ", unit=" + fUnit;
			throw new KryptonCodeException(source, m);
		}
		return (int) ssecs;
	}

	public static int ssecsRange(String source, long tsRef, long tsToex)
			throws KryptonCodeException {
		final long ssecs = ((tsToex - tsRef) / CArgon.MS_PER_SEC);
		if (ssecs < Integer.MIN_VALUE || ssecs > Integer.MAX_VALUE) {
			final String m = "Invalid time (" + DateFormatter.newT8FromTs(tsToex) + "), reference ("
					+ DateFormatter.newT8FromTs(tsRef) + ") pair";
			throw new KryptonCodeException(source, m);
		}
		return (int) ssecs;
	}

	public static long tsG1(String source, int century, int yoc, int moy, int dom, int hod, int moh)
			throws KryptonCodeException {
		try {
			final Calendar cal = Calendar.getInstance(GMT);
			cal.setLenient(false);
			cal.set(Calendar.YEAR, ((century * 100) + yoc));
			cal.set(Calendar.MONTH, moy - 1);
			cal.set(Calendar.DATE, dom);
			cal.set(Calendar.HOUR_OF_DAY, hod);
			cal.set(Calendar.MINUTE, moh);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTimeInMillis();
		} catch (final RuntimeException ex) {
			final String f = "cc" + century + " yoc" + yoc + " moy" + moy + " dom" + dom + " hod" + hod + " moh" + moh;
			final String m = "Invalid time fields " + f;
			throw new KryptonCodeException(source, m);
		}
	}

	public static long tsG2(String source, int year, int moy, int dom, int hod, int moh, int sec)
			throws KryptonCodeException {
		try {
			final Calendar cal = Calendar.getInstance(GMT);
			cal.setLenient(false);
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, moy - 1);
			cal.set(Calendar.DATE, dom);
			cal.set(Calendar.HOUR_OF_DAY, hod);
			cal.set(Calendar.MINUTE, moh);
			cal.set(Calendar.SECOND, sec);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTimeInMillis();
		} catch (final RuntimeException ex) {
			final String f = "year" + year + " moy" + moy + " dom" + dom + " hod" + hod + " moh" + moh;
			final String m = "Invalid time fields " + f;
			throw new KryptonCodeException(source, m);
		}
	}
}
