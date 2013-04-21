/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author roach
 */
public class TimeFactors implements Comparable<TimeFactors> {

	private TimeFactors newDstCompensated(long tsNeo) {
		final TimeFactors tfBase = new TimeFactors(tsNeo, timeZone);
		final int minsCompensate = minsGMTAdjustedOffset - tfBase.minsGMTAdjustedOffset;
		if (minsCompensate == 0) return tfBase;
		final long tsCompensate = tfBase.ts + (minsCompensate * CArgon.MS_PER_MIN);
		final TimeFactors tfCompensate = new TimeFactors(tsCompensate, timeZone);
		return tfCompensate;
	}

	private int rangeSignedSeconds(int sodTarget, NearestSense sense) {
		assert sense != null;
		final int sodOrigin = secondOfDay();
		final int ssecsRange = sodTarget - sodOrigin;
		if (ssecsRange == 0) return 0;
		final int asecsFwdRange;
		final int asecsRevRange;
		if (ssecsRange < 0) {
			asecsFwdRange = CArgon.SEC_PER_DAY + ssecsRange;
			asecsRevRange = -ssecsRange;
		} else {
			asecsFwdRange = ssecsRange;
			asecsRevRange = CArgon.SEC_PER_DAY - ssecsRange;
		}
		switch (sense) {
			case AlwaysFuture:
				return asecsFwdRange;
			case AlwaysPast:
				return -asecsRevRange;
			case Proximity:
				return (asecsRevRange < asecsFwdRange) ? -asecsRevRange : asecsFwdRange;
			default:
				throw new IllegalArgumentException("unsupported sense>" + sense + "<");
		}
	}

	private int rangeSignedSeconds(TimeOfDayRule rule, NearestSense sense) {
		assert rule != null;
		assert sense != null;
		final int sodOrigin = secondOfDay();
		final int[] sodCandidatesAsc = rule.secondsOfDayAsc();
		final int candidateCount = sodCandidatesAsc.length;
		int asecsMinRange = CArgon.SEC_PER_DAY;
		boolean isMinPast = false;
		for (int i = 0; i < candidateCount; i++) {
			final int sodCandidate = sodCandidatesAsc[i];
			final int ssecsRange = sodCandidate - sodOrigin;
			if (ssecsRange == 0) return 0;
			final int asecsFwdRange;
			final int asecsRevRange;
			if (ssecsRange < 0) {
				asecsFwdRange = CArgon.SEC_PER_DAY + ssecsRange;
				asecsRevRange = -ssecsRange;
			} else {
				asecsFwdRange = ssecsRange;
				asecsRevRange = CArgon.SEC_PER_DAY - ssecsRange;
			}
			boolean usePast = false;
			switch (sense) {
				case AlwaysFuture:
					usePast = false;
				break;
				case AlwaysPast:
					usePast = true;
				break;
				case Proximity:
					usePast = asecsRevRange < asecsFwdRange;
				break;
				default:
					throw new IllegalArgumentException("unsupported sense>" + sense + "<");
			}
			if (usePast) {
				if (asecsRevRange < asecsMinRange) {
					asecsMinRange = asecsRevRange;
					isMinPast = true;
				}
			} else {
				if (asecsFwdRange < asecsMinRange) {
					asecsMinRange = asecsFwdRange;
					isMinPast = false;
				}
			}
		}
		return isMinPast ? -asecsMinRange : asecsMinRange;
	}

	@Override
	public int compareTo(TimeFactors rhs) {
		if (ts < rhs.ts) return -1;
		if (ts > rhs.ts) return +1;
		return 0;
	}

	public String dowCode() {
		return UArgon.DowCodeMon0[dowMon0];
	}

