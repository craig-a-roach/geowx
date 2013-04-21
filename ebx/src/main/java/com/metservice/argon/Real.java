/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 */
public class Real implements Comparable<Real> {

	public static final Real NaN = new Real(Double.NaN, Float.NaN);

	public boolean canRelate(Real rhs) {
		return !Double.isNaN(m_central) && !Double.isNaN(rhs.m_central);
	}

	public double central() {
		return m_central;
	}

	@Override
	public int compareTo(Real rhs) {
		return Double.compare(m_central, rhs.m_central);
	}

	public long convertToLong() {
		return Math.round(m_central);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof Real)) return false;
		return equals((Real) o);
	}

	public boolean equals(Real rhs) {
		return rhs != null && relationEquals(rhs);
	}

	public float errorMagnitude() {
		return m_err;
	}

	@Override
	public int hashCode() {
		final long bits = Double.doubleToLongBits(m_central);
		return (int) (bits ^ (bits >>> 32));
	}

	public boolean isNaN() {
		return Double.isNaN(m_central);
	}

	public boolean isNegative() {
		return m_central < 0.0;
	}

	public boolean isPositive() {
		return m_central > 0.0;
	}

	public boolean isZero(double tolerance) {
		final double hi = m_central + m_err;
		if (hi <= -tolerance) return false;
		final double lo = m_central - m_err;
		if (lo >= tolerance) return false;
		return true;
	}

	public boolean relationEquals(Real rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (!canRelate(rhs)) return false;
		if (rhs == this) return true;
		if (Double.isInfinite(m_central) || Double.isInfinite(rhs.m_central)) return m_central == rhs.m_central;
		final double lhi = m_central + m_err;
		final double rlo = rhs.m_central - rhs.m_err;
		if (lhi < rlo) return false;
		final double llo = m_central - m_err;
		final double rhi = rhs.m_central + rhs.m_err;
		if (llo > rhi) return false;
		return true;
	}

	public boolean relationGreaterThan(Real rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (!canRelate(rhs)) return false;
		if (rhs == this) return false;
		if (Double.isInfinite(m_central) || Double.isInfinite(rhs.m_central)) return m_central > rhs.m_central;
		final double llo = m_central - m_err;
		final double rhi = rhs.m_central + rhs.m_err;
		return llo > rhi;
	}

	public boolean relationLessThan(Real rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (!canRelate(rhs)) return false;
		if (rhs == this) return false;
		if (Double.isInfinite(m_central) || Double.isInfinite(rhs.m_central)) return m_central < rhs.m_central;
		final double lhi = m_central + m_err;
		final double rlo = rhs.m_central - rhs.m_err;
		return lhi < rlo;
	}

	@Override
	public String toString() {
		return Double.toString(m_central);
	}

	public Real unaryAbsolute() {
		if (m_central < 0.0) {
			final double c = -m_central;
			final float e = m_err;
			return new Real(c, e);
		}
		return this;
	}

	public Real unaryNegate() {
		final double c = -m_central;
		final float e = m_err;
		return new Real(c, e);
	}

	public Real unarySquared() {
		final double c = m_central * m_central;
		final double ce = m_central * m_err;
		final double e = ce + ce + ce + ce;
		final float fe = (float) e;
		return new Real(c, fe);
	}

	public Real unarySquareRoot() {
		final double c = Math.sqrt(m_central);
		final double ce1 = Math.sqrt(m_central + m_err);
		final double ce2 = Math.sqrt(Math.max(0.0, m_central - m_err));
		final double e = ce1 - ce2;
		final float fe = (float) e;
		return new Real(c, fe);
	}

	private static Real newInstance(String zSpec, Parse p)
			throws ArgonFormatException {
		assert p != null;
		final double mul = pow10(p.expval);
		final double dcentral = p.sigval * mul;
		final double derr = 0.5 * pow10(p.expval - p.dpdigits);
		if (derr > Float.MAX_VALUE) throw new ArgonFormatException("Error bar overflow (" + derr + ") in '" + zSpec + "'");
		final float ferr = (float) derr;
		return new Real(dcentral, ferr);
	}

	private static Parse newParse(String zSpec) {
		assert zSpec != null;
		final String ztwSpec = zSpec.trim();
		final int len = ztwSpec.length();
		if (len == 0) return new Parse("Empty value string");

		final StringBuilder bsig = new StringBuilder();
		final StringBuilder bexp = new StringBuilder();
		boolean partsig = true;
		boolean inleadzero = true;
		boolean acceptsign = true;
		boolean acceptdp = false;
		boolean seendp = false;
		int sigdigits = 0;
		int dpdigits = 0;
		for (int i = 0; i < len; i++) {
			final char ch = zSpec.charAt(i);
			if (Character.isDigit(ch)) {
				if (partsig) {
					bsig.append(ch);
					if (inleadzero && ch != '0') {
						inleadzero = false;
					}
					if (!inleadzero) {
						sigdigits++;
					}
					if (seendp) {
						dpdigits++;
					}
					if (!seendp) {
						acceptdp = true;
					}
				} else {
					bexp.append(ch);
				}
				acceptsign = false;
			} else if (ch == '-') {
				if (!acceptsign) return new Parse("Not expecting sign at position " + i);
				if (partsig) {
					bsig.append(ch);
				} else {
					bexp.append(ch);
				}
				acceptsign = false;
			} else if (ch == '+') {
				if (!acceptsign) return new Parse("Not expecting sign at position " + i);
				acceptsign = false;
			} else if (ch == '.') {
				if (!acceptdp) return new Parse("Not expecting decimal point at position " + i);
				if (partsig) {
					bsig.append(ch);
				}
				seendp = true;
				acceptdp = false;
			} else if (ch == CArgon.REAL_LEXPONENT || ch == CArgon.REAL_UEXPONENT) {
				if (sigdigits == 0) return new Parse("Missing significand");
				partsig = false;
				acceptsign = true;
				acceptdp = false;
			} else {
				final String m = "Unexpected character '" + ch + "' at position " + i;
				return new Parse(m);
			}
		}

		if (partsig) return new Parse("Not in scientific format");

		final String zsig = bsig.toString();
		if (sigdigits == 0 || zsig.length() == 0) return new Parse("Missing significand");
		double sigval = 0.0;
		try {
			sigval = Double.parseDouble(zsig);
		} catch (final NumberFormatException ex) {
			return new Parse("Malformed significand '" + zsig + "'");
		}

		final String zexp = bexp.toString();
		int expval = 0;
		if (zexp.length() > 0) {
			try {
				expval = Integer.parseInt(zexp);
			} catch (final NumberFormatException ex) {
				return new Parse("Malformed exponent '" + zexp + "'");
			}
		}

		return new Parse(sigval, dpdigits, expval);
	}

	private static double pow10(int exp) {
		if (exp == 0) return 1.0;
		if (exp > 0 && exp < PPOW10.length) return PPOW10[exp];
		if (exp < 0) {
			final int pexp = -exp;
			if (pexp < NPOW10.length) return NPOW10[pexp];
		}
		return Math.pow(10.0, exp);
	}

	public static Real binaryDivide(Real lhs, double rhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");

		final double c = lhs.m_central / rhs;
		final double rcle = rhs * lhs.m_err;
		final double rcrc = rhs * rhs;
		final double eq = rcle + rcle;
		final double ed = rcrc;
		final double e = eq / ed;
		final float fe = (float) e;
		return new Real(c, fe);
	}

	public static Real binaryDivide(Real lhs, Real orhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (orhs == null) return lhs;

		final double c = lhs.m_central / orhs.m_central;
		final double lcre = lhs.m_central * orhs.m_err;
		final double rcle = orhs.m_central * lhs.m_err;
		final double rcrc = orhs.m_central * orhs.m_central;
		final double rere = orhs.m_err * orhs.m_err;
		final double eq = lcre + lcre + rcle + rcle;
		final double ed = rcrc - rere;
		final double e = ed < rere ? c : (eq / ed);
		final float fe = (float) e;
		return new Real(c, fe);
	}

	public static Real binaryMax(Real lhs, Real orhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (orhs == null) return lhs;

		final int cmp = Double.compare(lhs.m_central, orhs.m_central);
		return cmp < 0 ? orhs : lhs;
	}

	public static Real binaryMin(Real lhs, Real orhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (orhs == null) return lhs;

		final int cmp = Double.compare(lhs.m_central, orhs.m_central);
		return cmp > 0 ? orhs : lhs;
	}

	public static Real binaryMinus(Real lhs, double rhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		final double c = lhs.m_central - rhs;
		final float e = lhs.m_err;
		return new Real(c, e);
	}

	public static Real binaryMinus(Real lhs, Real orhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (orhs == null) return lhs;

		final double c = lhs.m_central - orhs.m_central;
		final float e = lhs.m_err + orhs.m_err;
		return new Real(c, e);
	}

	public static Real binaryModulo(Real lhs, double rhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");

		final double c = lhs.m_central % rhs;
		final float e = lhs.m_err;
		return new Real(c, e);
	}

	public static Real binaryModulo(Real lhs, Real orhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (orhs == null) return lhs;

		final double c = lhs.m_central % orhs.m_central;
		final float e = lhs.m_err + orhs.m_err;
		return new Real(c, e);
	}

	public static Real binaryMultiply(Real lhs, double rhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");

		final double c = lhs.m_central * rhs;
		final double rcle = rhs * lhs.m_err;
		final float e = (float) (rcle + rcle);
		return new Real(c, e);
	}

	public static Real binaryMultiply(Real lhs, Real orhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (orhs == null) return lhs;

		final double c = lhs.m_central * orhs.m_central;
		final double lcre = lhs.m_central * orhs.m_err;
		final double rcle = orhs.m_central * lhs.m_err;
		final double e = lcre + lcre + rcle + rcle;
		final float fe = (float) e;
		return new Real(c, fe);
	}

	public static Real binaryPlus(Real lhs, double rhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		final double c = lhs.m_central + rhs;
		final float e = lhs.m_err;
		return new Real(c, e);
	}

	public static Real binaryPlus(Real lhs, Real orhs) {
		if (lhs == null) throw new IllegalArgumentException("object is null");
		if (orhs == null) return lhs;

		final double c = lhs.m_central + orhs.m_central;
		final float e = lhs.m_err + orhs.m_err;
		return new Real(c, e);
	}

	public static Real createInstance(String zSpec) {
		if (zSpec == null) throw new IllegalArgumentException("object is null");
		final Parse parse = newParse(zSpec);
		try {
			if (parse.oqErr != null) return newInstance(zSpec, parse);
		} catch (final ArgonFormatException ex) {
		}
		return null;
	}

	public static boolean isWellFormed(String zSpec) {
		if (zSpec == null) throw new IllegalArgumentException("object is null");
		return newParse(zSpec).oqErr == null;
	}

	public static Real newInstance(double c) {
		return new Real(c, 0.0f);
	}

	public static Real newInstance(int c) {
		return new Real(c, 0.0f);
	}

	public static Real newInstance(long c) {
		return new Real(c, 0.0f);
	}

	public static Real newInstance(String zSpec)
			throws ArgonFormatException {
		if (zSpec == null) throw new IllegalArgumentException("object is null");
		final Parse parse = newParse(zSpec);
		if (parse.oqErr != null) {
			final String m = "Invalid scientific double format '" + zSpec + "'..." + parse.oqErr;
			throw new ArgonFormatException(m);
		}
		return newInstance(zSpec, parse);
	}

	private Real(double c, float e) {
		m_central = c;
		m_err = e;
	}

	private final double m_central;
	private final float m_err;

	private static final double[] PPOW10 = { 1.0, 1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5, 1.0e6 };
	private static final double[] NPOW10 = { 1.0, 1.0e-1, 1.0e-2, 1.0e-3, 1.0e-4, 1.0e-5, 1.0e-6 };

	private static class Parse {

		Parse(double sigval, int dpdigits, int expval) {
			this.sigval = sigval;
			this.dpdigits = dpdigits;
			this.expval = expval;
		}

		Parse(String qErr) {
			this.oqErr = qErr;
		}

		double sigval;
		int dpdigits;
		int expval;
		String oqErr;
	}

}
