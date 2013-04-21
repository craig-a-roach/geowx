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
public class TestUnit1Elapsed {

	@Test
	public void t50_wellFormed() {
		Assert.assertTrue(ElapsedFactory.isWellFormed("0"));
		Assert.assertTrue(ElapsedFactory.isWellFormed("+3d"));
		Assert.assertTrue(ElapsedFactory.isWellFormed("-3d5h"));
		Assert.assertTrue(ElapsedFactory.isWellFormed("+0d"));

		Assert.assertFalse(ElapsedFactory.isWellFormed("3"));
		Assert.assertFalse(ElapsedFactory.isWellFormed("3a"));
		Assert.assertFalse(ElapsedFactory.isWellFormed("3.1"));
		Assert.assertFalse(ElapsedFactory.isWellFormed("-3d+5h"));
	}

	@Test
	public void t55_msParse() {
		try {
			Assert.assertEquals(0L, ElapsedFactory.ms("0"));
			Assert.assertEquals(0L, ElapsedFactory.ms("0h"));
			Assert.assertEquals(500L, ElapsedFactory.ms("500t"));
			Assert.assertEquals(3L * -1000, ElapsedFactory.ms("-3s"));
			Assert.assertEquals((1L * 60 + 5) * 1000, ElapsedFactory.ms("+1m5s"));
			Assert.assertEquals(((2L * 60 + 10L) * 60 + 15) * 1000, ElapsedFactory.ms("2h10m15s"));
			Assert.assertEquals((((1 * 24L + 12) * 60 + 7L) * 60 + 95) * -1000, ElapsedFactory.ms("-1d12h07m95s"));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t56_msParse() {
		try {
			Assert.assertEquals((500 * 24L) * 60L * 60L * 1000L, ElapsedFactory.ms("500d"));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t60_formatPositive() {
		try {
			Assert.assertEquals("2h30m", ElapsedFormatter.formatMixedUnits(ElapsedFactory.ms("2h30m")));
			Assert.assertEquals("150m", ElapsedFormatter.formatSingleUnit(ElapsedFactory.ms("2h30m")));
			Assert.assertEquals("150m", ElapsedFormatter.formatUnit(ElapsedFactory.ms("2h30m"), ElapsedUnit.Minutes, true));
			Assert.assertEquals("2h", ElapsedFormatter.formatUnit(ElapsedFactory.ms("2h30m"), ElapsedUnit.Hours, false));
			Assert.assertEquals("3h", ElapsedFormatter.formatUnit(ElapsedFactory.ms("2h30m"), ElapsedUnit.Hours, true));
			Assert.assertEquals("0d", ElapsedFormatter.formatUnit(ElapsedFactory.ms("2h30m"), ElapsedUnit.Days, true));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t61_formatNegative() {
		try {
			Assert.assertEquals("-2m30s", ElapsedFormatter.formatMixedUnits(ElapsedFactory.ms("-2m30s")));
			Assert.assertEquals("-150s", ElapsedFormatter.formatSingleUnit(ElapsedFactory.ms("-2m30s")));
			Assert.assertEquals("-150s", ElapsedFormatter.formatUnit(ElapsedFactory.ms("-2m30s"), ElapsedUnit.Seconds, true));
			Assert.assertEquals("-2m", ElapsedFormatter.formatUnit(ElapsedFactory.ms("-2m30s"), ElapsedUnit.Minutes, false));
			Assert.assertEquals("-3m", ElapsedFormatter.formatUnit(ElapsedFactory.ms("-2m30s"), ElapsedUnit.Minutes, true));
			Assert.assertEquals("0h", ElapsedFormatter.formatUnit(ElapsedFactory.ms("-2m30s"), ElapsedUnit.Hours, true));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t62_formatZero() {
		try {
			Assert.assertEquals("0", ElapsedFormatter.formatMixedUnits(ElapsedFactory.ms("0m")));
			Assert.assertEquals("0", ElapsedFormatter.formatSingleUnit(ElapsedFactory.ms("0m")));
			Assert.assertEquals("0m", ElapsedFormatter.formatUnit(ElapsedFactory.ms("-0"), ElapsedUnit.Minutes, false));
			Assert.assertEquals("0m", ElapsedFormatter.formatUnit(ElapsedFactory.ms("+0"), ElapsedUnit.Minutes, true));
			Assert.assertEquals("0h", ElapsedFormatter.formatUnit(ElapsedFactory.ms("+0m"), ElapsedUnit.Hours, false));
			Assert.assertEquals("0h", ElapsedFormatter.formatUnit(ElapsedFactory.ms("-0h"), ElapsedUnit.Hours, true));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test(expected = ArgonFormatException.class)
	public void t91_badParse1()
			throws ArgonFormatException {
		ElapsedFactory.ms("-3");
	}

	@Test(expected = ArgonFormatException.class)
	public void t91_badParse2()
			throws ArgonFormatException {
		ElapsedFactory.ms("+3a");
	}

}
