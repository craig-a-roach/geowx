/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author roach
 */
public class TimeOfDayFactory {

	private static final Pattern RuleTermSplitter = Pattern.compile(",");

	private static String msgMal(String qtwSpec) {
		return "Time of day '" + qtwSpec + "' is not in [h]h[mm][ss] format";
	}

	private static int parseHours(String qtw)
			throws ArgonFormatException {
		try {
			final int n = Integer.parseInt(qtw);
			if (n < 0) throw new ArgonFormatException("Hours field '" + qtw + "' must be >= 0");
			if (n > 23) throw new ArgonFormatException("Hours field '" + qtw + "' must be <= 23");
			return n;
		} catch (final NumberFormatException exNF) {
			throw new ArgonFormatException("Expecting a numeric hours field '" + qtw + "'");
		}
	}

	private static int parseMinutes(String qtw)
			throws ArgonFormatException {
		try {
			final int n = Integer.parseInt(qtw);
			if (n < 0) throw new ArgonFormatException("Minutes field '" + qtw + "' must be >= 0");
			if (n > 59) throw new ArgonFormatException("Minutes field '" + qtw + "' must be <= 59");
			return n;
		} catch (final NumberFormatException exNF) {
			throw new ArgonFormatException("Expecting a numeric minutes field '" + qtw + "'");
		}
	}

	private static TimeOfDayRule parseRule(String qtwSpec)
			throws ArgonApiException {
		final String[] zptTerms = RuleTermSplitter.split(qtwSpec);
		if (zptTerms.length == 0) throw new ArgonApiException("Time of day expression is empty>" + qtwSpec + "<");
		return parseRule(zptTerms);
	}

	private static TimeOfDayRule parseRule(String[] xptzTerms)
			throws ArgonApiException {
		final List<TimeOfDayRuleTerm> zlTerms = new ArrayList<TimeOfDayRuleTerm>();
		for (int i = 0; i < xptzTerms.length; i++) {
			final String ztwTerm = xptzTerms[i].trim();
			if (ztwTerm.length() > 0) {
				final TimeOfDayRuleTerm term = TimeOfDayRuleTerm.newTerm(ztwTerm);
				zlTerms.add(term);
			}
		}
		final int termCount = zlTerms.size();
		if (termCount == 0) throw new ArgonApiException("Time of day expression does not contain any terms");
		final TimeOfDayRuleTerm[] xptTerms = zlTerms.toArray(new TimeOfDayRuleTerm[termCount]);
		return new TimeOfDayRule(xptTerms);
	}

	private static int parseSecondOfDay(String qtwSpec)
			throws ArgonFormatException {
		final int len = qtwSpec.length();
		if (len > 6) throw new ArgonFormatException(msgMal(qtwSpec));
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		int posStart = 0;
		int posEnd = (len == 1 || len == 3 || len == 5) ? 1 : 2;
		hours = parseHours(qtwSpec.substring(posStart, posEnd));
		posStart = posEnd;
		posEnd += 2;
		if (posEnd <= len) {
			minutes = parseMinutes(qtwSpec.substring(posStart, posEnd));
			posStart = posEnd;
			posEnd += 2;
		}
		if (posEnd <= len) {
			seconds = parseSeconds(qtwSpec.substring(posStart, posEnd));
		}

		return (((hours * 60) + minutes) * 60) + seconds;
	}

	private static int parseSeconds(String qtw)
			throws ArgonFormatException {
		try {
			final int n = Integer.parseInt(qtw);
			if (n < 0) throw new ArgonFormatException("Seconds field '" + qtw + "' must be >= 0");
			if (n > 59) throw new ArgonFormatException("Seconds field '" + qtw + "' must be <= 59");
			return n;
		} catch (final NumberFormatException exNF) {
			throw new ArgonFormatException("Expecting a numeric minutes field '" + qtw + "'");
		}
	}

	public static String formatSecondOfDay(int sod) {
		int secresidue = sod;
		final int hod = secresidue / CArgon.SEC_PER_HR;
		secresidue = secresidue - (hod * CArgon.SEC_PER_HR);
		final int moh = secresidue / CArgon.SEC_PER_MIN;
		secresidue = secresidue - (moh * CArgon.SEC_PER_MIN);
		final int som = secresidue;
		final StringBuilder sb = new StringBuilder();
		sb.append(UArgon.intToDec2(hod));
		sb.append(UArgon.intToDec2(moh));
		sb.append(UArgon.intToDec2(som));
		return sb.toString();
	}

	public static TimeOfDayRule newRule(String qRuleSpec)
			throws ArgonApiException {
		if (qRuleSpec == null || qRuleSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String ztwRuleSpec = qRuleSpec.trim();
		final int len = ztwRuleSpec.length();
		if (len == 0) throw new ArgonApiException("Time of day rule string is whitespace>" + qRuleSpec + "<");
		return parseRule(ztwRuleSpec);
	}

	public static TimeOfDayRule newRule(String[] xptTerms)
			throws ArgonApiException {
		if (xptTerms == null || xptTerms.length == 0) throw new IllegalArgumentException("array is null or empty");
		return parseRule(xptTerms);
	}

	public static TimeOfDayRule newRuleConstant(String qRuleSpec) {
		try {
			return newRule(qRuleSpec);
		} catch (final ArgonApiException ex) {
			throw new IllegalArgumentException("invalid rule spec literal>" + ex + "<");
		}
	}

	public static int secondOfDay(String qSpec)
			throws ArgonFormatException {
		if (qSpec == null || qSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String ztwSpec = qSpec.trim();
		final int len = ztwSpec.length();
		if (len == 0) throw new ArgonFormatException("Time of day string is whitespace>" + qSpec + "<");
		return parseSecondOfDay(ztwSpec);
	}

	private TimeOfDayFactory() {
	}

}
