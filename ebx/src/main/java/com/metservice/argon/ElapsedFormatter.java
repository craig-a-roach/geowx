/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class ElapsedFormatter {

	private static long signedUnitCount(long sms, ElapsedUnit unit, boolean round) {
		final boolean isNegative = sms < 0L;
		final long ams = isNegative ? -sms : sms;

		final long msAdjust = round ? (unit.ms / 2L) : 0L;
		final long aunitCount = (ams + msAdjust) / unit.ms;
		final long sunitCount = isNegative ? -aunitCount : aunitCount;
		return sunitCount;
	}

	public static String formatMixedUnits(long sms) {
		if (sms == 0L) return "0";
		final boolean isNegative = sms < 0L;
		long residue = isNegative ? -sms : sms;
		final long dayCount = residue / CArgon.MS_PER_DAY;
		residue = residue - (dayCount * CArgon.MS_PER_DAY);
		final long hourCount = residue / CArgon.MS_PER_HR;
		residue = residue - (hourCount * CArgon.MS_PER_HR);
		final long minuteCount = residue / CArgon.MS_PER_MIN;
		residue = residue - (minuteCount * CArgon.MS_PER_MIN);
		final long secondCount = residue / CArgon.MS_PER_SEC;
		residue = residue - (secondCount * CArgon.MS_PER_SEC);
		final long millisecondCount = residue;

		final StringBuilder sb = new StringBuilder();
		if (isNegative) {
			sb.append('-');
		}
		if (dayCount > 0L) {
			sb.append(dayCount);
			sb.append(CArgon.ELAPSED_UNIT_LDAYS);
		}
		if (hourCount > 0L) {
			sb.append(hourCount);
			sb.append(CArgon.ELAPSED_UNIT_LHOURS);
		}
		if (minuteCount > 0L) {
			sb.append(minuteCount);
			sb.append(CArgon.ELAPSED_UNIT_LMINUTES);
		}
		if (secondCount > 0L) {
			sb.append(secondCount);
			sb.append(CArgon.ELAPSED_UNIT_LSECONDS);
		}
		if (millisecondCount > 0L) {
			sb.append(millisecondCount);
			sb.append(CArgon.ELAPSED_UNIT_LMILLISECONDS);
		}
		return sb.toString();
	}

	public static String formatSecsMixedUnits(int ssecs) {
		return formatMixedUnits(ssecs * CArgon.LMS_PER_SEC);
	}

	public static String formatSecsSingleUnit(int ssecs) {
		return formatSingleUnit(ssecs * CArgon.LMS_PER_SEC);
	}

	public static String formatSecsUnit(int ssecs, ElapsedUnit unit, boolean round) {
		return formatUnit(ssecs * CArgon.LMS_PER_SEC, unit, round);
	}

	public static String formatSingleUnit(long sms) {
		if (sms == 0L) return "0";
		final boolean isNegative = sms < 0L;
		final long ams = isNegative ? -sms : sms;
		if ((ams % CArgon.MS_PER_SEC) > 0L) return formatUnit(sms, ElapsedUnit.Milliseconds, false);
		if ((ams % CArgon.MS_PER_MIN) > 0L) return formatUnit(sms, ElapsedUnit.Seconds, false);
		if ((ams % CArgon.MS_PER_HR) > 0L) return formatUnit(sms, ElapsedUnit.Minutes, false);
		if ((ams % CArgon.MS_PER_DAY) > 0L) return formatUnit(sms, ElapsedUnit.Hours, false);
		return formatUnit(sms, ElapsedUnit.Days, false);
	}

	public static String formatUnit(long sms, ElapsedUnit unit, boolean round) {
		if (unit == null) throw new IllegalArgumentException("object is null");
		final StringBuilder sb = new StringBuilder();
		sb.append(signedUnitCount(sms, unit, round));
		sb.append(unit.lcsuffix);
		return sb.toString();
	}

	public static String formatUnitNoSuffix(long sms, ElapsedUnit unit, boolean round) {
		if (unit == null) throw new IllegalArgumentException("object is null");
		return Long.toString(signedUnitCount(sms, unit, round));
	}

	private ElapsedFormatter() {
	}

}
