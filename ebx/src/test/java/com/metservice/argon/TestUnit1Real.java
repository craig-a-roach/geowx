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
public class TestUnit1Real {

	@Test
	public void t20_wellFormed() {
		Assert.assertTrue(Real.isWellFormed("5r"));
		Assert.assertTrue(Real.isWellFormed("5r-2"));
		Assert.assertTrue(Real.isWellFormed("-5r-2"));
		Assert.assertTrue(Real.isWellFormed("+5R-2"));
		Assert.assertTrue(Real.isWellFormed("+5r+2"));
		Assert.assertTrue(Real.isWellFormed("0.5r+2"));
		Assert.assertTrue(Real.isWellFormed("0.05r+2"));
		Assert.assertTrue(Real.isWellFormed("0.050r+2"));
		Assert.assertTrue(Real.isWellFormed("5.1r+2"));
		Assert.assertTrue(Real.isWellFormed("5.01r+2"));
		Assert.assertTrue(Real.isWellFormed("5.010r+2"));

		Assert.assertFalse(Real.isWellFormed("r2"));
		Assert.assertFalse(Real.isWellFormed("0r"));
		Assert.assertFalse(Real.isWellFormed("+-5r+2"));
		Assert.assertFalse(Real.isWellFormed("5r+-2"));
		Assert.assertFalse(Real.isWellFormed("5.2"));
		Assert.assertFalse(Real.isWellFormed("0"));
		Assert.assertFalse(Real.isWellFormed(".1r+2"));
		Assert.assertFalse(Real.isWellFormed(".1.r+2"));
	}

	@Test
	public void t30_construct() {
		try {
			{
				final Real x = Real.newInstance("54r-1");
				Assert.assertEquals(5.4, x.central(), 0.0001);
				Assert.assertEquals(0.05, x.errorMagnitude(), 0.0001);
			}
			{
				final Real x = Real.newInstance("5.40r");
				Assert.assertEquals(5.4, x.central(), 0.0001);
				Assert.assertEquals(0.005, x.errorMagnitude(), 0.0001);
				Assert.assertEquals(5L, x.convertToLong());
			}
			{
				final Real x = Real.newInstance("5.40r3");
				Assert.assertEquals(5400, x.central(), 0.0001);
				Assert.assertEquals(5, x.errorMagnitude(), 0.0001);
			}
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t32_relation() {
		try {
			{
				final Real x1 = Real.newInstance("5.30r");
				final Real x2 = Real.newInstance("5.31r");
				Assert.assertFalse(x1.equals(x2));
				Assert.assertTrue(x1.relationLessThan(x2));
				Assert.assertFalse(x2.relationLessThan(x1));
				Assert.assertFalse(x1.relationGreaterThan(x2));
				Assert.assertTrue(x2.relationGreaterThan(x1));
			}
			{
				final Real x1 = Real.newInstance("5.40r");
				final Real x2 = Real.newInstance("5.402r");
				Assert.assertTrue(x1.equals(x2));
				Assert.assertFalse(x1.relationLessThan(x2));
				Assert.assertFalse(x2.relationLessThan(x1));
				Assert.assertFalse(x1.relationGreaterThan(x2));
				Assert.assertFalse(x2.relationGreaterThan(x1));
			}
			{
				final Real x1 = Real.newInstance(3205246132L);
				final Real x2 = Real.newInstance(3205246132L);
				Assert.assertTrue(x1.equals(x2));
				Assert.assertFalse(x1.relationLessThan(x2));
				Assert.assertFalse(x2.relationLessThan(x1));
				Assert.assertFalse(x1.relationGreaterThan(x2));
				Assert.assertFalse(x2.relationGreaterThan(x1));
			}
			{
				final Real x = Real.newInstance("0.023r");
				Assert.assertTrue(x.isZero(0.1));
				Assert.assertTrue(x.isZero(0.023));
				Assert.assertFalse(x.isZero(0.01));
			}
			{
				final Real x = Real.newInstance("-5.40r");
				final Real d1 = Real.binaryDivide(x, 0.0);
				final Real d2 = x.unarySquareRoot();
				Assert.assertFalse(d1.isNaN());
				Assert.assertFalse(x.equals(d1));
				Assert.assertTrue(d2.isNaN());
				Assert.assertFalse(x.equals(d2));
			}
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t40_binaryMultiply() {
		try {
			{
				final Real x = Real.newInstance("5.40r");
				final Real y = Real.newInstance("0.10r");
				final Real z1 = Real.binaryMultiply(x, y);
				final Real z2 = Real.binaryMultiply(x, 0.1);
				Assert.assertEquals(0.54, z1.central(), 0.0001);
				Assert.assertEquals(0.055, z1.errorMagnitude(), 0.0001);
				Assert.assertEquals("0.5", DecimalMask.FixedNegSignNatural.format(z1));
				Assert.assertEquals("0.540", DecimalMask.FixedNegSignNatural.format(z2));
			}

		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t42_binaryDivide() {
		try {
			{
				final Real x = Real.newInstance("5.40r");
				final Real y = Real.newInstance("0.10r");
				final Real z1 = Real.binaryDivide(x, y);
				final Real z2 = Real.binaryDivide(x, 0.1);
				Assert.assertEquals(54.0, z1.central(), 0.0001);
				Assert.assertEquals("54", DecimalMask.FixedNegSignNatural.format(z1));
				Assert.assertEquals("54.0", DecimalMask.FixedNegSignNatural.format(z2));
			}

		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t43_binaryModulo() {
		try {
			{
				final Real x = Real.newInstance("5.40r");
				final Real y = Real.newInstance("2.5r");
				final Real z1 = Real.binaryModulo(x, y);
				final Real z2 = Real.binaryModulo(x, 2.5);
				Assert.assertEquals("0.4", DecimalMask.FixedNegSignNatural.format(z1));
				Assert.assertEquals("0.40", DecimalMask.FixedNegSignNatural.format(z2));
			}

		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t44_unarySquareRoot() {
		try {
			{
				final Real x = Real.newInstance("25.30r");
				final Real z = x.unarySquareRoot();
				Assert.assertEquals("5.030", DecimalMask.FixedNegSignNatural.format(z));
			}

		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t45_maxmin() {
		try {
			{
				final Real x = Real.newInstance("25.30r");
				final Real y = Real.newInstance("25.31r");
				final Real max1 = Real.binaryMax(x, y);
				final Real max2 = Real.binaryMax(y, x);
				final Real min1 = Real.binaryMin(x, y);
				final Real min2 = Real.binaryMin(y, x);
				Assert.assertTrue(max1 == y);
				Assert.assertTrue(max2 == y);
				Assert.assertTrue(min1 == x);
				Assert.assertTrue(min2 == x);
			}

		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void t50_plus() {

		try {
			Real sum = null;
			sum = Real.binaryPlus(Real.newInstance("6.1r"), sum);
			sum = Real.binaryPlus(Real.newInstance("5.9r"), sum);
			sum = Real.binaryPlus(Real.newInstance("8.0r"), sum);
			sum = Real.binaryPlus(Real.newInstance("5.3r"), sum);
			sum = Real.binaryPlus(Real.newInstance("7.7r"), sum);
			Assert.assertEquals("33.0", DecimalMask.FixedNegSignNatural.format(sum));
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.getMessage());
		}
	}
}
