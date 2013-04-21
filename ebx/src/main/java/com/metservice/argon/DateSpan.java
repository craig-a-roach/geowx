/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Date;

/**
 * @author roach
 */
public class DateSpan implements Comparable<DateSpan> {

	public static final long TsMin = -8520336000000L;
	public static final long TsMax = 32503680000000L;
	public static final DateSpan Always = new DateSpan(TsMin, TsMax);
	public static final Date DateMin = new Date(TsMin);
	public static final Date DateMax = new Date(TsMax);

	@Override
	public int compareTo(DateSpan rhs) {
		if (tsFrom < rhs.tsFrom) return -1;
		if (tsFrom > rhs.tsFrom) return +1;
		if (tsToex < rhs.tsToex) return -1;
		if (tsToex > rhs.tsToex) return +1;
		return 0;
	}

	public boolean contains(long ts) {
		return tsFrom <= ts && tsToex > ts;
	}

	public boolean equals(DateSpan rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return tsFrom == rhs.tsFrom && tsToex == rhs.tsToex;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof DateSpan)) return false;
		return equals((DateSpan) o);
	}

	@Override
	public int hashCode() {
		int result = HashCoder.INIT;
		result = HashCoder.and(result, tsFrom);
		result = HashCoder.and(result, tsToex);
		return result;
	}

	public boolean intersects(DateSpan rhs, boolean orTouches) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		if (tsFrom < rhs.tsToex && tsToex > rhs.tsFrom) return true;
		if (!orTouches) return false;
		return tsToex == rhs.tsFrom || tsFrom == rhs.tsToex;
	}

	public boolean isOnwards() {
		return tsToex == TsMax;
	}

	public boolean isPoint() {
		return tsFrom == tsToex;
	}

	public boolean isUntil() {
		return tsFrom == TsMin;
	}

	public long msElapsed() {
		return tsToex - tsFrom;
	}

	public Date newDateFrom() {
		if (tsFrom == TsMin) return DateMin;
		return new Date(tsFrom);
	}

	public Date newDateToex() {
		if (tsToex == TsMax) return DateMax;
		return new Date(tsToex);
	}

	@Override
	public String toString() {
		final String sfrom;
		if (tsFrom == TsMin) {
			sfrom = "MIN";
		} else {
			final Date df = new Date(tsFrom);
			sfrom = df.toString();
		}
		final String stoex;
		if (tsToex == TsMax) {
			stoex = "MAX";
		} else {
			final Date dt = new Date(tsToex);
			stoex = dt.toString();
		}
		return sfrom + " --> " + stoex;
	}

	public long tsMid() {
		return tsFrom + ((tsToex - tsFrom) / 2L);
	}

	public static DateSpan newAt(long ts) {
		return new DateSpan(ts, ts);
	}

	public static DateSpan newInstance(long tsFrom, long tsToex)
			throws ArgonApiException {
		if (tsFrom > tsToex) {
			final Date df = new Date(tsFrom);
			final Date dt = new Date(tsToex);
			final String m = "Transposed date span; from=" + df + ", toex=" + dt;
			throw new ArgonApiException(m);
		}
		return new DateSpan(tsFrom, tsToex);
	}

	public static DateSpan newOnwards(long tsFrom) {
		return new DateSpan(tsFrom, TsMax);
	}

	public static DateSpan newTranspose(long ts1, long ts2) {
		final long tsFrom;
		final long tsToex;
		if (ts1 <= ts2) {
			tsFrom = ts1;
			tsToex = ts2;
		} else {
			tsFrom = ts2;
			tsToex = ts1;
		}
		return new DateSpan(tsFrom, tsToex);
	}

	public static DateSpan newUntil(long tsToex) {
		return new DateSpan(TsMin, tsToex);
	}

	private DateSpan(long tsFrom, long tsToex) {
		this.tsFrom = tsFrom;
		this.tsToex = tsToex;
	}
	public final long tsFrom;

	public final long tsToex;

}
