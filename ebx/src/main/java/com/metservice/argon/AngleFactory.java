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
				final String p = "character index " + i + "(" + ch + ")";
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

	private static enum Fraction {
		Degrees("Degrees", 1.0), Minutes("Minutes", 60.0), Seconds("Seconds", 3600.0);

		private Fraction(String title, double divisor) {
			this.title = title;
			this.divisor = divisor;
		}
		public final String title;
		public final double divisor;
	}

	private static class ParseException extends Exception {

		public ParseException(String message) {
			super(message);
		}
	}

	private static class Parser {

		private void push(Fraction oFraction)
				throws ParseException {
			if (oFraction == null) throw new ParseException("no further fractional part allowed");
			final String zValue = m_buffer.toString();
			if (zValue.isEmpty()) throw new ParseException(oFraction.title + " value is missing");
			m_buffer.setLength(0);
			pushFraction(zValue, oFraction);
		}

		private void pushFraction(String qValue, Fraction fraction)
				throws ParseException {
			try {
				final double q = Double.parseDouble(qValue);
				m_uresult += (q / fraction.divisor);
			} catch (final NumberFormatException ex) {
				throw new ParseException(fraction.title + " numeric value '" + qValue + "' is malformed");
			}
		}

		private void pushTerminal(Fraction oFraction)
				throws ParseException {
			final String zValue = m_buffer.toString();
			if (zValue.isEmpty()) return;
			if (oFraction == null) throw new ParseException("fractional part '" + zValue + "' not allowed");
			pushFraction(zValue, oFraction);
		}

		public void degrees()
				throws ParseException {
			push(Fraction.Degrees);
			m_oDefaultFraction = Fraction.Minutes;
		}

		public void minutes()
				throws ParseException {
			push(Fraction.Minutes);
			m_oDefaultFraction = Fraction.Seconds;
		}

		public void numeric(char ch) {
			m_buffer.append(ch);
		}

		public double result() {
			return m_negated ? -m_uresult : m_uresult;
		}

		public void seconds()
				throws ParseException {
			push(Fraction.Seconds);
			m_oDefaultFraction = null;
		}

		public void sign(char ch)
				throws ParseException {
			if (m_seenSign) throw new ParseException("ambiguous sign indicator");
			m_negated = ch == '-' || ch == IndSouth || ch == IndWest;
			m_seenSign = true;
			if (m_buffer.length() > 0) {
				degrees();
			}
		}

		public void terminate()
				throws ParseException {
			pushTerminal(m_oDefaultFraction);
		}

		@Override
		public String toString() {
			final Ds ds = Ds.o("Parser");
			ds.a("buffer", m_buffer);
			ds.a("defaultFraction", m_oDefaultFraction);
			ds.a("negated", m_negated);
			ds.a("seenSign", m_seenSign);
			ds.a("uresult", m_uresult);
			return ds.ss();
		}

		Parser() {
		}
		private final StringBuilder m_buffer = new StringBuilder();
		private double m_uresult = 0.0;
		private Fraction m_oDefaultFraction = Fraction.Degrees;
		private boolean m_negated;
		private boolean m_seenSign;
	}

}
