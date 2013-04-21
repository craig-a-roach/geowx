/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class DecimalMask {
	public String format(double value) {
		return m_mask.format(value, 0.0f);
	}

	public String format(long value) {
		return m_mask.format(value);
	}

	public String format(Real value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		final double central = value.central();
		final float erm = value.errorMagnitude();
		return m_mask.format(central, erm);
	}

	public String mask() {
		return m_maskPattern;
	}

	@Override
	public String toString() {
		return m_maskPattern;
	}

	private static int popInteger(String maskPattern, int pos, StringBuilder sb)
			throws ArgonApiException {
		if (sb.length() == 0) return 0;

		try {
			final String q = sb.toString();
			sb.setLength(0);
			return Integer.parseInt(q);
		} catch (final NumberFormatException exNF) {
			throw new ArgonApiException("Malformed maskPattern '" + maskPattern + "' at position " + pos);
		}
	};

	static double pow10(int exponent) {
		return (exponent >= 0 && exponent < POW10.length) ? POW10[exponent] : Math.pow(10.0, exponent);
	}

	static String zeroPad(int digits) {
		if (digits < ZEROPAD.length) return ZEROPAD[digits];
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < digits; i++) {
			b.append('0');
		}
		return b.toString();
	}

	public static DecimalMask newFixed(boolean signedPositive, boolean signedZero, int leadCount, boolean leadZero,
			boolean naturalFraction, int fractionDigits) {
		final Fixed fixed = new Fixed(naturalFraction, fractionDigits);
		fixed.enableSignedPositive(signedPositive);
		fixed.enableSignedZero(signedZero);
		fixed.setLead(leadCount, leadZero);

		final StringBuilder mp = new StringBuilder();
		mp.append("Fixed");
		mp.append(signedPositive ? "+" : "");
		mp.append(signedZero ? "~" : "");
		if (leadCount > 0) {
			mp.append(leadZero ? "0" : "");
			mp.append(leadCount);
		}
		if (!naturalFraction) {
			mp.append('.');
			mp.append(fractionDigits);
		}

		return new DecimalMask(fixed, mp.toString());
	}

	public static DecimalMask newFixed(String zMaskPattern)
			throws ArgonApiException {
		if (zMaskPattern == null) throw new IllegalArgumentException("zMaskPattern is null");

		final int len = zMaskPattern.length();
		boolean signedPositive = false;
		boolean signedZero = false;
		int leadCount = 0;
		boolean leadZero = false;
		boolean naturalFraction = true;
		int fractionDigits = 0;
		FixedPatternState state = FixedPatternState.SignFlags;

		final StringBuilder nb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			final char ch = zMaskPattern.charAt(i);
			boolean error = false;
			switch (ch) {
				case '+': {
					switch (state) {
						case SignFlags: {
							signedPositive = true;
						}
						break;
						case LeadWidth: {
							error = true;
						}
						break;
						case FractionDigits: {
							error = true;
						}
						break;
					}
				}
				break;

				case '~': {
					switch (state) {
						case SignFlags: {
							signedZero = true;
						}
						break;
						case LeadWidth: {
							error = true;
						}
						break;
						case FractionDigits: {
							error = true;
						}
						break;
					}
				}
				break;

				case '0': {
					switch (state) {
						case SignFlags: {
							leadZero = true;
							state = FixedPatternState.LeadWidth;
						}
						break;
						case LeadWidth: {
							nb.append(ch);
						}
						break;
						case FractionDigits: {
							nb.append(ch);
						}
						break;
					}

				}
				break;

				case '.': {
					switch (state) {
						case SignFlags: {
							leadCount = 0;
							naturalFraction = false;
							state = FixedPatternState.FractionDigits;
						}
						break;
						case LeadWidth: {
							leadCount = popInteger(zMaskPattern, i, nb);
							naturalFraction = false;
							state = FixedPatternState.FractionDigits;
						}
						break;
						case FractionDigits: {
							error = true;
						}
						break;
					}
				}
				break;

				default: {
					if (Character.isDigit(ch)) {
						switch (state) {
							case SignFlags: {
								nb.append(ch);
								state = FixedPatternState.LeadWidth;
							}
							break;
							case LeadWidth: {
								nb.append(ch);
							}
							break;
							case FractionDigits: {
								nb.append(ch);
							}
							break;
						}
					} else {
						error = true;
					}
				}
			}
			if (error) throw new ArgonApiException("Malformed maskPattern '" + zMaskPattern + "' at position " + i);
		}// for

		switch (state) {
			case SignFlags:
			break;
			case LeadWidth: {
				leadCount = popInteger(zMaskPattern, len, nb);
			}
			break;
			case FractionDigits: {
				fractionDigits = popInteger(zMaskPattern, len, nb);
			}
			break;
		}

		return newFixed(signedPositive, signedZero, leadCount, leadZero, naturalFraction, fractionDigits);
	}

	public static DecimalMask newFloating(boolean signedPositive) {
		final Floating floating = new Floating();
		floating.enableSignedPositive(signedPositive);

		final StringBuilder mp = new StringBuilder();
		mp.append("Floating");
		mp.append(signedPositive ? "+" : "");
		return new DecimalMask(floating, mp.toString());
	}

	public static DecimalMask newFloating(String zMaskPattern)
			throws ArgonApiException {
		if (zMaskPattern == null) throw new IllegalArgumentException("zMaskPattern is null");

		final int len = zMaskPattern.length();
		boolean signedPositive = false;
		for (int i = 0; i < len; i++) {
			final char ch = zMaskPattern.charAt(i);
			if (i == 0 && ch == '+') {
				signedPositive = true;
			} else
				throw new ArgonApiException("Malformed maskPattern '" + zMaskPattern + "' at position " + i);
		}
		return newFloating(signedPositive);
	}

	/**
	 * Constructs decimal mask using a specification string.
	 * 
	 * The first character of the specification string determines the basic structure.
	 * <ul>
	 * <li>'f' indicates fixed-point</li>
	 * <li>'d' indicates floating-point</li>
	 * </ul>
	 * 
	 * Both formats will switch to scientific notation, and include an exponent, as necessary.
	 * 
	 * 
	 * <p>
	 * Here's the syntax of a fixed-point specification...
	 * </p>
	 * 
	 * <pre>
	 * pattern :== 'f' signFlags? leadWidth? fractionDigits?
	 * signFlags :== signedPositive? signedZero?
	 * signedPositive :== '+'
	 * signedZero :== '~'
	 * leadWidth :== '0'? characterCount
	 * fractionDigits :== '.' digitCount
	 *</pre>
	 * 
	 * <u>Example 1</u>: 'f03.2' means pad out with a leading zero if required (3 characters before decimal place),
	 * then round to 2 fraction digits.
	 * 
	 * <pre>
	 * 27.317 ==> '027.32'
	 * 7.317 ==> '007.32'
	 * 0.317 ==> '000.32'
	 * 0.0 ==> '000.00'
	 * -0.317 ==> '-00.32'
	 * -5.517 ==> '-05.52'
	 * -25.517 ==> '-25.52'
	 * </pre>
	 * 
	 * <u>Example 2</u>: 'f+3.0' means include the sign on positive numbers, pad out with a leading space if required
	 * (3 characters before decimal place), the round to nearest integer.
	 * 
	 * <pre>
	 * 27.317 ==> '27'
	 * 7.317 ==> '  7'
	 * 0.317 ==> '  0'
	 * 0.0 ==> '  0'
	 * -0.317 ==> '  0'
	 * -5.517 ==> ' -6'
	 * -25.517 ==> '-26'
	 * </pre>
	 * 
	 * <p>
	 * Here's the syntax of a floating-point specification...
	 * </p>
	 * 
	 * <pre>
	 * pattern :== 'd' signedPositive?
	 * signedPositive :== '+'
	 *</pre>
	 * 
	 * <u>Example 3</u>: 'd+' means include the sign on positive numbers.
	 * 
	 * <pre>
	 * 27.317 ==> '27.317'
	 * 7.317 ==> '7.317'
	 * 0.31 ==> '0.31'
	 * 0.0 ==> '0.0'
	 * -0.317 ==> '-0.317'
	 * 2.7317E19 ==> '+2.7317E19'
	 * -2.7317E19 ==> '-2.7317E19'
	 * </pre>
	 * 
	 * @param zSpec
	 *              non-null specification string. If zero length, defaults to 'd'.
	 * @return a new, thread-safe DecimalMask object.
	 * @throws DecimalMaskException
	 *               if specification string does not follow the syntax rules above.
	 */
	public static DecimalMask newInstance(String zSpec)
			throws ArgonApiException {
		if (zSpec == null) throw new IllegalArgumentException("object is null");
		final int len = zSpec.length();
		if (len == 0) return FloatingNegSign;
		final char chType = zSpec.charAt(0);
		if (chType == 'f') return newFixed(zSpec.substring(1));
		if (chType == 'd') return newFloating(zSpec.substring(1));
		throw new ArgonApiException("Unsupported mask type '" + chType + "' in '" + zSpec + "'");
	}

	private DecimalMask(AMask mask, String maskPattern) {
		assert mask != null;
		m_mask = mask;
		m_maskPattern = maskPattern;
	}

	private final AMask m_mask;

	private final String m_maskPattern;
	private static final int MAX_FIXED_FRACTION_DIGITS = 18;

	private static final double D_FIXED_OVER = 1e18;
	private static final double D_FIXED_UNDER = 1e-18;

	private static final double[] POW10 = { 1.0, 1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5, 1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10, 1.0e11,
			1.0e12, 1.0e13, 1.0e14, 1.0e15, 1.0e16, 1.0e17, 1.0e18, 1.0e19 };

	private static final String[] ZEROPAD = { "", "0", "00", "000", "0000", "00000", "000000" };

	public static final DecimalMask FloatingNegSign = newFloating(false);
	public static final DecimalMask FixedNegSignNatural = newFixed(false, false, 0, false, true, 0);

	private static abstract class AMask {
		abstract String format(double value, float erm);

		abstract String format(long value);

		protected String formatFloating(boolean signedPositive, double value) {
			if (Double.isNaN(value)) return "NaN";
			final boolean isPositive = value > 0.0;
			if (isPositive && signedPositive) return "+" + Double.toString(value);
			return Double.toString(value);
		}

		protected static int naturalDecimalPlaces(float erm) {
			if (erm == 0.0f) return 0;
			float limit = 0.49f;
			for (int i = 0; i < MAX_FIXED_FRACTION_DIGITS; i++) {
				if (erm >= limit) return i;
				limit = limit * 0.1f;
			}
			return MAX_FIXED_FRACTION_DIGITS;
		}

		protected static boolean useFloating(double value) {
			if (value == 0.0) return false;
			if (Double.isNaN(value)) return true;
			if (Double.isInfinite(value)) return true;
			final double mag = Math.abs(value);
			if (mag >= D_FIXED_OVER) return true;
			if (mag <= D_FIXED_UNDER) return true;
			return false;
		}

		protected AMask() {
		}
	}

	private static class Fixed extends AMask {
		void enableSignedPositive(boolean enabled) {
			m_signedPositive = enabled;
		}

		void enableSignedZero(boolean enabled) {
			m_signedZero = enabled;
		}

		@Override
		String format(double value, float erm) {

			boolean useFloating = false;
			int fractionDigits = m_fractionDigits;
			if (useFloating(value)) {
				useFloating = true;
			} else {
				if (m_naturalFraction) {
					if (erm > 0.0f) {
						fractionDigits = naturalDecimalPlaces(erm);
					} else {
						useFloating = true;
					}
				}
			}
			if (useFloating) return formatFloating(m_signedPositive, value);

			final boolean isPositive = value > 0.0;
			final boolean isNegative = value < 0.0;
			String s = "";
			double x = 0.0;
			if (isPositive) {
				s = m_signedPositive ? "+" : "";
				x = value;
			} else if (isNegative) {
				s = "-";
				x = -value;
			}

			final double p10 = pow10(fractionDigits);
			final double x10 = (x * p10) + 0.5;
			if (useFloating(x10)) return formatFloating(m_signedPositive, value);

			final long n = (long) Math.floor(x10);

			final String m1;
			if (n == 0) {
				m1 = "0";
				if (m_signedPositive && m_signedZero && isPositive) {
					s = "+";
				} else if (m_signedZero && isNegative) {
					s = "-";
				} else {
					s = "";
				}
			} else {
				m1 = Long.toString(n);
			}

			if (fractionDigits == 0) return prefixed(s, m1, "");

			final int k1 = m1.length();
			final String m2;
			if (k1 > fractionDigits) {
				m2 = m1;
			} else {
				final StringBuilder mb = new StringBuilder();
				final int zl = fractionDigits + 1 - k1;
				for (int i = 0; i < zl; i++) {
					mb.append('0');
				}
				mb.append(m1);
				m2 = mb.toString();
			}
			final int k2 = m2.length();
			final int al = k2 - fractionDigits;
			final String a = m2.substring(0, al);
			final String b = m2.substring(al);
			return prefixed(s, a, b);
		}

		@Override
		String format(long value) {
			final boolean isPositive = value > 0;
			final boolean isNegative = value < 0;
			String s = "";
			long x = 0;
			if (isPositive) {
				s = m_signedPositive ? "+" : "";
				x = value;
			} else if (isNegative) {
				s = "-";
				x = -value;
			}
			final String a = Long.toString(x);
			final String b = zeroPad(m_fractionDigits);
			return prefixed(s, a, b);
		}

		void setLead(int leadCount, boolean leadZero) {
			m_leadCount = leadCount;
			m_leadZero = leadZero;
		}

		protected String prefixed(String s, String a, String zb) {
			final StringBuilder sb = new StringBuilder();
			if (m_leadCount > 0) {
				final int sl = s.length();
				final int al = a.length();
				if (m_leadZero) {
					sb.append(s);
				}
				final int pad = m_leadCount - sl - al;
				final char leadChar = m_leadZero ? '0' : ' ';
				for (int i = 0; i < pad; i++) {
					sb.append(leadChar);
				}
				if (!m_leadZero) {
					sb.append(s);
				}
			} else {
				sb.append(s);
			}
			sb.append(a);
			if (zb.length() > 0) {
				sb.append('.');
				sb.append(zb);
			}
			return sb.toString();
		}

		Fixed(boolean naturalFraction, int fractionDigits) {
			if (naturalFraction) {
				m_naturalFraction = true;
				m_fractionDigits = 0;
			} else {
				m_naturalFraction = false;
				m_fractionDigits = Math.max(0, Math.min(MAX_FIXED_FRACTION_DIGITS, fractionDigits));
			}
		}

		private final boolean m_naturalFraction;
		private final int m_fractionDigits;
		private boolean m_signedPositive = false;
		private boolean m_signedZero = false;
		private int m_leadCount = 0;
		private boolean m_leadZero = false;
	}

	private static enum FixedPatternState {
		SignFlags, LeadWidth, FractionDigits
	}

	private static class Floating extends AMask {

		void enableSignedPositive(boolean enabled) {
			m_signedPositive = enabled;
		}

		@Override
		String format(double value, float erm) {
			return formatFloating(m_signedPositive, value);
		}

		@Override
		String format(long value) {
			final boolean isPositive = value > 0;
			if (isPositive && m_signedPositive) return "+" + Long.toString(value);
			return Long.toString(value);
		}

		Floating() {
		}

		private boolean m_signedPositive = false;
	}
}
