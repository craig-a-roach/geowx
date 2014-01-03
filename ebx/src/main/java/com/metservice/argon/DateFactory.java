/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author roach
 */
public class DateFactory {

	public static final Pattern DefaultFields = Pattern.compile("\\d+");

	private static boolean isAlpha(String qnctw) {
		assert qnctw != null;
		final int len = qnctw.length();
		for (int i = 0; i < len; i++) {
			final char ch = qnctw.charAt(i);
			if (!ArgonText.isLetter(ch)) return false;
		}
		return true;
	}

	private static boolean isDigit(String qnctw) {
		assert qnctw != null;
		final int len = qnctw.length();
		for (int i = 0; i < len; i++) {
			final char ch = qnctw.charAt(i);
			if (!ArgonText.isDigit(ch)) return false;
		}
		return true;
	}

	private static boolean isMarkerMalformed(String in) {
		assert in != null;
		final int len = in.length();
		if (len < 9) return true;
		if (in.charAt(8) != CArgon.DATE_SEPARATOR_DAYHOUR) return true;
		if (len > 13 && in.charAt(13) != CArgon.DATE_SEPARATOR_MINSEC) return true;
		if (len > 16 && in.charAt(16) != CArgon.DATE_SEPARATOR_SECMILLI) return true;
		return false;
	}

	private static boolean isWellFormed(String in) {
		assert in != null;
		if (isMarkerMalformed(in)) return false;
		final int len = in.length();
		try {
			final int year = Integer.parseInt(in.substring(0, 4));
			final int monthJan1 = Integer.parseInt(in.substring(4, 6));
			final int day = Integer.parseInt(in.substring(6, 8));
			final int hour = len < 11 ? 0 : Integer.parseInt(in.substring(9, 11));
			final int minute = len < 13 ? 0 : Integer.parseInt(in.substring(11, 13));
			final int second = len < 16 ? 0 : Integer.parseInt(in.substring(14, 16));
			if (len == 20) {
				Integer.parseInt(in.substring(17, 20));
			}
			if (hour > 23) return false;
			if (minute > 59) return false;
			if (second > 59) return false;
			return UArgon.isValidDay(year, monthJan1, day);
		} catch (final NumberFormatException ex) {
		}
		return false;
	}

	private static boolean isYear(String qnctw) {
		assert qnctw != null && qnctw.length() > 0;
		if (qnctw.length() != 4) return false;
		try {
			Integer.parseInt(qnctw);
			return true;
		} catch (final NumberFormatException ex) {
			return false;
		}
	}

	private static int moy(String nOrCode) {
		assert nOrCode != null;
		final int len = nOrCode.length();
		if (len == 0) return 0;
		if (len < 3) {
			try {
				final int n = Integer.parseInt(nOrCode);
				if (n >= 1 && n <= 12) return n;
			} catch (final NumberFormatException ex) {
			}
			return 0;
		}
		if (len == 3) return moy1(UArgon.MoyCodeJan0LowerCase, nOrCode);
		return moy1(UArgon.MoyNameJan0LowerCase, nOrCode);
	}

	private static int moy1(String[] xptlctwMonthCodes, String qnctw) {
		final String qlctw = qnctw.toLowerCase();
		for (int moy0 = 0; moy0 < xptlctwMonthCodes.length; moy0++) {
			if (qlctw.equals(xptlctwMonthCodes[moy0])) return moy0 + 1;
		}
		return 0;
	}

	private static final String msgBadPart(String qSpec, String problem, int i, String zValue) {
		return problem + " in part " + i + " (" + zValue + ") of '" + qSpec + "'";
	}

	private static final String msgBounds(String qSpec, String qField, int value) {
		return "Value for " + qField + " in '" + qSpec + "' (" + value + ") is out of bounds";
	}

	private static final String msgMissing(String qSpec, String qField) {
		return "Missing " + qField + " in '" + qSpec + "'";
	}

