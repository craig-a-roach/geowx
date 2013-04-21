/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author roach
 */
class UArgon {

	public static final String SysPropName_User = "user.name";
	public static final String SysPropName_UserHome = "user.home";

	public static final String CHARSET_NAME_ASCII = "US-ASCII";
	public static final String CHARSET_NAME_UTF8 = "UTF-8";
	public static final String CHARSET_NAME_ISO8859 = "ISO-8859-1";

	public static final Charset ASCII = Charset.forName(CHARSET_NAME_ASCII);
	public static final Charset UTF8 = Charset.forName(CHARSET_NAME_UTF8);
	public static final Charset ISO8859_1 = Charset.forName(CHARSET_NAME_ISO8859);

	public static final String TIMEZONE_NAME_GMT = "GMT";
	public static final TimeZone GMT = TimeZone.getTimeZone(TIMEZONE_NAME_GMT);

	public static final TimeZone PLATFORM = TimeZone.getDefault();

	public static final String[] DowNameMon0 = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

	public static final String[] DowCodeMon0 = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

	public static final String[] MoyCodeJan0 = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
			"Dec" };

	public static final String[] MoyNameJan0 = { "January", "February", "March", "April", "May", "June", "July", "August",
			"September", "October", "November", "December" };

	public static final String[] MoyCodeJan0LowerCase = { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct",
			"nov", "dec" };

	public static final String[] MoyNameJan0LowerCase = { "january", "february", "march", "april", "may", "june", "july",
			"august", "september", "october", "november", "december" };

	public static String byteToDecimal3(int octet) {
		final int x = octet & 0xFF;
		if (x < 10) return "00" + Integer.toString(x);
		if (x < 100) return "0" + Integer.toString(x);
		return Integer.toString(x);
	}

	public static String byteToHex2(int octet) {
		final int x = octet & 0xFF;
		if (x < 16) return "0" + Integer.toHexString(x);
		return Integer.toHexString(x);
	}

	public static String intToDec2(int val) {
		return zeroLeft(Integer.toString(val), 2);
	}

	public static void intToDec2(StringBuilder dst, int val) {
		zeroLeft(dst, Integer.toString(val), 2);
	}

	public static String intToDec3(int val) {
		return zeroLeft(Integer.toString(val), 3);
	}

	public static void intToDec3(StringBuilder dst, int val) {
		zeroLeft(dst, Integer.toString(val), 3);
	}

	public static String intToDec4(int val) {
		return zeroLeft(Integer.toString(val), 4);
	}

	public static void intToDec4(StringBuilder dst, int val) {
		zeroLeft(dst, Integer.toString(val), 4);
	}

	public static String intToDec6(int val) {
		return zeroLeft(Integer.toString(val), 6);
	}

	public static String intToHex4(int val) {
		return zeroLeft(Integer.toHexString(val), 4);
	}

	public static boolean isLeapYear(int year) {
		return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
	}

	public static boolean isValidDay(int year, int monthJan1, int day) {
		return (day >= 1) && (day <= lastDayOfMonth1(year, monthJan1));
	}

	public static int lastDayOfMonth1(int year, int monthJan1) {
		if ((monthJan1 == 9 || monthJan1 == 4 || monthJan1 == 6 || monthJan1 == 11)) return 30;
		if (monthJan1 == 2) return isLeapYear(year) ? 29 : 28;
		return 31;
	}

	public static String longToDec19(long val) {
		return zeroLeft(Long.toHexString(val), 19);
	}

	public static String longToHex16(long val) {
		return zeroLeft(Long.toHexString(val), 16);
	}

	public static String monthOfYear3(int monthJan1) {
		if (monthJan1 < 1 || monthJan1 > 12) return Integer.toString(monthJan1);
		return MoyCodeJan0[monthJan1 - 1];
	}

	public static String msgComma(Object[] ozpt) {
		if (ozpt == null || ozpt.length == 0) return "";
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ozpt.length; i++) {
			final Object ovalue = ozpt[i];
			if (ovalue != null) {
				final String ztw = ovalue.toString().trim();
				if (ztw.length() > 0) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(ztw);
				}
			}
		}
		return sb.toString();
	}

	public static Calendar newCalendar(TimeZone tz) {
		if (tz == null) throw new IllegalArgumentException("object is null");
		final Calendar cal = Calendar.getInstance(tz);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.setMinimalDaysInFirstWeek(7);
		cal.setLenient(false);
		return cal;
	}

	public static Calendar newGMTCalendar() {
		final Calendar cal = Calendar.getInstance(GMT);
		cal.setLenient(false);
		return cal;
	}

	public static Calendar newPlatformCalendar() {
		final Calendar cal = Calendar.getInstance(PLATFORM);
		cal.setLenient(false);
		return cal;
	}

	/**
	 * Returns a compact ordinal representation of index argument, relative to firstIndex
	 * 
	 * @param index
	 *              - relative to firstIndex
	 * @param firstIndex
	 *              - index corresponding to 1st
	 * @return 1st, 2nd, 3rd, 4th .. nth where n is (index - firstIndex + 1)
	 */
	public static String nth(int index, int firstIndex) {
		final int i = index - firstIndex + 1;
		if (i <= 0) return "?";
		final String n = Integer.toString(i);
		final int im10 = i % 10;
		if (im10 == 1 && i != 11) return n + "st";

		if (im10 == 2 && i != 12) return n + "nd";

		if (im10 == 3 && i != 13) return n + "rd";

		return n + "th";
	}

	public static String qUserHome()
			throws ArgonPermissionException {
		final String ozUserHome = System.getProperty(SysPropName_UserHome);
		if (ozUserHome == null || ozUserHome.length() == 0) throw new ArgonPermissionException("Cannot determine user home");
		return ozUserHome;
	}

	public static String qUserName() {
		final String ozUserName = System.getProperty(SysPropName_User);
		return ozUserName == null || ozUserName.length() == 0 ? "unknown" : ozUserName;
	}

	public static String zeroLeft(String zVal, int width) {
		final StringBuilder sb = new StringBuilder(width);
		zeroLeft(sb, zVal, width);
		return sb.toString();
	}

	public static void zeroLeft(StringBuilder dst, String zVal, int width) {
		final int pad = width - zVal.length();
		for (int i = 0; i < pad; i++) {
			dst.append('0');
		}
		dst.append(zVal);
	}
}
