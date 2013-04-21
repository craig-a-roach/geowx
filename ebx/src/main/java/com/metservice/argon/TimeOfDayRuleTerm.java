/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Arrays;

/**
 * @author roach
 */
public class TimeOfDayRuleTerm {

	public String format() {
		final StringBuilder sb = new StringBuilder();
		sb.append(TimeOfDayFactory.formatSecondOfDay(m_refSecOfDay));
		if (m_secRepeatEvery > 0) {
			sb.append('+');
			sb.append(ElapsedFormatter.formatSingleUnit(m_secRepeatEvery * CArgon.MS_PER_SEC));
			sb.append('*');
			sb.append(m_repeatCount);
		}
		return sb.toString();
	}

	public int[] secondsOfDayAsc() {
		if (m_secRepeatEvery == 0L || m_repeatCount == 0) {
			final int[] xpt = new int[1];
			xpt[0] = m_refSecOfDay;
			return xpt;
		}

		final int[] xptAsc = new int[1 + m_repeatCount];
		xptAsc[0] = m_refSecOfDay;
		int sod = m_refSecOfDay;
		for (int i = 0; i < m_repeatCount; i++) {
			sod = (sod + m_secRepeatEvery) % CArgon.SEC_PER_DAY;
			xptAsc[1 + i] = sod;
		}
		Arrays.sort(xptAsc);
		return xptAsc;
	}

	@Override
	public String toString() {
		return format();
	}

	private static String msgBadField(String qSpec, String qtwField, String fieldName, String oqcause) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Time of day rule term '").append(qSpec).append("'");
		sb.append(" has a malformed ").append(fieldName).append(" field value '").append(qtwField).append("'");
		if (oqcause != null) {
			sb.append("; ").append(oqcause);
		}
		return sb.toString();
	}

	private static int parseRefSecOfDay(String qSpec, String qtwField)
			throws ArgonApiException {
		try {
			return TimeOfDayFactory.secondOfDay(qtwField);
		} catch (final ArgonFormatException ex) {
			throw new ArgonApiException(msgBadField(qSpec, qtwField, "reference time of day", ex.getMessage()));
		}
	}

	private static int parseRepeatEveryPositiveSec(String qSpec, String qtwField)
			throws ArgonApiException {
		int secRepeatEvery = 0;
		try {
			final long msRepeatEvery = ElapsedFactory.ms(qtwField);
			secRepeatEvery = (int) ((msRepeatEvery / 1000L) % CArgon.MS_PER_DAY);
		} catch (final ArgonFormatException ex) {
			throw new ArgonApiException(msgBadField(qSpec, qtwField, "repeat interval", ex.getMessage()));
		}
		if (secRepeatEvery <= 0) throw new ArgonApiException(msgBadField(qSpec, qtwField, "repeat interval", "must be > 0"));
		return secRepeatEvery;
	}

	private static int parseRepeatPositiveCount(String qSpec, String qtwField)
			throws ArgonApiException {
		int n = 0;
		try {
			n = Integer.parseInt(qtwField);
		} catch (final NumberFormatException ex) {
			throw new ArgonApiException(msgBadField(qSpec, qtwField, "repeat count", "non-numeric"));
		}
		if (n <= 0) throw new ArgonApiException(msgBadField(qSpec, qtwField, "repeat count", "must be > 0"));
		return n;
	}

	public static TimeOfDayRuleTerm newTerm(String qSpec)
			throws ArgonApiException {
		if (qSpec == null || qSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String ztwSpec = qSpec.trim();
		final int len = ztwSpec.length();
		if (len == 0) throw new ArgonApiException("Time of day rule term string is whitespace>" + qSpec + "<");

		final StringBuilder bRef = new StringBuilder();
		final StringBuilder bRepeatEvery = new StringBuilder();
		final StringBuilder bRepeatCount = new StringBuilder();
		ParseState state = ParseState.Ref;
		for (int i = 0; i < len; i++) {
			final char ch = ztwSpec.charAt(i);
			if (ch == '+') {
				state = ParseState.RepeatEvery;
			} else if (ch == '*') {
				state = ParseState.RepeatCount;
			} else {
				switch (state) {
					case Ref:
						bRef.append(ch);
					break;
					case RepeatEvery:
						bRepeatEvery.append(ch);
					break;
					case RepeatCount:
						bRepeatCount.append(ch);
					break;
				}
			}
		}
		final String ztwRef = bRef.toString().trim();
		if (ztwRef.length() == 0)
			throw new ArgonApiException("Time of day rule term '" + qSpec + "' is missing a reference time of day");
		final int refSecOfDay = parseRefSecOfDay(qSpec, ztwRef);

		int secRepeatEvery = 0;
		int repeatCount = 0;
		final String ztwRepeatEvery = bRepeatEvery.toString().trim();
		if (ztwRepeatEvery.length() > 0) {
			secRepeatEvery = parseRepeatEveryPositiveSec(qSpec, ztwRepeatEvery);
			int repeatUnboundPlusCount = 1;
			final String ztwRepeatCount = bRepeatCount.toString().trim();
			if (ztwRepeatCount.length() > 0) {
				repeatUnboundPlusCount = parseRepeatPositiveCount(qSpec, ztwRepeatCount);
			}
			final int maxRepeat = (CArgon.SEC_PER_DAY - 1) / secRepeatEvery;
			repeatCount = Math.min(repeatUnboundPlusCount, maxRepeat);
		}

		return new TimeOfDayRuleTerm(refSecOfDay, secRepeatEvery, repeatCount);
	}

	private TimeOfDayRuleTerm(int refSecOfDay, int secRepeatEvery, int repeatCount) {
		m_refSecOfDay = refSecOfDay;
		m_secRepeatEvery = secRepeatEvery;
		m_repeatCount = repeatCount;
	}

	private final int m_refSecOfDay;
	private final int m_secRepeatEvery;
	private final int m_repeatCount;

	private static enum ParseState {
		Ref, RepeatEvery, RepeatCount;
	}
}