	private static final String msgNonNumeric(String qSpec, String qField) {
		return "Non-numeric value for " + qField + " in '" + qSpec + "'";
	}

	private static final String msgNotStrict(String qSpec) {
		return "Fields of  '" + qSpec + "' are well-formed, but fail the strictness test";
	}

	private static String padT8(String in) {
		if (in == null) throw new IllegalArgumentException("object is null");
		final int len = in.length();
		if (len == 20) return in;
		if (len == 16) return in + CArgon.DATE_SUFFIX1;
		if (len == 14) return in + CArgon.DATE_SUFFIX2;
		return in;
	}

	private static String selectMonth(String qSpec, String[] xptlctwMonthCodes, int ipart, String qnctw)
			throws ArgonFormatException {
		final String qlctw = qnctw.toLowerCase();
		for (int moy0 = 0; moy0 < xptlctwMonthCodes.length; moy0++) {
			if (qlctw.equals(xptlctwMonthCodes[moy0])) return UArgon.intToDec2(moy0 + 1);
		}
		throw new ArgonFormatException(msgBadPart(qSpec, "Unrecognised month field", ipart, qnctw));
	}

	private static String tzId(String qnctw) {
		assert qnctw != null;
		if (qnctw.startsWith("GMT")) return qnctw;
		if (isAlpha(qnctw)) return qnctw;
		return "GMT" + qnctw;
	}

	public static Date createDate(long ts) {
		if (ts < 0L) return null;
		return new Date(ts);
	}

	public static boolean isMarkerMalformedT8(String t) {
		final int len = t.length();
		return (len != 20) || isMarkerMalformed(t);
	}

	public static boolean isWellFormedT8(String t) {
		if (t == null) throw new IllegalArgumentException("object is null");
		final int len = t.length();
		return (len == 20) && isWellFormed(t);
	}

	public static boolean isWellFormedTX(String t) {
		final int len = t.length();
		return (len == 20 || len == 16 || len == 14) && isWellFormed(t);
	}

	public static Date newDate(long ts) {
		return new Date(ts);
	}

	public static Date newDateConstantFromT8(String t8) {
		try {
			return newDateFromT8(t8);
		} catch (final ArgonFormatException ex) {
			throw new IllegalArgumentException("invalid t8 literal>" + ex + "<");
		}
	}

	public static Date newDateConstantFromTX(String t) {
		return newDateConstantFromT8(padT8(t));
	}

	public static Date newDateFromT8(String t8)
			throws ArgonFormatException {
		return new Date(newTsFromT8(t8));
	}

	public static Date newDateFromTX(String t)
			throws ArgonFormatException {
		return new Date(newTsFromTX(t));
	}

	public static Date newInstance(String qSpec, Pattern oRegex, int[] ozptGroupReorder, TimeZone timeZone)
			throws ArgonFormatException, ArgonApiException {
		return newInstance(qSpec, oRegex, ozptGroupReorder, timeZone, null);
	}

