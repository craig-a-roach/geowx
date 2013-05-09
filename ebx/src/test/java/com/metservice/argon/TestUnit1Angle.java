/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1Angle {

	private static final double Delta = 1e-9;

	@Test
	public void t10_dms() {
		try {
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("S41\u00B030m"), Delta);
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("41S30m"), Delta);
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("S41\u00B030"), Delta);
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("41S30"), Delta);
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("S41d1800\""), Delta);
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("41S1800\""), Delta);
			Assert.assertEquals(-41.505, AngleFactory.newDegrees("S41\u00B030\'18\""), Delta);
			Assert.assertEquals(-41.505, AngleFactory.newDegrees("41S30\'18\""), Delta);
			Assert.assertEquals(-41.505, AngleFactory.newDegrees("S41\u00B030m18"), Delta);
			Assert.assertEquals(-41.505, AngleFactory.newDegrees("41S30m18"), Delta);
			Assert.assertEquals(-41, AngleFactory.newDegrees("S41"), Delta);
			Assert.assertEquals(-41, AngleFactory.newDegrees("41S"), Delta);
			Assert.assertEquals(41, AngleFactory.newDegrees("N41"), Delta);
			Assert.assertEquals(41, AngleFactory.newDegrees("41N"), Delta);
		} catch (final ArgonFormatException ex) {
			Assert.assertEquals("ok", ex.getMessage());
		}
	}

	@Test
	public void t20_ms() {
		try {
			Assert.assertEquals(0.505, AngleFactory.newDegrees("30\'18\""), Delta);
			Assert.assertEquals(-0.505, AngleFactory.newDegrees("-30\'18"), Delta);
		} catch (final ArgonFormatException ex) {
			Assert.assertEquals("ok", ex.getMessage());
		}
	}

	@Test
	public void t30_decimal() {
		try {
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("-41.5"), Delta);
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("S41.5"), Delta);
			Assert.assertEquals(-41.5, AngleFactory.newDegrees("41.5S"), Delta);
			Assert.assertEquals(41.5, AngleFactory.newDegrees("+41.5"), Delta);
			Assert.assertEquals(41.5, AngleFactory.newDegrees("N41.5"), Delta);
			Assert.assertEquals(41.5, AngleFactory.newDegrees("41.5N"), Delta);
		} catch (final ArgonFormatException ex) {
			Assert.assertEquals("ok", ex.getMessage());
		}
	}

	@Test
	public void t50_reject() {
		final String[] bad = { "41.5x", "41dm", "41d40m20s13", "  " };
		for (int i = 0; i < bad.length; i++) {
			final String x = bad[i];
			try {
				AngleFactory.newDegrees(x);
				Assert.fail("Accepted '" + x + "'");
			} catch (final ArgonFormatException ex) {
				System.out.println("Good exception for '" + x + "':" + ex.getMessage());
			}
		}
	}

}
