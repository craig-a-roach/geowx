/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class AngleFactory {

	private static final char IndDeg0 = 'd';
	private static final char IndDeg1 = 'o';
	private static final char IndDeg2 = '\u00B0';
	private static final char IndMin0 = 'm';
	private static final char IndMin1 = '\'';
	private static final char IndSec0 = 's';
	private static final char IndSec1 = '\"';

	private static final char IndNorth = 'N';
	private static final char IndEast = 'E';
	private static final char IndSouth = 'S';
	private static final char IndWest = 'W';

	private static final double DivDeg = 1.0;
	private static final double DivMin = 60.0;
	private static final double DivSec = 3600.0;

	public static double newDegrees(String qSpec)
			throws ArgonFormatException {
		if (qSpec == null || qSpec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String ztwSpec = qSpec.trim();
		final int len = ztwSpec.length();
		if (len == 0) throw new ArgonFormatException("Angle string is whitespace>" + qSpec + "<");

		final Parser parser = new Parser();
		for (int i = 0; i < len; i++) {
			final char ch = ztwSpec.charAt(i);
			try {
				if (ArgonText.isDigit(ch)) {
					parser.numeric(ch);
				} else {
					switch (ch) {
						case '.':
							parser.numeric(ch);
						break;
						case '+':
						case '-':
						case IndNorth:
						case IndEast:
						case IndSouth:
						case IndWest:
							parser.sign(ch);
						break;
						case IndDeg0:
						case IndDeg1:
						case IndDeg2:
							parser.degrees();
						break;
						case IndMin0:
						case IndMin1:
							parser.minutes();
						break;
						case IndSec0:
						case IndSec1:
							parser.seconds();
						break;
						default:
							throw new ParseException("invalid character");
					}
				}
			} catch (final ParseException ex) {
				final String h = "Angle '" + ztwSpec + " is malformed";
				final String p = " position " + i + "(" + ch + ")";
				final String m = h + "..." + ex.getMessage() + " at " + p;
				throw new ArgonFormatException(m);
			}
		}
		try {
			parser.terminate();
		} catch (final ParseException ex) {
			final String h = "Angle '" + ztwSpec + " is malformed";
			final String m = h + "..." + ex.getMessage();
			throw new ArgonFormatException(m);
		}
		return parser.result();
	}

	private static class ParseException extends Exception {

		public ParseException(String message) {
			super(message);
		}
	}

	private static class Parser {

		private void push(double divisor)
				throws ParseException {
			final String zValue = m_buffer.toString();
			m_buffer.setLength(0);
			if (zValue.length() == 0) return;
			try {
				final double q = Double.parseDouble(zValue);
				final double term = q / divisor;
				final double sgnTerm = m_negated ? -term : term;
				m_result = sgnTerm;
			} catch (final NumberFormatException ex) {
				throw new ParseException("malformed numeric component of angle");
			}
		}

		public void degrees()
				throws ParseException {
			push(DivDeg);
			m_divDefault = DivMin;
		}

		public void minutes()
				throws ParseException {
			push(DivMin);
			m_divDefault = DivSec;
		}

		public void numeric(char ch) {
			m_buffer.append(ch);
		}

		public double result() {
			return m_result;
		}

		public void seconds()
				throws ParseException {
			push(DivSec);
			m_divDefault = DivSec;
		}

		public void sign(char ch)
				throws ParseException {
			if (m_seenSign) throw new ParseException("ambiguous sign indicator");
			m_negated = ch == '-' || ch == IndSouth || ch == IndWest;
			m_seenSign = true;
		}

		public void terminate()
				throws ParseException {
			push(m_divDefault);
		}

		Parser() {
		}
		private final StringBuilder m_buffer = new StringBuilder();
		private double m_result = 0.0;
		private double m_divDefault = DivDeg;
		private boolean m_negated;
		private boolean m_seenSign;
	}

}