	public static Date newInstance(String qSpec, Pattern oRegex, int[] ozptGroupReorder, TimeZone timeZone, Date oNow)
			throws ArgonFormatException, ArgonApiException {
		if (qSpec == null || qSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (timeZone == null) throw new IllegalArgumentException("object is null");

		final Pattern regex = oRegex == null ? DefaultFields : oRegex;
		final String ztwSpec = qSpec.trim();
		final int len = ztwSpec.length();
		if (len == 0) throw new ArgonFormatException("Date string is whitespace>" + qSpec + "<");

		final String[] xptParts;
		final Matcher matcher = regex.matcher(ztwSpec);
		if (!matcher.find()) {
			final String m = "Date string '" + ztwSpec + "'does not match regex";
			throw new ArgonFormatException(m);
		}
		final int groupCount = matcher.groupCount();
		if (groupCount == 0) {
			xptParts = new String[1];
			xptParts[0] = ztwSpec;
		} else {
			if (ozptGroupReorder == null || ozptGroupReorder.length <= 1) {
				xptParts = new String[groupCount];
				for (int group = 1; group <= groupCount; group++) {
					xptParts[group - 1] = matcher.group(group);
				}
			} else {
				final int reorderCount = ozptGroupReorder.length;
				xptParts = new String[reorderCount];
				for (int i = 0; i < reorderCount; i++) {
					final int group = ozptGroupReorder[i];
					if (group > groupCount) {
						final String m = "Date parse reorder array contains invalid group number '" + group
								+ "' at index " + i + "; pattern declares " + groupCount + " groups";
						throw new ArgonApiException(m);
					}
					xptParts[i] = matcher.group(group);
				}
			}
		}
		return newInstance(ztwSpec, xptParts, timeZone, oNow);
	}

	public static Date newInstance(String ozSpec, String[] xptParts, TimeZone timeZone)
			throws ArgonFormatException {
		if (xptParts == null || xptParts.length == 0) throw new IllegalArgumentException("array is null or empty");
		if (timeZone == null) throw new IllegalArgumentException("object is null");
		final String qSpec = (ozSpec == null || ozSpec.length() == 0) ? UArgon.msgComma(xptParts) : ozSpec;
		final int partCount = xptParts.length;
		ParseState state = ParseState.Century;
		String zCC = "";
		String zYY = "";
		String zMM = "";
		String zDD = "";
		String qHH = "00";
		String qNN = "00";
		String qSS = "00";
		String qTTT = "000";
		boolean isAM = false;
		boolean isPM = false;
		TimeZone effTimeZone = timeZone;

		int ipart = 0;
		while (ipart < partCount) {
			final String ztwPart = xptParts[ipart].trim();
			final int len = ztwPart.length();
			if (len == 0) {
				continue;
			}
			final String qtwPart = ztwPart;
			boolean consumed = true;
			switch (state) {
				case Century: {
					if (len % 2 == 1 || len > 14) {
						final String m = msgBadPart(qSpec, "Expecting a century or CCYY[MM[DD[HH[MM[SS]]]]] date/time",
								ipart, qtwPart);
						throw new ArgonFormatException(m);
					}
					zCC = qtwPart.substring(0, 2);
					state = ParseState.YearOfCentury;
					if (len >= 4) {
						zYY = qtwPart.substring(2, 4);
						state = ParseState.MonthOfYear;
					}
					if (len >= 6) {
						zMM = qtwPart.substring(4, 6);
						state = ParseState.DayOfMonth;
					}
					if (len >= 8) {
						zDD = qtwPart.substring(6, 8);
						state = ParseState.HourOfDay;
					}
					if (len >= 10) {
						qHH = qtwPart.substring(8, 10);
						state = ParseState.MinuteOfHour;
					}
					if (len >= 12) {
						qNN = qtwPart.substring(10, 12);
						state = ParseState.SecondOfMinute;
					}
					if (len == 14) {
						qSS = qtwPart.substring(12, 14);
						state = ParseState.MillisecOfSecond;
					}
				}
				break;

				case YearOfCentury: {
					if (len == 2) {
						zYY = qtwPart;
						state = ParseState.MonthOfYear;
					} else {
						final String m = msgBadPart(qSpec, "Expecting a year", ipart, qtwPart);
						throw new ArgonFormatException(m);
					}
				}
				break;

				case MonthOfYear: {
					if (len <= 2) {
						zMM = qtwPart;
					} else if (len == 3) {
						zMM = selectMonth(qSpec, UArgon.MoyCodeJan0LowerCase, ipart, qtwPart);
					} else {
						zMM = selectMonth(qSpec, UArgon.MoyNameJan0LowerCase, ipart, qtwPart);
					}
					state = ParseState.DayOfMonth;
				}
				break;

				case DayOfMonth: {
					if (len <= 2) {
						zDD = qtwPart;
					} else {
						final String m = msgBadPart(qSpec, "Expecting a day of the month", ipart, qtwPart);
						throw new ArgonFormatException(m);
					}
					state = ParseState.HourOfDay;
				}
				break;

				case HourOfDay: {
					if (len <= 2) {
						qHH = qtwPart;
						state = ParseState.MinuteOfHour;
					} else if (len == 3) {
						qHH = qtwPart.substring(0, 1);
						qNN = qtwPart.substring(1, 3);
						state = ParseState.SecondOfMinute;
					} else {
						boolean badPart = true;
						final int posColon = qtwPart.indexOf(':');
						if (posColon < 0 && len == 4) {
							qHH = qtwPart.substring(0, 2);
							qNN = qtwPart.substring(2, 4);
							badPart = false;
						} else if (posColon == 2 && len == 5) {
							qHH = qtwPart.substring(0, 2);
							qNN = qtwPart.substring(3, 5);
							badPart = false;
						} else if (posColon == 1 && len == 4) {
							qHH = qtwPart.substring(0, 1);
							qNN = qtwPart.substring(2, 4);
							badPart = false;
						}
						state = ParseState.SecondOfMinute;
						if (badPart) {
							final String m = msgBadPart(qSpec, "Expecting an hour of the day", ipart, qtwPart);
							throw new ArgonFormatException(m);
						}
					}
				}
				break;

				case MinuteOfHour: {
					if (len <= 2) {
						qNN = qtwPart;
					} else {
						final String m = msgBadPart(qSpec, "Expecting an minute of the hour", ipart, qtwPart);
						throw new ArgonFormatException(m);
					}
					state = ParseState.SecondOfMinute;
				}
				break;

				case SecondOfMinute: {
					if (len == 2) {
						final String qlctwPart = qtwPart.toLowerCase();
						if (qlctwPart.equals("am")) {
							isAM = true;
							state = ParseState.ZoneOffset;
						} else if (qlctwPart.equals("pm")) {
							isPM = true;
							state = ParseState.ZoneOffset;
						} else {
							if (isDigit(qlctwPart)) {
								qSS = qtwPart;
								state = ParseState.MillisecOfSecond;
							} else {
								state = ParseState.ZoneOffset;
								consumed = false;
							}
						}
					} else {
						state = ParseState.ZoneOffset;
						consumed = false;
					}
				}
				break;

				case MillisecOfSecond: {
					if (len >= 3 && isDigit(qtwPart)) {
						qTTT = qtwPart.substring(0, Math.min(3, len));
					} else {
						consumed = false;
					}
					state = ParseState.ZoneOffset;
				}
				break;

				case ZoneOffset: {
					final String tzId = tzId(qtwPart);
					final TimeZone oZoneOffset = TimeZoneFactory.findById(tzId);
					if (oZoneOffset == null) {
						final String m = msgBadPart(qSpec, "Expecting a timezone offset (+|-)hours[[:]minutes]", ipart,
								qtwPart);
						throw new ArgonFormatException(m);
					}
					effTimeZone = oZoneOffset;
					state = ParseState.Exhausted;
				}
				break;

				case Exhausted: {
					final String m = msgBadPart(qSpec, "Unexpected", ipart, qtwPart);
					throw new ArgonFormatException(m);
				}

				default:
					throw new IllegalStateException("invalid state>" + state + "<");
			}
			if (consumed) {
				ipart++;
			}
		}

		if (zCC.length() == 0) throw new ArgonFormatException(msgMissing(qSpec, "century"));
		if (zYY.length() == 0) throw new ArgonFormatException(msgMissing(qSpec, "year"));
		if (zMM.length() == 0) throw new ArgonFormatException(msgMissing(qSpec, "month of year"));
		if (zDD.length() == 0) throw new ArgonFormatException(msgMissing(qSpec, "day of month"));

		String field = "";
		try {
			field = "century";
			final int century = Integer.parseInt(zCC);
			field = "year";
			final int yoc = Integer.parseInt(zYY);
			final int year = (century * 100) + yoc;
			field = "month of year";
			final int monthJan1 = Integer.parseInt(zMM);
			if (monthJan1 < 1 || monthJan1 > 12) throw new ArgonFormatException(msgBounds(qSpec, field, monthJan1));
			field = "day of month";
			final int day = Integer.parseInt(zDD);
			if (!UArgon.isValidDay(year, monthJan1, day)) throw new ArgonFormatException(msgBounds(qSpec, field, day));
			field = "hour of day";
			int hour = Integer.parseInt(qHH);
			if (hour > 23) throw new ArgonFormatException(msgBounds(qSpec, field, hour));
			if (isAM) {
				if (hour > 12) throw new ArgonFormatException(msgBounds(qSpec, field, hour));
				hour = (hour == 12) ? 0 : hour;
			} else if (isPM) {
				if (hour > 12) throw new ArgonFormatException(msgBounds(qSpec, field, hour));
				hour = (hour == 12) ? 12 : (12 + hour);
			}
			field = "minute of hour";
			final int minute = Integer.parseInt(qNN);
			if (minute > 59) throw new ArgonFormatException(msgBounds(qSpec, field, minute));
			field = "second of minute";
			final int second = Integer.parseInt(qSS);
			if (second > 59) throw new ArgonFormatException(msgBounds(qSpec, field, second));
			field = "millisecond";
			final int millis = Integer.parseInt(qTTT);
			final Calendar cal = UArgon.newCalendar(effTimeZone);
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, monthJan1 - 1);
			cal.set(Calendar.DATE, day);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, second);
			cal.set(Calendar.MILLISECOND, millis);
			return cal.getTime();
		} catch (final NumberFormatException ex) {
			throw new ArgonFormatException(msgNonNumeric(qSpec, field));
		} catch (final IllegalArgumentException exIA) {
			throw new ArgonFormatException(msgNotStrict(qSpec));
		}
	}

	public static Date newInstance(String ozSpec, String[] xptParts, TimeZone timeZone, Date oNow)
			throws ArgonFormatException {

		if (oNow != null && xptParts.length == 3) {
			final TimeFactors nowF = TimeFactors.newInstance(oNow, timeZone);
			return newRecent(xptParts[0], xptParts[1], xptParts[2], timeZone, nowF.year, nowF.moyJan1());
		}

		return newInstance(ozSpec, xptParts, timeZone);
	}

	public static Date newInstance(String qSpec, TimeZone timeZone)
			throws ArgonFormatException, ArgonApiException {
		return newInstance(qSpec, null, null, timeZone);
	}

	public static Date newInstance(String[] xptParts, TimeZone timeZone)
			throws ArgonFormatException {
		return newInstance(null, xptParts, timeZone);
	}

	public static Date newRecent(String qtwMoy, String qtwDom, String qtwYearOrHM, TimeZone timeZone, int nowYYYY, int nowMM)
			throws ArgonFormatException {
		if (qtwMoy == null || qtwMoy.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qtwDom == null || qtwDom.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qtwYearOrHM == null || qtwYearOrHM.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String[] xptParts = new String[4];
		if (isYear(qtwYearOrHM)) {
			xptParts[0] = qtwYearOrHM;
			xptParts[1] = qtwMoy;
			xptParts[2] = qtwDom;
			xptParts[3] = "0000";
		} else {
			final int moy1 = moy(qtwMoy);
			if (moy1 == 0) throw new ArgonFormatException("Unrecognised month code '" + qtwMoy + "'");
			final boolean lastYear = moy1 > (nowMM + 1);
			final int year = lastYear ? nowYYYY - 1 : nowYYYY;
			xptParts[0] = UArgon.intToDec4(year);
			xptParts[1] = qtwMoy;
			xptParts[2] = qtwDom;
			xptParts[3] = qtwYearOrHM;
		}
		return newInstance(null, xptParts, timeZone);
	}

	public static long newTsConstantFromT8(String t8) {
		try {
			return newTsFromT8(t8);
		} catch (final ArgonFormatException ex) {
			throw new IllegalArgumentException("invalid t8 literal>" + ex + "<");
		}
	}

	public static long newTsConstantFromTX(String t) {
		try {
			return newTsFromTX(t);
		} catch (final ArgonFormatException ex) {
			throw new IllegalArgumentException("invalid t8 literal>" + ex + "<");
		}
	}

	public static long newTsFromT8(String t8)
			throws ArgonFormatException {
		if (t8 == null) throw new IllegalArgumentException("object is null");

		final int len = t8.length();
		if (len != 20) throw new ArgonFormatException("Expecting '" + t8 + "' to be 20 characters long");

		final Calendar cal = UArgon.newGMTCalendar();
		String field = "";
		try {
			field = "YYYY";
			final int year = Integer.parseInt(t8.substring(0, 4));
			field = "MM";
			final int monthJan1 = Integer.parseInt(t8.substring(4, 6));
			field = "DD";
			final int day = Integer.parseInt(t8.substring(6, 8));
			field = "DateTimeSeparator";
			final char dtsep = t8.charAt(8);
			if (dtsep != CArgon.DATE_SEPARATOR_DAYHOUR)
				throw new ArgonFormatException("Illegal date-time separator '" + dtsep + "' in '" + t8 + "'");
			field = "hh";
			final int hour = Integer.parseInt(t8.substring(9, 11));
			field = "mm";
			final int minute = Integer.parseInt(t8.substring(11, 13));
			field = "MinuteSecondSeparator";
			final char mssep = t8.charAt(13);
			if (mssep != CArgon.DATE_SEPARATOR_MINSEC)
				throw new ArgonFormatException("Illegal minute-second separator '" + mssep + "' in '" + t8 + "'");
			field = "ss";
			final int second = Integer.parseInt(t8.substring(14, 16));
			field = "SecondMilliSeparator";
			final char smsep = t8.charAt(16);
			if (smsep != CArgon.DATE_SEPARATOR_SECMILLI)
				throw new ArgonFormatException("Illegal second-millisecond separator '" + mssep + "' in '" + t8 + "'");
			field = "ttt";
			final int millis = Integer.parseInt(t8.substring(17, 20));
			if (monthJan1 < 1 || monthJan1 > 12)
				throw new ArgonFormatException("Illegal month '" + monthJan1 + "' in '" + t8 + "'");
			if (day < 1 || day > 31) throw new ArgonFormatException("Illegal day value '" + day + "' in '" + t8 + "'");
			if (hour > 23) throw new ArgonFormatException("Illegal hour value '" + hour + "' in '" + t8 + "'");
			if (minute > 59) throw new ArgonFormatException("Illegal minute value '" + minute + "' in '" + t8 + "'");
			if (second > 59) throw new ArgonFormatException("Illegal second value '" + second + "' in '" + t8 + "'");
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, monthJan1 - 1);
			cal.set(Calendar.DATE, day);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, second);
			cal.set(Calendar.MILLISECOND, millis);
			return cal.getTimeInMillis();
		} catch (final NumberFormatException exNF) {
			throw new ArgonFormatException("Non-numeric " + field + " field in '" + t8 + "'");
		} catch (final IllegalArgumentException exIA) {
			throw new ArgonFormatException("Date '" + t8 + "' fails strictness test for month and day");
		}
	}

	public static long newTsFromTX(String t)
			throws ArgonFormatException {
		return newTsFromT8(padT8(t));
	}

	public static long newTsNowConstantFromTX(String t) {
		try {
			final long tsNow = newTsFromTX(t);
			ArgonClock.simulatedNow(tsNow);
			return tsNow;
		} catch (final ArgonFormatException ex) {
			throw new IllegalArgumentException("invalid t8 literal>" + ex + "<");
		}
	}

	private DateFactory() {
	}

	private static enum ParseState {
		Century,
		YearOfCentury,
		MonthOfYear,
		DayOfMonth,
		HourOfDay,
		MinuteOfHour,
		SecondOfMinute,
		MillisecOfSecond,
		ZoneOffset,
		Exhausted;
	}
}
