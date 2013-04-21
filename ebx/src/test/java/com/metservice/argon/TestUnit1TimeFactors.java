/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1TimeFactors {

	@Test
	public void t40_align()
			throws ArgonFormatException, ArgonApiException {
		final TimeZone tz = TimeZoneFactory.selectById("Pacific/Auckland");
		final Date dfeb14 = DateFactory.newInstance(new String[] { "20080214", "1516", "30", "135" }, tz);
		final Date dapr04 = DateFactory.newInstance(new String[] { "2010", "Apr", "04", "1416", "30", "635" }, tz);
		final Date dsep26 = DateFactory.newInstance(new String[] { "2010", "Sep", "26", "1516", "30", "635" }, tz);
		final TimeFactors feb14 = TimeFactors.newInstance(dfeb14.getTime(), tz);
		final TimeFactors apr04 = TimeFactors.newInstance(dapr04.getTime(), tz);
		final TimeFactors sep26 = TimeFactors.newInstance(dsep26.getTime(), tz);
		Assert.assertEquals("2008-Feb-14 (Thu) 15:16:30.135 GMT+13:00", feb14.toString());
		Assert.assertEquals("2010-Apr-04 (Sun) 14:16:30.635 GMT+12:00", apr04.toString());
		Assert.assertEquals("2010-Sep-26 (Sun) 15:16:30.635 GMT+13:00", sep26.toString());

		final TimeFactors.CalendarUnit DAY = TimeFactors.DecoderCalendarUnit.select("day");
		final TimeFactors.CalendarUnit MONTH = TimeFactors.DecoderCalendarUnit.select("month");
		final TimeFactors.CalendarUnit YEAR = TimeFactors.DecoderCalendarUnit.select("year");
		final TimeFactors.AlignSense ROUND = TimeFactors.DecoderAlignSense.select("round");
		final TimeFactors.AlignSense FLOOR = TimeFactors.DecoderAlignSense.select("floor");
		final TimeFactors.AlignSense CEILING = TimeFactors.DecoderAlignSense.select("ceiling");

		Assert.assertEquals("2008-Feb-14 (Thu) 00:00:00.000 GMT+13:00", feb14.newAlignedCalendar(DAY, FLOOR).toString());
		Assert.assertEquals("2008-Feb-14 (Thu) 23:59:59.999 GMT+13:00", feb14.newAlignedCalendar(DAY, ROUND).toString());
		Assert.assertEquals("2008-Feb-14 (Thu) 23:59:59.999 GMT+13:00", feb14.newAlignedCalendar(DAY, CEILING).toString());
		Assert.assertEquals("2008-Feb-01 (Fri) 00:00:00.000 GMT+13:00", feb14.newAlignedCalendar(MONTH, FLOOR).toString());
		Assert.assertEquals("2008-Feb-01 (Fri) 00:00:00.000 GMT+13:00", feb14.newAlignedCalendar(MONTH, ROUND).toString());
		Assert.assertEquals("2008-Feb-29 (Fri) 23:59:59.999 GMT+13:00", feb14.newAlignedCalendar(MONTH, CEILING).toString());
		Assert.assertEquals("2008-Jan-01 (Tue) 00:00:00.000 GMT+13:00", feb14.newAlignedCalendar(YEAR, FLOOR).toString());
		Assert.assertEquals("2008-Jan-01 (Tue) 00:00:00.000 GMT+13:00", feb14.newAlignedCalendar(YEAR, ROUND).toString());
		Assert.assertEquals("2008-Dec-31 (Wed) 23:59:59.999 GMT+13:00", feb14.newAlignedCalendar(YEAR, CEILING).toString());

		Assert.assertEquals("2010-Apr-04 (Sun) 00:00:00.000 GMT+13:00", apr04.newAlignedCalendar(DAY, FLOOR).toString());
		Assert.assertEquals("2010-Apr-04 (Sun) 23:59:59.999 GMT+12:00", apr04.newAlignedCalendar(DAY, ROUND).toString());
		Assert.assertEquals("2010-Apr-04 (Sun) 23:59:59.999 GMT+12:00", apr04.newAlignedCalendar(DAY, CEILING).toString());
		Assert.assertEquals("2010-Apr-01 (Thu) 00:00:00.000 GMT+13:00", apr04.newAlignedCalendar(MONTH, FLOOR).toString());
		Assert.assertEquals("2010-Apr-01 (Thu) 00:00:00.000 GMT+13:00", apr04.newAlignedCalendar(MONTH, ROUND).toString());
		Assert.assertEquals("2010-Apr-30 (Fri) 23:59:59.999 GMT+12:00", apr04.newAlignedCalendar(MONTH, CEILING).toString());
		Assert.assertEquals("2010-Jan-01 (Fri) 00:00:00.000 GMT+13:00", apr04.newAlignedCalendar(YEAR, FLOOR).toString());
		Assert.assertEquals("2010-Jan-01 (Fri) 00:00:00.000 GMT+13:00", apr04.newAlignedCalendar(YEAR, ROUND).toString());
		Assert.assertEquals("2010-Dec-31 (Fri) 23:59:59.999 GMT+13:00", apr04.newAlignedCalendar(YEAR, CEILING).toString());

		Assert.assertEquals("2010-Sep-26 (Sun) 00:00:00.000 GMT+12:00", sep26.newAlignedCalendar(DAY, FLOOR).toString());
		Assert.assertEquals("2010-Sep-26 (Sun) 23:59:59.999 GMT+13:00", sep26.newAlignedCalendar(DAY, ROUND).toString());
		Assert.assertEquals("2010-Sep-26 (Sun) 23:59:59.999 GMT+13:00", sep26.newAlignedCalendar(DAY, CEILING).toString());
		Assert.assertEquals("2010-Sep-01 (Wed) 00:00:00.000 GMT+12:00", sep26.newAlignedCalendar(MONTH, FLOOR).toString());
		Assert.assertEquals("2010-Sep-30 (Thu) 23:59:59.999 GMT+13:00", sep26.newAlignedCalendar(MONTH, ROUND).toString());
		Assert.assertEquals("2010-Sep-30 (Thu) 23:59:59.999 GMT+13:00", sep26.newAlignedCalendar(MONTH, CEILING).toString());
		Assert.assertEquals("2010-Jan-01 (Fri) 00:00:00.000 GMT+13:00", sep26.newAlignedCalendar(YEAR, FLOOR).toString());
		Assert.assertEquals("2010-Dec-31 (Fri) 23:59:59.999 GMT+13:00", sep26.newAlignedCalendar(YEAR, ROUND).toString());
		Assert.assertEquals("2010-Dec-31 (Fri) 23:59:59.999 GMT+13:00", sep26.newAlignedCalendar(YEAR, CEILING).toString());
	}

	@Test
	public void t45_alignSecond()
			throws ArgonApiException, ArgonFormatException {
		final TimeZone tz = TimeZoneFactory.selectById("Pacific/Auckland");
		final Date dateL = DateFactory.newDateConstantFromT8("20091120T0216Z30M499");
		final TimeFactors tfL = TimeFactors.newInstance(dateL.getTime(), tz);
		Assert.assertEquals((15 * 3600 + 16 * 60 + 30), tfL.secondOfDay());
		final TimeFactors tfLF = tfL.newAlignedSecond(TimeFactors.AlignSense.Floor);
		final TimeFactors tfLC = tfL.newAlignedSecond(TimeFactors.AlignSense.Ceiling);
		final TimeFactors tfLR = tfL.newAlignedSecond(TimeFactors.AlignSense.Round);
		Assert.assertEquals("2009-Nov-20 (Fri) 15:16:30.000 GMT+13:00", tfLF.toString());
		Assert.assertEquals("2009-Nov-20 (Fri) 15:16:31.000 GMT+13:00", tfLC.toString());
		Assert.assertEquals("2009-Nov-20 (Fri) 15:16:30.000 GMT+13:00", tfLR.toString());
		final Date dateU = DateFactory.newDateConstantFromT8("20091120T0216Z30M500");
		final TimeFactors tfU = TimeFactors.newInstance(dateU.getTime(), tz);
		Assert.assertEquals((15 * 3600 + 16 * 60 + 30 + 1), tfU.secondOfDay());
		final TimeFactors tfUF = tfU.newAlignedSecond(TimeFactors.AlignSense.Floor);
		final TimeFactors tfUC = tfU.newAlignedSecond(TimeFactors.AlignSense.Ceiling);
		final TimeFactors tfUR = tfU.newAlignedSecond(TimeFactors.AlignSense.Round);
		Assert.assertEquals("2009-Nov-20 (Fri) 15:16:30.000 GMT+13:00", tfUF.toString());
		Assert.assertEquals("2009-Nov-20 (Fri) 15:16:31.000 GMT+13:00", tfUC.toString());
		Assert.assertEquals("2009-Nov-20 (Fri) 15:16:31.000 GMT+13:00", tfUR.toString());
	}

	@Test
	public void t50_nearest4x6()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20091120T0216Z30M535");
		final TimeZone tz = TimeZoneFactory.selectById("Pacific/Auckland");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		Assert.assertEquals("2009-Nov-20 (Fri) 15:16:30.535 GMT+13:00", tf.toString());
		final TimeFactors.NearestSense PROXIMITY = TimeFactors.DecoderNearestSense.select(" proximity ");
		final TimeFactors.NearestSense ALWAYSPAST = TimeFactors.DecoderNearestSense.select("alwaysPast");
		final TimeFactors.NearestSense ALWAYSFUTURE = TimeFactors.DecoderNearestSense.select("AlwaysFuture");
		final TimeFactors.AlignSense ROUND = TimeFactors.DecoderAlignSense.select("round");
		final TimeFactors.AlignSense FLOOR = TimeFactors.DecoderAlignSense.select("Floor");
		final TimeFactors.AlignSense CEILING = TimeFactors.DecoderAlignSense.select("ceiling");
		{
			final long e6h = ElapsedFactory.ms("6h");
			final TimeOfDayRule r = TimeOfDayFactory.newRule("1300+ 6h* 3");
			final TimeFactors proximity = tf.newNearest(r, PROXIMITY);
			final TimeFactors past = tf.newNearest(r, ALWAYSPAST);
			final TimeFactors future = tf.newNearest(r, ALWAYSFUTURE);
			final TimeFactors round = tf.newAlignedInterval(e6h, ROUND);
			final TimeFactors ceiling = tf.newAlignedInterval(e6h, CEILING);
			final TimeFactors floor = tf.newAlignedInterval(e6h, FLOOR);
			Assert.assertEquals("2009-Nov-20 (Fri) 13:00:00.000 GMT+13:00", proximity.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 13:00:00.000 GMT+13:00", past.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 19:00:00.000 GMT+13:00", future.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 18:00:00.000 GMT+13:00", round.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 18:00:00.000 GMT+13:00", ceiling.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 12:00:00.000 GMT+13:00", floor.toString());
		}
	}

	@Test
	public void t51_nearest2x12()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20091120T0216Z30M435");
		final TimeZone tz = TimeZoneFactory.selectById("Pacific/Auckland");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		Assert.assertEquals("2009-Nov-20 (Fri) 15:16:30.435 GMT+13:00", tf.toString());
		final TimeFactors.NearestSense PROXIMITY = TimeFactors.DecoderNearestSense.select("Proximity");
		final TimeFactors.NearestSense ALWAYSPAST = TimeFactors.DecoderNearestSense.select("AlwaysPast");
		final TimeFactors.NearestSense ALWAYSFUTURE = TimeFactors.DecoderNearestSense.select("AlwaysFuture");
		final TimeFactors.AlignSense ROUND = TimeFactors.DecoderAlignSense.select("round");
		final TimeFactors.AlignSense FLOOR = TimeFactors.DecoderAlignSense.select("floor");
		final TimeFactors.AlignSense CEILING = TimeFactors.DecoderAlignSense.select("ceiling");
		{
			final long e12h = ElapsedFactory.ms("12h");
			final TimeOfDayRule r = TimeOfDayFactory.newRule("1300+ 12h");
			final TimeFactors proximity = tf.newNearest(r, PROXIMITY);
			final TimeFactors past = tf.newNearest(r, ALWAYSPAST);
			final TimeFactors future = tf.newNearest(r, ALWAYSFUTURE);
			final TimeFactors round = tf.newAlignedInterval(e12h, ROUND);
			final TimeFactors ceiling = tf.newAlignedInterval(e12h, CEILING);
			final TimeFactors floor = tf.newAlignedInterval(e12h, FLOOR);
			Assert.assertEquals("2009-Nov-20 (Fri) 13:00:00.000 GMT+13:00", proximity.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 13:00:00.000 GMT+13:00", past.toString());
			Assert.assertEquals("2009-Nov-21 (Sat) 01:00:00.000 GMT+13:00", future.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 12:00:00.000 GMT+13:00", round.toString());
			Assert.assertEquals("2009-Nov-21 (Sat) 00:00:00.000 GMT+13:00", ceiling.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 12:00:00.000 GMT+13:00", floor.toString());
		}
	}

	@Test
	public void t52_nearestDSTAutumnPre2x12()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20100403T1316Z30M535");
		final TimeZone tz = TimeZoneFactory.selectById("Pacific/Auckland");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		Assert.assertEquals("2010-Apr-04 (Sun) 02:16:30.535 GMT+13:00", tf.toString());
		final TimeFactors.NearestSense PROXIMITY = TimeFactors.DecoderNearestSense.select("Proximity");
		final TimeFactors.NearestSense ALWAYSPAST = TimeFactors.DecoderNearestSense.select("AlwaysPast");
		final TimeFactors.NearestSense ALWAYSFUTURE = TimeFactors.DecoderNearestSense.select("AlwaysFuture");
		final TimeFactors.AlignSense ROUND = TimeFactors.DecoderAlignSense.select("round");
		final TimeFactors.AlignSense FLOOR = TimeFactors.DecoderAlignSense.select("floor");
		final TimeFactors.AlignSense CEILING = TimeFactors.DecoderAlignSense.select("ceiling");
		{
			final long e12h = ElapsedFactory.ms("12h");
			final TimeOfDayRule r = TimeOfDayFactory.newRule("2200+ 12h");
			final TimeFactors proximity = tf.newNearest(r, PROXIMITY);
			final TimeFactors past = tf.newNearest(r, ALWAYSPAST);
			final TimeFactors future = tf.newNearest(r, ALWAYSFUTURE);
			final TimeFactors round = tf.newAlignedInterval(e12h, ROUND);
			final TimeFactors ceiling = tf.newAlignedInterval(e12h, CEILING);
			final TimeFactors floor = tf.newAlignedInterval(e12h, FLOOR);
			Assert.assertEquals("2010-Apr-03 (Sat) 22:00:00.000 GMT+13:00", proximity.toString());
			Assert.assertEquals("2010-Apr-03 (Sat) 22:00:00.000 GMT+13:00", past.toString());
			Assert.assertEquals("2010-Apr-04 (Sun) 10:00:00.000 GMT+12:00", future.toString());
			Assert.assertEquals("2010-Apr-04 (Sun) 00:00:00.000 GMT+13:00", round.toString());
			Assert.assertEquals("2010-Apr-04 (Sun) 12:00:00.000 GMT+12:00", ceiling.toString());
			Assert.assertEquals("2010-Apr-04 (Sun) 00:00:00.000 GMT+13:00", floor.toString());
		}
	}

	@Test
	public void t53_nearestDSTAutumnPost2x12()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20100403T1416Z30M435");
		final TimeZone tz = TimeZoneFactory.selectById("Pacific/Auckland");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		Assert.assertEquals("2010-Apr-04 (Sun) 02:16:30.435 GMT+12:00", tf.toString());
		final TimeFactors.NearestSense PROXIMITY = TimeFactors.DecoderNearestSense.select("Proximity");
		final TimeFactors.NearestSense ALWAYSPAST = TimeFactors.DecoderNearestSense.select("AlwaysPast");
		final TimeFactors.NearestSense ALWAYSFUTURE = TimeFactors.DecoderNearestSense.select("AlwaysFuture");
		final TimeFactors.AlignSense ROUND = TimeFactors.DecoderAlignSense.select("round");
		final TimeFactors.AlignSense FLOOR = TimeFactors.DecoderAlignSense.select("floor");
		final TimeFactors.AlignSense CEILING = TimeFactors.DecoderAlignSense.select("ceiling");
		{
			final long e12h = ElapsedFactory.ms("12h");
			final TimeOfDayRule r = TimeOfDayFactory.newRule("2200+ 12h");
			final TimeFactors proximity = tf.newNearest(r, PROXIMITY);
			final TimeFactors past = tf.newNearest(r, ALWAYSPAST);
			final TimeFactors future = tf.newNearest(r, ALWAYSFUTURE);
			final TimeFactors round = tf.newAlignedInterval(e12h, ROUND);
			final TimeFactors ceiling = tf.newAlignedInterval(e12h, CEILING);
			final TimeFactors floor = tf.newAlignedInterval(e12h, FLOOR);
			Assert.assertEquals("2010-Apr-03 (Sat) 22:00:00.000 GMT+13:00", proximity.toString());
			Assert.assertEquals("2010-Apr-03 (Sat) 22:00:00.000 GMT+13:00", past.toString());
			Assert.assertEquals("2010-Apr-04 (Sun) 10:00:00.000 GMT+12:00", future.toString());
			Assert.assertEquals("2010-Apr-04 (Sun) 00:00:00.000 GMT+13:00", round.toString());
			Assert.assertEquals("2010-Apr-04 (Sun) 12:00:00.000 GMT+12:00", ceiling.toString());
			Assert.assertEquals("2010-Apr-04 (Sun) 00:00:00.000 GMT+13:00", floor.toString());
		}
	}

	@Test
	public void t54_nearestDSTSpringPre2x12()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20100925T1316Z30M535");
		final TimeZone tz = TimeZoneFactory.selectById("Pacific/Auckland");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		Assert.assertEquals("2010-Sep-26 (Sun) 01:16:30.535 GMT+12:00", tf.toString());
		final TimeFactors.NearestSense PROXIMITY = TimeFactors.DecoderNearestSense.select("Proximity");
		final TimeFactors.NearestSense ALWAYSPAST = TimeFactors.DecoderNearestSense.select("AlwaysPast");
		final TimeFactors.NearestSense ALWAYSFUTURE = TimeFactors.DecoderNearestSense.select("AlwaysFuture");
		final TimeFactors.AlignSense ROUND = TimeFactors.DecoderAlignSense.select("round");
		final TimeFactors.AlignSense FLOOR = TimeFactors.DecoderAlignSense.select("floor");
		final TimeFactors.AlignSense CEILING = TimeFactors.DecoderAlignSense.select("ceiling");
		{
			final long e12h = ElapsedFactory.ms("12h");
			final TimeOfDayRule r = TimeOfDayFactory.newRule("2200+ 12h");
			final TimeFactors proximity = tf.newNearest(r, PROXIMITY);
			final TimeFactors past = tf.newNearest(r, ALWAYSPAST);
			final TimeFactors future = tf.newNearest(r, ALWAYSFUTURE);
			final TimeFactors round = tf.newAlignedInterval(e12h, ROUND);
			final TimeFactors ceiling = tf.newAlignedInterval(e12h, CEILING);
			final TimeFactors floor = tf.newAlignedInterval(e12h, FLOOR);
			Assert.assertEquals("2010-Sep-25 (Sat) 22:00:00.000 GMT+12:00", proximity.toString());
			Assert.assertEquals("2010-Sep-25 (Sat) 22:00:00.000 GMT+12:00", past.toString());
			Assert.assertEquals("2010-Sep-26 (Sun) 10:00:00.000 GMT+13:00", future.toString());
			Assert.assertEquals("2010-Sep-26 (Sun) 00:00:00.000 GMT+12:00", round.toString());
			Assert.assertEquals("2010-Sep-26 (Sun) 12:00:00.000 GMT+13:00", ceiling.toString());
			Assert.assertEquals("2010-Sep-26 (Sun) 00:00:00.000 GMT+12:00", floor.toString());
		}
	}

	@Test
	public void t55_nearestDSTSpringPost2x12()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20100925T1416Z30M435");
		final TimeZone tz = TimeZoneFactory.selectById("Pacific/Auckland");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		Assert.assertEquals("2010-Sep-26 (Sun) 03:16:30.435 GMT+13:00", tf.toString());
		final TimeFactors.NearestSense PROXIMITY = TimeFactors.DecoderNearestSense.select("Proximity");
		final TimeFactors.NearestSense ALWAYSPAST = TimeFactors.DecoderNearestSense.select("AlwaysPast");
		final TimeFactors.NearestSense ALWAYSFUTURE = TimeFactors.DecoderNearestSense.select("AlwaysFuture");
		final TimeFactors.AlignSense ROUND = TimeFactors.DecoderAlignSense.select("round");
		final TimeFactors.AlignSense FLOOR = TimeFactors.DecoderAlignSense.select("floor");
		final TimeFactors.AlignSense CEILING = TimeFactors.DecoderAlignSense.select("ceiling");
		{
			final long e12h = ElapsedFactory.ms("12h");
			final TimeOfDayRule r = TimeOfDayFactory.newRule("2200+ 12h");
			final TimeFactors proximity = tf.newNearest(r, PROXIMITY);
			final TimeFactors past = tf.newNearest(r, ALWAYSPAST);
			final TimeFactors future = tf.newNearest(r, ALWAYSFUTURE);
			final TimeFactors round = tf.newAlignedInterval(e12h, ROUND);
			final TimeFactors ceiling = tf.newAlignedInterval(e12h, CEILING);
			final TimeFactors floor = tf.newAlignedInterval(e12h, FLOOR);
			Assert.assertEquals("2010-Sep-25 (Sat) 22:00:00.000 GMT+12:00", proximity.toString());
			Assert.assertEquals("2010-Sep-25 (Sat) 22:00:00.000 GMT+12:00", past.toString());
			Assert.assertEquals("2010-Sep-26 (Sun) 10:00:00.000 GMT+13:00", future.toString());
			Assert.assertEquals("2010-Sep-26 (Sun) 00:00:00.000 GMT+12:00", round.toString());
			Assert.assertEquals("2010-Sep-26 (Sun) 12:00:00.000 GMT+13:00", ceiling.toString());
			Assert.assertEquals("2010-Sep-26 (Sun) 00:00:00.000 GMT+12:00", floor.toString());
		}
	}

	@Test
	public void t56_nearest_sod()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20091119T1316Z30M435");
		final TimeZone tz = TimeZoneFactory.selectById("Pacific/Auckland");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		{
			final int SOD_0930_00 = 9 * 3600 + 30 * 60;
			final int SOD_2130_00 = 21 * 3600 + 30 * 60;
			final TimeFactors proximity0930 = tf.newNearest(SOD_0930_00, TimeFactors.NearestSense.Proximity);
			final TimeFactors past0930 = tf.newNearest(SOD_0930_00, TimeFactors.NearestSense.AlwaysPast);
			final TimeFactors future0930 = tf.newNearest(SOD_0930_00, TimeFactors.NearestSense.AlwaysFuture);
			final TimeFactors proximity2130 = tf.newNearest(SOD_2130_00, TimeFactors.NearestSense.Proximity);
			final TimeFactors past2130 = tf.newNearest(SOD_2130_00, TimeFactors.NearestSense.AlwaysPast);
			final TimeFactors future2130 = tf.newNearest(SOD_2130_00, TimeFactors.NearestSense.AlwaysFuture);
			Assert.assertEquals("2009-Nov-20 (Fri) 09:30:00.000 GMT+13:00", proximity0930.toString());
			Assert.assertEquals("2009-Nov-19 (Thu) 09:30:00.000 GMT+13:00", past0930.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 09:30:00.000 GMT+13:00", future0930.toString());
			Assert.assertEquals("2009-Nov-19 (Thu) 21:30:00.000 GMT+13:00", proximity2130.toString());
			Assert.assertEquals("2009-Nov-19 (Thu) 21:30:00.000 GMT+13:00", past2130.toString());
			Assert.assertEquals("2009-Nov-20 (Fri) 21:30:00.000 GMT+13:00", future2130.toString());
		}
	}

	@Test
	public void t60_ice()
			throws ArgonFormatException, ArgonApiException {
		final TimeZone NZ = TimeZoneFactory.selectById("Pacific/Auckland");
		final TimeZone GMT = TimeZoneFactory.GMT;
		final TimeFactors.NearestSense PROXIMITY = TimeFactors.NearestSense.Proximity;
		final TimeMask tm = TimeMask.newInstance("[YEAR][MONZ][DOMZ] [H24Z][MINZ]");

		final Date d0000 = DateFactory.newDateConstantFromTX("20061015T0000Z");
		final Date d0215 = DateFactory.newDateConstantFromTX("20061015T0215Z");
		final Date d1200 = DateFactory.newDateConstantFromTX("20061015T1200Z");
		final Date d2315 = DateFactory.newDateConstantFromTX("20061015T2315Z");

		final TimeFactors x0000 = TimeFactors.newInstance(d0000.getTime(), GMT);
		final TimeFactors x0215 = TimeFactors.newInstance(d0215.getTime(), GMT);
		final TimeFactors x1200 = TimeFactors.newInstance(d1200.getTime(), GMT);
		final TimeFactors x2315 = TimeFactors.newInstance(d2315.getTime(), GMT);

		Assert.assertEquals("20061015 0000", tm.format(x0215.newNearest(TimeOfDayFactory.newRule("0000"), PROXIMITY)));
		Assert.assertEquals("20061015 0215", tm.format(x0215.newNearest(TimeOfDayFactory.newRule("0215"), PROXIMITY)));
		Assert.assertEquals("20061015 0230", tm.format(x0215.newNearest(TimeOfDayFactory.newRule("0230"), PROXIMITY)));
		Assert.assertEquals("20061015 1414", tm.format(x0215.newNearest(TimeOfDayFactory.newRule("1414"), PROXIMITY)));
		Assert.assertEquals("20061015 1415", tm.format(x0215.newNearest(TimeOfDayFactory.newRule("1415"), PROXIMITY)));
		Assert.assertEquals("20061014 1416", tm.format(x0215.newNearest(TimeOfDayFactory.newRule("1416"), PROXIMITY)));
		Assert.assertEquals("20061014 1530", tm.format(x0215.newNearest(TimeOfDayFactory.newRule("1530"), PROXIMITY)));
		Assert.assertEquals("20061014 2330", tm.format(x0215.newNearest(TimeOfDayFactory.newRule("2330"), PROXIMITY)));

		Assert.assertEquals("20061016 0000", tm.format(x2315.newNearest(TimeOfDayFactory.newRule("0000"), PROXIMITY)));
		Assert.assertEquals("20061016 0215", tm.format(x2315.newNearest(TimeOfDayFactory.newRule("0215"), PROXIMITY)));
		Assert.assertEquals("20061016 1114", tm.format(x2315.newNearest(TimeOfDayFactory.newRule("1114"), PROXIMITY)));
		Assert.assertEquals("20061016 1115", tm.format(x2315.newNearest(TimeOfDayFactory.newRule("1115"), PROXIMITY)));
		Assert.assertEquals("20061015 1116", tm.format(x2315.newNearest(TimeOfDayFactory.newRule("1116"), PROXIMITY)));

		Assert.assertEquals("20061015 0000", tm.format(x0000.newNearest(TimeOfDayFactory.newRule("0000"), PROXIMITY)));
		Assert.assertEquals("20061015 0215", tm.format(x0000.newNearest(TimeOfDayFactory.newRule("0215"), PROXIMITY)));
		Assert.assertEquals("20061015 1159", tm.format(x0000.newNearest(TimeOfDayFactory.newRule("1159"), PROXIMITY)));
		Assert.assertEquals("20061015 1200", tm.format(x0000.newNearest(TimeOfDayFactory.newRule("1200"), PROXIMITY)));
		Assert.assertEquals("20061014 1201", tm.format(x0000.newNearest(TimeOfDayFactory.newRule("1201"), PROXIMITY)));
		Assert.assertEquals("20061014 2359", tm.format(x0000.newNearest(TimeOfDayFactory.newRule("2359"), PROXIMITY)));
		//
		Assert.assertEquals("20061016 0000", tm.format(x1200.newNearest(TimeOfDayFactory.newRule("0000"), PROXIMITY)));
		Assert.assertEquals("20061015 0215", tm.format(x1200.newNearest(TimeOfDayFactory.newRule("0215"), PROXIMITY)));
		Assert.assertEquals("20061015 1159", tm.format(x1200.newNearest(TimeOfDayFactory.newRule("1159"), PROXIMITY)));
		Assert.assertEquals("20061015 1200", tm.format(x1200.newNearest(TimeOfDayFactory.newRule("1200"), PROXIMITY)));
		Assert.assertEquals("20061015 1201", tm.format(x1200.newNearest(TimeOfDayFactory.newRule("1201"), PROXIMITY)));
		Assert.assertEquals("20061015 2359", tm.format(x1200.newNearest(TimeOfDayFactory.newRule("2359"), PROXIMITY)));

		{
			final Date d = DateFactory.newDateConstantFromTX("20051001T0800Z");
			final TimeFactors x = TimeFactors.newInstance(d.getTime(), NZ); // GMT+12 to +13
			final TimeFactors y = x.newNearest(TimeOfDayFactory.newRule("0400"), PROXIMITY);
			final TimeFactors yz = y.newRezoned(GMT);
			Assert.assertEquals("20051001 1500", tm.format(yz));
			// time shift is +7hrs (not 4am - 8pm = 8hrs)
		}

		{
			final Date d = DateFactory.newDateConstantFromTX("20061015T0215Z");
			final TimeFactors x = TimeFactors.newInstance(d.getTime(), GMT);
			Assert.assertEquals("20061014 2300",
					tm.format(x.newNearest(TimeOfDayFactory.newRule("0700,2300,1400"), PROXIMITY)));
		}

		{
			final Date d = DateFactory.newDateConstantFromTX("20090926T1100Z");
			final TimeFactors x = TimeFactors.newInstance(d.getTime(), NZ); // GMT+12
			final TimeFactors y = x.newNearest(TimeOfDayFactory.newRule("0000"), PROXIMITY);
			final TimeFactors yz = y.newRezoned(GMT);
			Assert.assertEquals("20090926 1200", tm.format(yz));
		}

		{
			final Date d = DateFactory.newDateConstantFromTX("20090926T1100Z");
			final TimeFactors x = TimeFactors.newInstance(d.getTime(), NZ);
			final TimeFactors y = x.newNearest(TimeOfDayFactory.newRule("0400"), PROXIMITY);
			final TimeFactors yz = y.newRezoned(GMT);
			Assert.assertEquals("20090926 1500", tm.format(yz)); // GMT+13
		}
	}
}
