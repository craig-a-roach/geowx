/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
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
public class TestUnit1TimeMask {

	@Test(expected = ArgonApiException.class)
	public void t48()
			throws ArgonApiException {
		TimeMask.newInstance("[BUG]");
	}

	@Test
	public void t49()
			throws ArgonFormatException {
		final Date date = DateFactory.newDateConstantFromT8("20091120T0216Z30M000");
		final TimeZone tz = TimeZone.getTimeZone("Pacific/Auckland");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		try {
			final TimeMask tm = TimeMask.newInstance("[[[DOWC]DAY] [MONN]<<[MONZ]>");
			Assert.assertEquals("[FriDAY] November<11>", tm.format(tf));
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		}
	}

	@Test
	public void t50()
			throws ArgonFormatException {
		final Date date = DateFactory.newDateConstantFromT8("20091120T0216Z30M000");
		final TimeZone tz = TimeZone.getTimeZone("Pacific/Auckland");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		try {
			final TimeMask tm1 = TimeMask.newInstance("[H24Z][MINZ] [DOWN], [DOMZ]-[MONC]-[YEAR]");
			final TimeMask tm2 = TimeMask.newInstance("[TZN], [DOWC]");
			final TimeMask tm3 = TimeMask.newInstance("[TZRID], [DOWC]");
			final TimeMask tm4 = TimeMask.newInstance("[TZC], [DOWC]");
			final TimeMask tm5 = TimeMask.newInstance("[GMTHM], [DOWC]");
			Assert.assertEquals("1516 Friday, 20-Nov-2009", tm1.format(tf));
			Assert.assertEquals("New Zealand Daylight Time, Fri", tm2.format(tf));
			Assert.assertEquals("Pacific/Auckland, Fri", tm3.format(tf));
			Assert.assertEquals("NZDT, Fri", tm4.format(tf));
			Assert.assertEquals("GMT+13:00, Fri", tm5.format(tf));
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		}
	}