	public String dowName() {
		return UArgon.DowNameMon0[dowMon0];
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof TimeFactors)) return false;
		return equals((TimeFactors) o);
	}

	public boolean equals(TimeFactors rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return ts == rhs.ts;
	}

	@Override
	public int hashCode() {
		return (int) (ts ^ (ts >>> 32));
	}

	public int hour12() {
		final int h11 = hour24 % 12;
		return h11 == 0 ? 12 : h11;
	}

	public boolean isAM() {
		return hour24 < 12;
	}

	public boolean isDaylightSavingInEffect() {
		return minsGMTAdjustedOffset != minsGMTStandardOffset;
	}

	public boolean isMidnightUsageUntil() {
		return hour24 == 23 && minute == 59;
	}

	public boolean isNoonUsageAt() {
		return hour24 == 12 && minute == 0;
	}

	public boolean isNoonUsageUntil() {
		return hour24 == 11 && minute == 59;
	}

	public int lastDayOfMonth() {
		return UArgon.lastDayOfMonth1(year, moyJan0 + 1);
	}

	public int moyJan1() {
		return moyJan0 + 1;
	}

	public TimeFactors newAlignedCalendar(CalendarUnit unit, AlignSense sense) {
		if (unit == null) throw new IllegalArgumentException("object is null");
		if (sense == null) throw new IllegalArgumentException("object is null");
		final TimeFactors base = newAlignedSecond(sense);
		return newAlignedCalendar(base, unit, sense);
	}

	public TimeFactors newAlignedInterval(long msInterval, AlignSense sense)
			throws ArgonApiException {
		if (sense == null) throw new IllegalArgumentException("object is null");
		if (msInterval <= 0 || msInterval > CArgon.MS_PER_DAY2) {
			final String m = "Invalid alignment interval for " + sense + " (" + msInterval + "ms); must be > 0 and <= 12h";
			throw new ArgonApiException(m);
		}
		final TimeFactors base = newAlignedSecond(sense);
		return newAlignedInterval(base, msInterval, sense);

	}

	public TimeFactors newAlignedSecond(AlignSense sense) {
		if (sense == null) throw new IllegalArgumentException("object is null");
		if (millisecond == 0L) return this;
		final long msRange;
		switch (sense) {
			case Round:
				msRange = millisecond >= 500L ? (1000L - millisecond) : (-millisecond);
			break;
			case Ceiling:
				msRange = 1000L - millisecond;
			break;
			case Floor:
				msRange = -millisecond;
			break;
			default:
				throw new IllegalArgumentException("invalid sense>" + sense + "<");
		}
		final long tsNeo = ts + msRange;
		return newDstCompensated(tsNeo);
	}

	public Date newDate() {
		return DateFactory.newDate(ts);
	}

	public TimeFactors newNearest(int secondOfDayTarget, NearestSense sense) {
		if (sense == null) throw new IllegalArgumentException("object is null");
		return newNearest(newAlignedSecond(AlignSense.Round), secondOfDayTarget, sense);
	}

	public TimeFactors newNearest(TimeOfDayRule rule, NearestSense sense) {
		if (rule == null) throw new IllegalArgumentException("object is null");
		if (sense == null) throw new IllegalArgumentException("object is null");
		return newNearest(newAlignedSecond(AlignSense.Round), rule, sense);
	}

	public TimeFactors newRezoned(TimeZone neoTimeZone) {
		if (neoTimeZone == null) throw new IllegalArgumentException("object is null");
		return new TimeFactors(ts, neoTimeZone);
	}

	public TimeFactors newUsageUntil() {
		return new TimeFactors(ts - 1L, timeZone);
	}

	public int secondOfDay() {
		return (hour24 * CArgon.SEC_PER_HR) + (minute * CArgon.SEC_PER_MIN) + second + (millisecond >= 500 ? 1 : 0);
	}

	public int smsGMTAdjustedOffset() {
		return minsGMTAdjustedOffset * CArgon.MS_PER_MIN;
	}

	public int smsGMTStandardOffset() {
		return minsGMTStandardOffset * CArgon.MS_PER_MIN;
	}

	@Override
	public String toString() {
		return TimeMask.Default.format(this);
	}

	private static TimeFactors newAlignedCalendar(TimeFactors base, CalendarUnit unit, AlignSense sense) {
		assert base != null;
		final boolean useStart;
		switch (sense) {
			case Floor: {
				useStart = true;
			}
			break;
			case Ceiling: {
				useStart = false;
			}
			break;
			case Round: {
				switch (unit) {
					case Day:
						useStart = base.secondOfDay() < CArgon.SEC_PER_DAY2;
					break;
					case Month:
						useStart = base.dom1 < 15;
					break;
					case Year:
						useStart = base.moyJan0 < 6;
					break;
					default:
						throw new IllegalArgumentException("invalid unit>" + unit + "<");
				}
			}
			break;
			default:
				throw new IllegalArgumentException("invalid sense>" + sense + "<");
		}

		final Calendar cal = UArgon.newCalendar(base.timeZone);
		cal.setTimeInMillis(base.ts);
		switch (unit) {
			case Day:
			break;
			case Month:
				if (useStart) {
					cal.set(Calendar.DAY_OF_MONTH, 1);
				} else {
					cal.set(Calendar.DAY_OF_MONTH, base.lastDayOfMonth());
				}
			break;
			case Year:
				if (useStart) {
					cal.set(Calendar.MONTH, 0);
					cal.set(Calendar.DAY_OF_MONTH, 1);
				} else {
					cal.set(Calendar.MONTH, 11);
					cal.set(Calendar.DAY_OF_MONTH, 31);
				}
			break;
			default:
				throw new IllegalArgumentException("invalid unit>" + unit + "<");
		}

		if (useStart) {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		} else {
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 999);
		}

		final long tsNeo = cal.getTimeInMillis();
		return new TimeFactors(tsNeo, base.timeZone);
	}

	private static TimeFactors newAlignedInterval(TimeFactors base, long msInterval, AlignSense sense) {
		final long msod = base.secondOfDay() * CArgon.MS_PER_SEC;
		final long tsStart = base.ts - msod;
		final long intervalCount;
		switch (sense) {
			case Round:
				intervalCount = (msod + (msInterval / 2L)) / msInterval;
			break;
			case Ceiling:
				intervalCount = (msod / msInterval) + (base.ts % msInterval == 0L ? 0L : 1L);
			break;
			case Floor:
				intervalCount = msod / msInterval;
			break;
			default:
				throw new IllegalArgumentException("invalid sense>" + sense + "<");
		}
		final long tsNeo = tsStart + (intervalCount * msInterval);
		return base.newDstCompensated(tsNeo);
	}

	private static TimeFactors newNearest(TimeFactors base, int sodTarget, NearestSense sense) {
		assert base != null;
		final int ssecRange = base.rangeSignedSeconds(sodTarget, sense);
		final long tsNeo = base.ts + (ssecRange * CArgon.MS_PER_SEC);
		return base.newDstCompensated(tsNeo);
	}

	private static TimeFactors newNearest(TimeFactors base, TimeOfDayRule rule, NearestSense sense) {
		assert base != null;
		final int ssecRange = base.rangeSignedSeconds(rule, sense);
		final long tsNeo = base.ts + (ssecRange * CArgon.MS_PER_SEC);
		return base.newDstCompensated(tsNeo);
	}

	public static TimeFactors newInstance(Date date, TimeZone timeZone) {
		if (date == null) throw new IllegalArgumentException("object is null");
		if (timeZone == null) throw new IllegalArgumentException("object is null");
		return new TimeFactors(date.getTime(), timeZone);
	}

	public static TimeFactors newInstance(long ts, TimeZone timeZone) {
		if (timeZone == null) throw new IllegalArgumentException("object is null");
		return new TimeFactors(ts, timeZone);
	}

	private TimeFactors(long ts, TimeZone timeZone) {
		assert timeZone != null;

		final Calendar cal = UArgon.newCalendar(timeZone);
		cal.setTimeInMillis(ts);
		final int dowSun0 = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;

		this.ts = ts;
		this.timeZone = timeZone;
		this.millisecond = (int) (ts % 1000L);
		this.second = cal.get(Calendar.SECOND);
		this.minute = cal.get(Calendar.MINUTE);
		this.hour24 = cal.get(Calendar.HOUR_OF_DAY);
		this.dowMon0 = (dowSun0 == 0) ? 6 : (dowSun0 - 1);
		this.dom1 = cal.get(Calendar.DAY_OF_MONTH);
		this.doy1 = cal.get(Calendar.DAY_OF_YEAR);
		this.moyJan0 = cal.get(Calendar.MONTH);
		this.year = cal.get(Calendar.YEAR);
		this.minsGMTAdjustedOffset = timeZone.getOffset(ts) / CArgon.MS_PER_MIN;
		this.minsGMTStandardOffset = timeZone.getRawOffset() / CArgon.MS_PER_MIN;
	}

	public final long ts;

	public final TimeZone timeZone;
	public final int millisecond;
	public final int second;
	public final int minute;
	public final int hour24;
	public final int dowMon0;
	public final int dom1;
	public final int doy1;
	public final int moyJan0;
	public final int year;
	public final int minsGMTAdjustedOffset;
	public final int minsGMTStandardOffset;

	public static final EnumDecoder<NearestSense> DecoderNearestSense = new EnumDecoder<NearestSense>(NearestSense.values(),
			"Nearest Sense");

	public static final EnumDecoder<AlignSense> DecoderAlignSense = new EnumDecoder<AlignSense>(AlignSense.values(),
			"Align Sense");

	public static final EnumDecoder<CalendarUnit> DecoderCalendarUnit = new EnumDecoder<CalendarUnit>(CalendarUnit.values(),
			"Calendar Unit");

	public static enum AlignSense {
		Round, Floor, Ceiling
	}

	public static enum CalendarUnit {
		Day, Month, Year;
	}

	public static enum NearestSense {
		Proximity, AlwaysPast, AlwaysFuture;
	}
}
