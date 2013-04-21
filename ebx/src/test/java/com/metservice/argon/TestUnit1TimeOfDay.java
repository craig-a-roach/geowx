/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1TimeOfDay {

	@Test
	public void t40_parseSimple() {
		try {
			Assert.assertEquals(3 * 3600, TimeOfDayFactory.secondOfDay("3"));
			Assert.assertEquals(3 * 3600, TimeOfDayFactory.secondOfDay("03"));
			Assert.assertEquals(3 * 3600 + 45 * 60, TimeOfDayFactory.secondOfDay("345"));
			Assert.assertEquals(3 * 3600 + 45 * 60, TimeOfDayFactory.secondOfDay("0345"));
			Assert.assertEquals(3 * 3600 + 45 * 60 + 16, TimeOfDayFactory.secondOfDay("34516"));
			Assert.assertEquals(3 * 3600 + 45 * 60 + 16, TimeOfDayFactory.secondOfDay("034516"));
			Assert.assertEquals(0, TimeOfDayFactory.secondOfDay("0"));
			Assert.assertEquals(0, TimeOfDayFactory.secondOfDay("00"));
			Assert.assertEquals(0, TimeOfDayFactory.secondOfDay("000"));
			Assert.assertEquals(0, TimeOfDayFactory.secondOfDay("0000"));
			Assert.assertEquals(0, TimeOfDayFactory.secondOfDay("00000"));
			Assert.assertEquals(0, TimeOfDayFactory.secondOfDay("000000"));
			Assert.assertEquals(23 * 3600 + 59 * 60 + 59, TimeOfDayFactory.secondOfDay("235959"));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t42_parseRuleNoCap() {
		try {
			final TimeOfDayRule r = TimeOfDayFactory.newRule("0100+ 30m* 8, 0600, 0700+5m, 09, 11+ 1h5m*4, 0000+1m10s*2");
			Assert.assertEquals("010000+30m*8,060000,070000+5m*1,090000,110000+65m*4,000000+70s*2", r.toString());
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t43_parseRuleCap() {
		final int H = 60 * 60;
		final int M = 60;
		try {
			final TimeOfDayRule r1 = TimeOfDayFactory.newRule("1300+ 6h* 4");
			Assert.assertEquals("130000+6h*3", r1.toString());
			Assert.assertArrayEquals(new int[] { 1 * H, 7 * H, 13 * H, 19 * H }, r1.secondsOfDayAsc());
			final TimeOfDayRule r2 = TimeOfDayFactory.newRule("1300+ 12h*5");
			Assert.assertEquals("130000+12h*1", r2.toString());
			Assert.assertArrayEquals(new int[] { 1 * H, 13 * H }, r2.secondsOfDayAsc());
			final TimeOfDayRule r3 = TimeOfDayFactory.newRule("1300+ 6h* 4, 1300+12h*2, 1030");
			Assert.assertArrayEquals(new int[] { 1 * H, 7 * H, 10 * H + 30 * M, 13 * H, 19 * H }, r3.secondsOfDayAsc());

		} catch (final ArgonApiException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test(expected = ArgonApiException.class)
	public void t44_parseRuleBadEvery()
			throws ArgonApiException {
		try {
			TimeOfDayFactory.newRule("0100+ 0h* 4");
		} catch (final ArgonApiException ex) {
			System.out.println("Good: " + ex.getMessage());
			throw ex;
		}
	}

}