	@Test
	public void t51()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20091120T0216Z30M000");
		final TimeZone tz = TimeZoneFactory.selectById("Australia/Adelaide");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		try {
			final TimeMask tm1 = TimeMask.newInstance("[H24Z][MINZ] [DOWN], [DOMZ]-[MONC]-[YEAR]");
			final TimeMask tm2 = TimeMask.newInstance("[TZN], [DOWC]");
			final TimeMask tm3 = TimeMask.newInstance("[TZRID], [DOWC]");
			final TimeMask tm4 = TimeMask.newInstance("[TZC], [DOWC]");
			final TimeMask tm5 = TimeMask.newInstance("[GMTHM], [DOWC]");
			Assert.assertEquals("1246 Friday, 20-Nov-2009", tm1.format(tf));
			Assert.assertEquals("Central Summer Time (South Australia), Fri", tm2.format(tf));
			Assert.assertEquals("Australia/Adelaide, Fri", tm3.format(tf));
			Assert.assertEquals("CST, Fri", tm4.format(tf));
			Assert.assertEquals("GMT+10:30, Fri", tm5.format(tf));
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		}
	}

	@Test
	public void t52()
			throws ArgonFormatException {
		final Date date = DateFactory.newDateConstantFromT8("20091120T0216Z30M000");
		final TimeZone tz = TimeZone.getTimeZone("Australia/Perth");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		try {
			final TimeMask tm1 = TimeMask.newInstance("[H24Z][MINZ] [DOWN], [DOMZ]-[MONC]-[YEAR]");
			final TimeMask tm2 = TimeMask.newInstance("[TZN], [DOWC]");
			final TimeMask tm3 = TimeMask.newInstance("[TZRID], [DOWC]");
			final TimeMask tm4 = TimeMask.newInstance("[TZC], [DOWC]");
			final TimeMask tm5 = TimeMask.newInstance("[GMTHM], [DOWC]");
			Assert.assertEquals("1016 Friday, 20-Nov-2009", tm1.format(tf));
			Assert.assertEquals("Western Standard Time (Australia), Fri", tm2.format(tf));
			Assert.assertEquals("Australia/Perth, Fri", tm3.format(tf));
			Assert.assertEquals("WST, Fri", tm4.format(tf));
			Assert.assertEquals("GMT+08:00, Fri", tm5.format(tf));
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		}
	}

	@Test
	public void t53()
			throws ArgonFormatException {
		final Date date = DateFactory.newDateConstantFromT8("20091120T0216Z30M000");
		final TimeZone tz = TimeZone.getTimeZone("Australia/Sydney");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		try {
			final TimeMask tm1 = TimeMask.newInstance("[H24Z][MINZ] [DOWN], [DOMZ]-[MONC]-[YEAR]");
			final TimeMask tm2 = TimeMask.newInstance("[TZN], [DOWC]");
			final TimeMask tm3 = TimeMask.newInstance("[TZRID], [DOWC]");
			final TimeMask tm4 = TimeMask.newInstance("[TZC], [DOWC]");
			final TimeMask tm5 = TimeMask.newInstance("[GMTHM], [DOWC]");
			Assert.assertEquals("1316 Friday, 20-Nov-2009", tm1.format(tf));
			Assert.assertEquals("Eastern Summer Time (New South Wales), Fri", tm2.format(tf));
			Assert.assertEquals("Australia/Sydney, Fri", tm3.format(tf));
			Assert.assertEquals("EST, Fri", tm4.format(tf));
			Assert.assertEquals("GMT+11:00, Fri", tm5.format(tf));
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		}
	}

	@Test
	public void t54()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20091220T0216Z30M000");
		final TimeZone tz = TimeZoneFactory.selectById("America/Los_Angeles");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		try {
			final TimeMask tm1 = TimeMask.newInstance("[H24Z][MINZ] [DOWN], [DOMZ]-[MONC]-[YEAR]");
			final TimeMask tm2 = TimeMask.newInstance("[TZN], [DOWC]");
			final TimeMask tm3 = TimeMask.newInstance("[TZRID], [DOWC]");
			final TimeMask tm4 = TimeMask.newInstance("[TZC], [DOWC]");
			final TimeMask tm5 = TimeMask.newInstance("[GMTHM], [DOWC]");
			Assert.assertEquals("1816 Saturday, 19-Dec-2009", tm1.format(tf));
			Assert.assertEquals("Pacific Standard Time, Sat", tm2.format(tf));
			Assert.assertEquals("America/Los_Angeles, Sat", tm3.format(tf));
			Assert.assertEquals("PST, Sat", tm4.format(tf));
			Assert.assertEquals("GMT-08:00, Sat", tm5.format(tf));
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		}
	}

	@Test
	public void t55()
			throws ArgonFormatException, ArgonApiException {
		final Date date = DateFactory.newDateConstantFromT8("20091120T0216Z30M075");
		final TimeZone tz = TimeZoneFactory.selectById("NZ");
		final TimeFactors tf = TimeFactors.newInstance(date.getTime(), tz);
		try {
			final TimeMask tm1 = TimeMask.newInstance("[H24Z][MINZ]:[SECZ].[MILLISECZ] [DOWN], [DOMZ]-[MONC]-[YEAR] [GMTHM]");
			final TimeMask tm2 = TimeMask.newInstance("[H24Z][MINZ] [DOWN], [DOMZ]-[MONC]-[YEAR] [TZN]");
			final TimeMask tm3 = TimeMask.newInstance("[H24Z][MINZ] [DOMTH]");
			Assert.assertEquals("1516:30.075 Friday, 20-Nov-2009 GMT+13:00", tm1.format(tf));
			Assert.assertEquals("1516 Friday, 20-Nov-2009 New Zealand Daylight Time", tm2.format(tf));
			Assert.assertEquals("1516 20th", tm3.format(tf));
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		}
	}

	@Test
	public void t60()
			throws ArgonFormatException, ArgonApiException {
		final Date date1 = DateFactory.newDateConstantFromT8("20091120T0016Z00M000");
		final Date date2 = DateFactory.newDateConstantFromT8("20100620T0016Z00M000");
		final Date date3 = DateFactory.newDateConstantFromT8("20100620T0000Z00M000");
		final Date date4 = DateFactory.newDateConstantFromT8("20100620T1200Z00M000");
		final TimeZone tz = TimeZoneFactory.selectById("NZ");
		final TimeFactors tf1 = TimeFactors.newInstance(date1.getTime(), tz);
		final TimeFactors tf2 = TimeFactors.newInstance(date2.getTime(), tz);
		final TimeFactors tf3 = TimeFactors.newInstance(date3.getTime(), tz);
		final TimeFactors tf4 = TimeFactors.newInstance(date4.getTime(), tz);
		try {
			final TimeMask tmA = TimeMask
					.newInstance("[?IS NOON]<Midday><[H12Z][MINZ][AMPM]> [DOWN], [?IS DST]<NZDT([GMTHM])><NZST> [DOMZ]-[MONC]-[YEAR]");
			final TimeMask tmB = TimeMask
					.newInstance("[?CASE NOON END]<Midday><Midnight><[H12Z][MINZ][AMPM]> [DOWN], [?IS DST]<NZDT([GMTHM])><NZST> [DOMZ]-[MONC]-[YEAR]");
			final TimeMask tmC = TimeMask
					.newInstance("[USAGE=UNTIL][?CASE NOON END]<Midday><Midnight><[H12Z][MINZ][AMPM]> [DOWN], [?IS DST]<NZDT([GMTHM])><NZST> [DOMZ]-[MONC]-[YEAR]");
			final TimeMask tmD = TimeMask
					.newInstance("[?CASE AM NOON]<[H12]:[MINZ]AM><noon><[H12]:[MINZ]PM> [DOWN], [?IS DST]<NZDT([GMTHM])><NZST> [DOMZ]-[MONC]-[YEAR]");

			Assert.assertEquals("0116pm Friday, NZDT(GMT+13:00) 20-Nov-2009", tmA.format(tf1));
			Assert.assertEquals("0116pm Friday, NZDT(GMT+13:00) 20-Nov-2009", tmB.format(tf1));
			Assert.assertEquals("0115pm Friday, NZDT(GMT+13:00) 20-Nov-2009", tmC.format(tf1));
			Assert.assertEquals("1:16PM Friday, NZDT(GMT+13:00) 20-Nov-2009", tmD.format(tf1));

			Assert.assertEquals("1216pm Sunday, NZST 20-Jun-2010", tmA.format(tf2));
			Assert.assertEquals("1216pm Sunday, NZST 20-Jun-2010", tmB.format(tf2));
			Assert.assertEquals("1215pm Sunday, NZST 20-Jun-2010", tmC.format(tf2));
			Assert.assertEquals("12:16PM Sunday, NZST 20-Jun-2010", tmD.format(tf2));

			Assert.assertEquals("Midday Sunday, NZST 20-Jun-2010", tmA.format(tf3));
			Assert.assertEquals("Midday Sunday, NZST 20-Jun-2010", tmB.format(tf3));
			Assert.assertEquals("Midday Sunday, NZST 20-Jun-2010", tmC.format(tf3));
			Assert.assertEquals("noon Sunday, NZST 20-Jun-2010", tmD.format(tf3));

			Assert.assertEquals("1200am Monday, NZST 21-Jun-2010", tmA.format(tf4));
			Assert.assertEquals("1200am Monday, NZST 21-Jun-2010", tmB.format(tf4));
			Assert.assertEquals("Midnight Sunday, NZST 20-Jun-2010", tmC.format(tf4));
			Assert.assertEquals("12:00AM Monday, NZST 21-Jun-2010", tmD.format(tf4));
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		}
	}
}
