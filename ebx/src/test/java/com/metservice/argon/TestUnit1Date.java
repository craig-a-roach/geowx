/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Date;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1Date {

	@Test
	public void t20_parseHourOnly() {
		try {
			final Date d = DateFactory.newInstance("2010030412", TimeZoneFactory.GMT);
			Assert.assertEquals("20100304T1200Z00M000", DateFormatter.newT8FromDate(d));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t21_parseAllFields() {
		try {
			final Date d = DateFactory.newInstance("20100304124517", TimeZoneFactory.GMT);
			Assert.assertEquals("20100304T1245Z17M000", DateFormatter.newT8FromDate(d));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t22_parseRecent() {
		try {
			final Date d1 = DateFactory.newRecent("Apr", "02", "17:21", TimeZoneFactory.GMT, 2010, 7);
			Assert.assertEquals("20100402T1721Z00M000", DateFormatter.newT8FromDate(d1));
			final Date d2 = DateFactory.newRecent("August", "02", "8:21", TimeZoneFactory.GMT, 2010, 7);
			Assert.assertEquals("20100802T0821Z00M000", DateFormatter.newT8FromDate(d2));
			final Date d3 = DateFactory.newRecent("9", "02", "17:21", TimeZoneFactory.GMT, 2010, 7);
			Assert.assertEquals("20090902T1721Z00M000", DateFormatter.newT8FromDate(d3));
			final Date d4 = DateFactory.newRecent("12", "02", "0:21", TimeZoneFactory.GMT, 2010, 7);
			Assert.assertEquals("20091202T0021Z00M000", DateFormatter.newT8FromDate(d4));
			final Date y1 = DateFactory.newRecent("Sep", "02", "2009", TimeZoneFactory.GMT, 2010, 7);
			Assert.assertEquals("20090902T0000Z00M000", DateFormatter.newT8FromDate(y1));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t32_parseRegexNumericSameOrder() {
		try {
			final Pattern regex = Pattern.compile("(\\d+)T(\\d+)Z(\\d+)M(\\d+)");
			final Date d = DateFactory.newInstance("20100304T1245Z17M251", regex, null, TimeZoneFactory.GMT);
			Assert.assertEquals("20100304T1245Z17M251", DateFormatter.newT8FromDate(d));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t33_parseAlphaNumericSameOrder() {
		try {
			final Pattern regex = Pattern.compile("(\\d+)-(\\w+)-(\\d+)\\s+(\\d+):(\\d+)");
			final Date d = DateFactory.newInstance("2010-Mar-4 12:45", regex, null, TimeZoneFactory.GMT);
			Assert.assertEquals("20100304T1245Z00M000", DateFormatter.newT8FromDate(d));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t34_parseAMPMSameOrder() {
		try {
			final Pattern regex = Pattern.compile("(\\d+)-(\\w+)-(\\d+)\\s+(\\d+):(\\d+)(Am|Pm)");
			final Date d1 = DateFactory.newInstance("2010-Mar-4 12:45Am", regex, null, TimeZoneFactory.GMT);
			final Date d2 = DateFactory.newInstance("2010-Mar-4 6:45Am", regex, null, TimeZoneFactory.GMT);
			final Date d3 = DateFactory.newInstance("2010-Mar-4 12:45Pm", regex, null, TimeZoneFactory.GMT);
			final Date d4 = DateFactory.newInstance("2010-Mar-4 6:45Pm", regex, null, TimeZoneFactory.GMT);
			Assert.assertEquals("20100304T0045Z00M000", DateFormatter.newT8FromDate(d1));
			Assert.assertEquals("20100304T0645Z00M000", DateFormatter.newT8FromDate(d2));
			Assert.assertEquals("20100304T1245Z00M000", DateFormatter.newT8FromDate(d3));
			Assert.assertEquals("20100304T1845Z00M000", DateFormatter.newT8FromDate(d4));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t36_parseOffsetSameOrder() {
		try {
			final Pattern regex = Pattern.compile("(\\d+)-(\\d+)-(\\d+)\\s+(\\d+):(\\d+):(\\d+)[.](\\d+)\\s+([+-]\\d+)");
			final Date d1 = DateFactory.newInstance("2011-06-16 05:23:32.08265 +1200", regex, null, TimeZoneFactory.GMT);
			final Date d2 = DateFactory.newInstance("2011-06-16 05:23:32.08265 +12", regex, null, TimeZoneFactory.GMT);
			final Date d3 = DateFactory.newInstance("2011-06-15 17:23:32.08265 +0000", regex, null, TimeZoneFactory.GMT);
			final Date d4 = DateFactory.newInstance("2011-06-15 17:23:32.08265 +0", regex, null, TimeZoneFactory.GMT);
			final Date d5 = DateFactory.newInstance("2011-06-15 12:23:32.08265 -5", regex, null, TimeZoneFactory.GMT);
			Assert.assertEquals("20110615T1723Z32M082", DateFormatter.newT8FromDate(d1));
			Assert.assertEquals("20110615T1723Z32M082", DateFormatter.newT8FromDate(d2));
			Assert.assertEquals("20110615T1723Z32M082", DateFormatter.newT8FromDate(d3));
			Assert.assertEquals("20110615T1723Z32M082", DateFormatter.newT8FromDate(d4));
			Assert.assertEquals("20110615T1723Z32M082", DateFormatter.newT8FromDate(d5));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t40_parseAlphaNumericReOrder() {
		try {
			final Pattern regex = Pattern.compile("(\\d{1,2})-(\\w+)-(\\d{4})\\s+(\\d+):(\\d+)");
			final int[] reorder = { 3, 2, 1, 4, 5 };
			final Date d = DateFactory.newInstance("4-MARCH-2010 8:45", regex, reorder, TimeZoneFactory.GMT);
			Assert.assertEquals("20100304T0845Z00M000", DateFormatter.newT8FromDate(d));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test(expected = ArgonFormatException.class)
	public void t50_rejectHourOnly()
			throws ArgonFormatException {
		try {
			DateFactory.newInstance("2010022912", TimeZoneFactory.GMT);
		} catch (final ArgonFormatException ex) {
			System.out.println("Good: " + ex.getMessage());
			throw ex;
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test(expected = ArgonFormatException.class)
	public void t51_rejectAM()
			throws ArgonFormatException {
		try {
			final Pattern regex = Pattern.compile("(\\d+)-(\\w+)-(\\d+)\\s+(\\d+):(\\d+)(Am|Pm)");
			DateFactory.newInstance("2010-Mar-4 13:45Am", regex, null, TimeZoneFactory.GMT);
		} catch (final ArgonFormatException ex) {
			System.out.println("Good: " + ex.getMessage());
			throw ex;
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test(expected = ArgonFormatException.class)
	public void t52_rejectOffset()
			throws ArgonFormatException {
		try {
			final Pattern regex = Pattern.compile("(\\d+)-(\\d+)-(\\d+)\\s+(\\d+):(\\d+):(\\d+)[.](\\d+)\\s+(\\w+)");
			DateFactory.newInstance("2011-06-16 05:23:32.08265 1200", regex, null, TimeZoneFactory.GMT);
		} catch (final ArgonFormatException ex) {
			System.out.println("Good: " + ex.getMessage());
			throw ex;
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t60_format() {
		try {
			final String s = "20080229T2359Z59M999";
			Assert.assertTrue(DateFactory.isWellFormedT8(s));
			final Date d = DateFactory.newDateFromT8(s);
			final String t = DateFormatter.newT8FromDate(d);
			Assert.assertEquals(s, t);
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t62_wellFormed() {
		Assert.assertTrue(DateFactory.isWellFormedT8("20000229T2359Z59M999"));
		Assert.assertFalse(DateFactory.isWellFormedT8("20100228T2459Z59M999"));
		Assert.assertFalse(DateFactory.isWellFormedT8("20100228TX459Z59M999"));
		Assert.assertFalse(DateFactory.isWellFormedT8("20100229T2359Z59M999"));
		Assert.assertFalse(DateFactory.isWellFormedT8("21000229T2359Z59M999"));
		Assert.assertTrue(DateFactory.isWellFormedTX("20000229T2359Z59M999"));
		Assert.assertFalse(DateFactory.isWellFormedTX("20100228T2459Z59M999"));
		Assert.assertFalse(DateFactory.isWellFormedTX("20100228TX459Z59M999"));
		Assert.assertFalse(DateFactory.isWellFormedTX("20100229T2359Z59M999"));
		Assert.assertFalse(DateFactory.isWellFormedTX("21000229T2359Z59M999"));
		Assert.assertTrue(DateFactory.isWellFormedTX("20000229T2359Z59"));
		Assert.assertTrue(DateFactory.isWellFormedTX("20000229T2359Z"));
		Assert.assertFalse(DateFactory.isWellFormedTX("20100228T2459Z"));
		Assert.assertFalse(DateFactory.isWellFormedTX("20100228TX459Z"));
		Assert.assertFalse(DateFactory.isWellFormedTX("20100229T2359Z"));
		Assert.assertFalse(DateFactory.isWellFormedTX("20000229T"));
	}

	@Test
	public void t70_span()
			throws ArgonFormatException, ArgonApiException {
		final Date d1 = DateFactory.newInstance("20100802034517", TimeZoneFactory.GMT);
		final Date d2 = DateFactory.newInstance("20100802052013", TimeZoneFactory.GMT);
		final DateSpan d1onwards = DateSpan.newOnwards(d1.getTime());
		final DateSpan untild1 = DateSpan.newUntil(d1.getTime());
		final DateSpan da2 = DateSpan.newAt(d2.getTime());
		final DateSpan d12 = DateSpan.newInstance(d1.getTime(), d2.getTime());
		final DateSpan d12t = DateSpan.newTranspose(d2.getTime(), d1.getTime());
		Assert.assertTrue(d12.equals(d12t));
		Assert.assertEquals(d12.hashCode(), d12t.hashCode());
		Assert.assertTrue(d1onwards.compareTo(da2) < 0);
		Assert.assertTrue(d12t.compareTo(d12) == 0);
		Assert.assertTrue(d1onwards.compareTo(d12) > 0);
		Assert.assertTrue(d1onwards.contains(d2.getTime()));
		Assert.assertTrue(d1onwards.intersects(da2, false));
		Assert.assertTrue(d1onwards.intersects(untild1, true));
	}
}
