/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.TimeZone;

/**
 * @author roach
 */
public class TimeZoneFormatter {

	public static String gmtStdDST(TimeZone tz) {
		return "GMT" + stdDST(tz, true);
	}

	public static String id(TimeZone tz) {
		if (tz == null) throw new IllegalArgumentException("object is null");
		return tz.getID();
	}

	public static String std(TimeZone tz) {
		return std(tz, false);
	}

	public static String std(TimeZone tz, boolean zeroToEmpty) {
		if (tz == null) throw new IllegalArgumentException("object is null");
		final int rawOffsetMins = tz.getRawOffset() / CArgon.MS_PER_MIN;
		if (zeroToEmpty && rawOffsetMins == 0) return "";
		final String sgn;
		final int absMins;
		if (rawOffsetMins == 0) {
			sgn = "";
			absMins = 0;
		} else if (rawOffsetMins < 0) {
			sgn = "-";
			absMins = -rawOffsetMins;
		} else {
			sgn = "+";
			absMins = rawOffsetMins;
		}
		final int absHours = absMins / 60;
		final int absMinsRem = absMins - (absHours * 60);
		final StringBuilder sb = new StringBuilder();
		sb.append(sgn);
		sb.append(UArgon.intToDec2(absHours));
		sb.append(':');
		sb.append(UArgon.intToDec2(absMinsRem));
		return sb.toString();
	}

	public static String stdDST(TimeZone tz) {
		return stdDST(tz, false);
	}

	public static String stdDST(TimeZone tz, boolean zeroToEmpty) {
		final String std = std(tz, zeroToEmpty);
		final String zDST = zDST(tz);
		return std + zDST;
	}

	public static String zDST(TimeZone tz) {
		if (tz == null) throw new IllegalArgumentException("object is null");
		final int dstSavingsMins = tz.getDSTSavings() / CArgon.MS_PER_MIN;
		if (dstSavingsMins == 0) return "";
		if (dstSavingsMins == 60) return "+1h";
		return "+" + dstSavingsMins + "m";
	}

	private TimeZoneFormatter() {
	}

}
