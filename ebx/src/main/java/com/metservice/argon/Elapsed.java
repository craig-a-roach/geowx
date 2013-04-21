/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

/**
 * @author roach
 * Represents the elapsed time in milliseconds.
 */
public class Elapsed implements Comparable<Elapsed> {

	public static final Elapsed Zero = new Elapsed(0L);
	public static final Elapsed MaxValue = new Elapsed(Long.MAX_VALUE);

	public static Elapsed clamp(Elapsed lo, Elapsed mid, Elapsed hi) {
		if (lo == null) throw new IllegalArgumentException("object is null");
		if (mid == null) throw new IllegalArgumentException("object is null");
		if (hi == null) throw new IllegalArgumentException("object is null");
		if (mid.sms < lo.sms) return lo;
		if (mid.sms > hi.sms) return hi;
		return mid;
	}

	public static int clamp(int smsLo, long sms, int smsHi) {
		if (sms < smsLo) return smsLo;
		if (sms > smsHi) return smsHi;
		return (int) sms;
	}

    // Unused?
	public static long clamp(long smsLo, long sms, long smsHi) {
		if (sms < smsLo) return smsLo;
		if (sms > smsHi) return smsHi;
		return sms;
	}

	public static Elapsed max(Elapsed olhs, Elapsed rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (olhs == null) return rhs;
		if (rhs.sms > olhs.sms) return rhs;
		return olhs;
	}

	public static Elapsed min(Elapsed olhs, Elapsed rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (olhs == null) return rhs;
		if (rhs.sms < olhs.sms) return rhs;
		return olhs;
	}

	public static Elapsed newInstance(long sms) {
		if (sms == 0L) return Zero;
		return new Elapsed(sms);
	}

	public Elapsed atLeast(Elapsed lo) {
		if (lo == null) throw new IllegalArgumentException("object is null");
		if (sms < lo.sms) return lo;
		return this;
	}

	public int atLeast(int smsLo) {
		if (sms < smsLo) return smsLo;
		if (sms > Integer.MAX_VALUE) {
			final String m = "Value (" + sms + "ms) too large to represent as integer ms";
			throw new IllegalStateException(m);
		}
		return (int) sms;
	}

	public long atLeast(long smsLo) {
		if (sms < smsLo) return smsLo;
		return sms;
	}

	public int atLeastSecs(int ssecLo) {
		final long ssec = sms / CArgon.MS_PER_SEC;
		if (ssec < ssecLo) return ssecLo;
		if (ssec > Integer.MAX_VALUE) {
			final String m = "Value (" + ssec + "sec) too large to represent as integer secs";
			throw new IllegalStateException(m);
		}
		return (int) ssec;
	}

    // Unused?
	public Elapsed clamp(Elapsed lo, Elapsed hi) {
		if (lo == null) throw new IllegalArgumentException("object is null");
		if (hi == null) throw new IllegalArgumentException("object is null");
		if (sms < lo.sms) return lo;
		if (sms > hi.sms) return hi;
		return this;
	}

    // Unused?
	public int clamp(int smsLo, int smsHi) {
		if (sms < smsLo) return smsLo;
		if (sms > smsHi) return smsHi;
		return (int) sms;
	}

	public long clamp(long smsLo, long smsHi) {
		if (sms < smsLo) return smsLo;
		if (sms > smsHi) return smsHi;
		return sms;
	}

	public int clampSecs(int ssecLo, int ssecHi) {
		final long ssec = sms / CArgon.MS_PER_SEC;
		if (ssec < ssecLo) return ssecLo;
		if (ssec > ssecHi) return ssecHi;
		return (int) ssec;
	}

	@Override
	public int compareTo(Elapsed rhs) {
		if (sms < rhs.sms) return -1;
		if (sms > rhs.sms) return +1;
		return 0;
	}

	public boolean equals(Elapsed rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return sms == rhs.sms;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof Elapsed)) return false;
		return equals((Elapsed) o);
	}

	@Override
	public int hashCode() {
		return (int) (sms ^ (sms >>> 32));
	}

	public int intMsSigned() {
		if (sms < Integer.MIN_VALUE) {
			final String m = "Value (" + sms + "ms) too large (negative) to represent as integer ms";
			throw new IllegalStateException(m);
		}
		if (sms > Integer.MAX_VALUE) {
			final String m = "Value (" + sms + "ms) too large (positive) to represent as integer ms";
			throw new IllegalStateException(m);
		}
		return (int) sms;
	}

	public int intSecsSigned() {
		final long ssec = sms / CArgon.MS_PER_SEC;
		if (ssec < Integer.MIN_VALUE) {
			final String m = "Value (" + ssec + "sec) too large (negative) to represent as integer secs";
			throw new IllegalStateException(m);
		}
		if (ssec > Integer.MAX_VALUE) {
			final String m = "Value (" + ssec + "sec) too large (positive) to represent as integer secs";
			throw new IllegalStateException(m);
		}
		return (int) ssec;
	}

	public Elapsed max(Elapsed orhs) {
		if (orhs == null) return this;
		if (orhs.sms > sms) return orhs;
		return this;
	}

	public Elapsed min(Elapsed orhs) {
		if (orhs == null) return this;
		if (orhs.sms < sms) return orhs;
		return this;
	}

	@Override
	public String toString() {
		return ElapsedFormatter.formatSingleUnit(sms);
	}

	public Elapsed(long sms) {
		this.sms = sms;
	}

	public long sms;
}
