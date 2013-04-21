/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.concurrent.TimeUnit;

/**
 * @author roach
 */
public class ElapsedFactory {

	private static boolean isZero(String qlctw) {
		return qlctw.equals("0") || qlctw.equals("+0") || qlctw.equals("-0");
	}

	private static String msgMalformed(String src, int pos, String reason) {
		return "Malformed elapsed time expression '" + src + "'; " + reason + " at position " + pos;
	}

	private static long msQty(StringBuilder bqty, int pos, ElapsedUnit unit, boolean validateOnly)
			throws ArgonFormatException {
		final String ztwQty = bqty.toString();
		bqty.setLength(0);
		if (validateOnly) return 0L;
		try {
			final long n = Long.parseLong(ztwQty);
			return n * unit.ms;
		} catch (final NumberFormatException ex) {
			throw new ArgonFormatException(msgMalformed(ztwQty, pos, "non-numeric '" + unit + "' quantity '" + ztwQty + "'"));
		}
	}

	private static long parseMs(String qlctw, boolean validateOnly)
			throws ArgonFormatException {
		if (isZero(qlctw)) return 0L;

		final int len = qlctw.length();
		final StringBuilder bqty = new StringBuilder();
		boolean seenSign = false;
		boolean isNegative = false;
		long ms = 0L;
		for (int i = 0; i < len; i++) {
			final char ch = qlctw.charAt(i);
			switch (ch) {
				case CArgon.ELAPSED_UNIT_LDAYS:
					ms += msQty(bqty, i, ElapsedUnit.Days, validateOnly);
				break;
				case CArgon.ELAPSED_UNIT_LHOURS:
					ms += msQty(bqty, i, ElapsedUnit.Hours, validateOnly);
				break;
				case CArgon.ELAPSED_UNIT_LMINUTES:
					ms += msQty(bqty, i, ElapsedUnit.Minutes, validateOnly);
				break;
				case CArgon.ELAPSED_UNIT_LSECONDS:
					ms += msQty(bqty, i, ElapsedUnit.Seconds, validateOnly);
				break;
				case CArgon.ELAPSED_UNIT_LMILLISECONDS:
					ms += msQty(bqty, i, ElapsedUnit.Milliseconds, validateOnly);
				break;
				case '+':
					if (seenSign) throw new ArgonFormatException(msgMalformed(qlctw, i, "duplicate sign"));
					seenSign = true;
				break;
				case '-':
					if (seenSign) throw new ArgonFormatException(msgMalformed(qlctw, i, "duplicate sign"));
					isNegative = true;
					seenSign = true;
				break;
				default:
					if (Character.isDigit(ch)) {
						bqty.append(ch);
					} else {
						final String m = msgMalformed(qlctw, i, "unknown elapsed time unit '" + ch + "'");
						throw new ArgonFormatException(m);
					}
			}
		}
		if (bqty.length() > 0) {
			final String m = msgMalformed(qlctw, len, "missing time unit after '" + bqty + "'");
			throw new ArgonFormatException(m);
		}
		return isNegative ? -ms : ms;
	}

	public static boolean isWellFormed(String zSpec) {
		if (zSpec == null) throw new IllegalArgumentException("object is null");
		final String zlctwSpec = zSpec.trim().toLowerCase();
		final int len = zlctwSpec.length();
		if (len == 0) return false;
		boolean isWellFormed = false;
		try {
			parseMs(zlctwSpec, true);
			isWellFormed = true;
		} catch (final ArgonFormatException ex) {
		}
		return isWellFormed;
	}

	public static long ms(String qSpec)
			throws ArgonFormatException {
		if (qSpec == null || qSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String zlctwSpec = qSpec.trim().toLowerCase();
		final int len = zlctwSpec.length();
		if (len == 0) throw new ArgonFormatException("Elapsed time string is whitespace>" + qSpec + "<");
		return parseMs(zlctwSpec, false);
	}

	public static Elapsed newElapsed(long unitCount, TimeUnit unit) {
		if (unit == null) throw new IllegalArgumentException("object is null");
		return new Elapsed(unit.toMillis(unitCount));
	}

	public static Elapsed newElapsed(String qSpec)
			throws ArgonFormatException {
		return new Elapsed(ms(qSpec));
	}

	public static Elapsed newElapsedConstant(String qSpec) {
		try {
			return newElapsed(qSpec);
		} catch (final ArgonFormatException ex) {
			throw new IllegalArgumentException("invalid elapsed literal>" + qSpec + "<");
		}
	}

	private ElapsedFactory() {
	}
}
