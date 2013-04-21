/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Calendar;
import java.util.Date;

/**
 * @author roach
 */
public class DateFormatter {

	private static void makeH(StringBuilder sb, Calendar cal) {
		UArgon.intToDec2(sb, cal.get(Calendar.HOUR_OF_DAY));
	}

	private static void makeHM(StringBuilder sb, Calendar cal) {
		makeH(sb, cal);
		makeM(sb, cal);
	}

	private static void makeM(StringBuilder sb, Calendar cal) {
		UArgon.intToDec2(sb, cal.get(Calendar.MINUTE));
	}

	private static void makeMD(StringBuilder sb, Calendar cal) {
		UArgon.intToDec2(sb, cal.get(Calendar.MONTH) + 1);
		UArgon.intToDec2(sb, cal.get(Calendar.DATE));
	}

	private static void makeS(StringBuilder sb, Calendar cal) {
		UArgon.intToDec2(sb, cal.get(Calendar.SECOND));
	}

	private static void makeT(StringBuilder sb, Calendar cal) {
		UArgon.intToDec3(sb, cal.get(Calendar.MILLISECOND));
	}

	private static void makeY(StringBuilder sb, Calendar cal) {
		UArgon.intToDec4(sb, cal.get(Calendar.YEAR));
	}

	private static String newPlatformFromTs(long ts, boolean year, int precision) {
		final Calendar cal = UArgon.newPlatformCalendar();
		final int minsGMTAdjustedOffset = cal.getTimeZone().getOffset(ts) / CArgon.MS_PER_MIN;
		return newPlatformFromTs(ts, cal, minsGMTAdjustedOffset, year, precision);
	}

	private static String newPlatformFromTs(long ts, Calendar cal, int minsGMTAdjustedOffset, boolean year, int precision) {
		assert cal != null;
		cal.setTimeInMillis(ts);
		final int moyJan1 = cal.get(Calendar.MONTH) + 1;
		final StringBuilder sb = new StringBuilder(64);
		UArgon.intToDec2(sb, cal.get(Calendar.DATE));
		sb.append('-');
		sb.append(UArgon.monthOfYear3(moyJan1));
		sb.append(' ');
		makeHM(sb, cal);
		if (precision > 0) {
			sb.append('.');
			makeS(sb, cal);
			if (precision > 1) {
				sb.append('.');
				makeT(sb, cal);
			}
		}
		if (year) {
			sb.append(' ');
			makeY(sb, cal);
		}
		final int hm;
		if (minsGMTAdjustedOffset < 0) {
			sb.append('-');
			hm = -minsGMTAdjustedOffset;
		} else {
			sb.append('+');
			hm = minsGMTAdjustedOffset;
		}
		final int h = hm / 60;
		final int m = hm - (h * 60);
		sb.append(UArgon.intToDec2(h));
		sb.append(':');
		sb.append(UArgon.intToDec2(m));
		return sb.toString();
	}

	public static String newPlatformDHMSFromTs(long ts) {
		return newPlatformFromTs(ts, false, 1);
	}

	public static String newPlatformDHMSTFromTs(long ts) {
		return newPlatformFromTs(ts, false, 2);
	}

	public static String newPlatformDHMSTYFromTs(long ts) {
		return newPlatformFromTs(ts, true, 2);
	}

	public static String newPlatformDHMSYFromTs(long ts) {
		return newPlatformFromTs(ts, true, 1);
	}

	public static String newT8FromDate(Date d) {
		if (d == null) throw new IllegalArgumentException("object is null");
		return newT8FromTs(d.getTime());
	}

	public static String newT8FromTs(long ts) {
		final Calendar cal = UArgon.newGMTCalendar();
		cal.setTimeInMillis(ts);
		final StringBuilder sb = new StringBuilder(20);
		makeY(sb, cal);
		makeMD(sb, cal);
		sb.append(CArgon.DATE_SEPARATOR_DAYHOUR);
		makeHM(sb, cal);
		sb.append(CArgon.DATE_SEPARATOR_MINSEC);
		makeS(sb, cal);
		sb.append(CArgon.DATE_SEPARATOR_SECMILLI);
		makeT(sb, cal);
		return sb.toString();
	}

	public static String newT8PlatformDHMFromTs(long ts) {
		final String t8 = DateFormatter.newT8FromTs(ts);
		final Calendar cal = UArgon.newPlatformCalendar();
		final int minsGMTAdjustedOffset = cal.getTimeZone().getOffset(ts) / CArgon.MS_PER_MIN;
		if (minsGMTAdjustedOffset == 0) return t8;

		final StringBuilder sb = new StringBuilder(64);
		sb.append(t8);
		sb.append(" (");
		sb.append(newPlatformFromTs(ts, cal, minsGMTAdjustedOffset, false, 0));
		sb.append(")");
		return sb.toString();
	}

	public static String newYMDHFromTs(long ts) {
		final Calendar cal = UArgon.newGMTCalendar();
		cal.setTimeInMillis(ts);
		final StringBuilder sb = new StringBuilder(20);
		makeY(sb, cal);
		makeMD(sb, cal);
		sb.append(' ');
		makeH(sb, cal);
		sb.append('Z');
		return sb.toString();
	}

	public static String newYMDHMFromTs(long ts) {
		final Calendar cal = UArgon.newGMTCalendar();
		cal.setTimeInMillis(ts);
		final StringBuilder sb = new StringBuilder(20);
		makeY(sb, cal);
		makeMD(sb, cal);
		sb.append(' ');
		makeHM(sb, cal);
		sb.append('Z');
		return sb.toString();
	}

	public static String newYMDHMSFromTs(long ts) {
		final Calendar cal = UArgon.newGMTCalendar();
		cal.setTimeInMillis(ts);
		final StringBuilder sb = new StringBuilder(20);
		makeY(sb, cal);
		makeMD(sb, cal);
		sb.append(' ');
		makeHM(sb, cal);
		sb.append('.');
		makeS(sb, cal);
		sb.append('Z');
		return sb.toString();
	}

	private DateFormatter() {
	}
}
