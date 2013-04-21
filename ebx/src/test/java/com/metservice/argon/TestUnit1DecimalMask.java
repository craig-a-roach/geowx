/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1DecimalMask {

	@Test(expected = ArgonApiException.class)
	public void t48()
			throws ArgonApiException {
		DecimalMask.newInstance("d+03.2");
	}

	@Test
	public void t49_floating() {
		try {
			final Real rNAN = Real.newInstance("-0.1r").unarySquareRoot();
			final Real rINF = Real.binaryDivide(Real.newInstance("1.0r"), 0.0);
			Assert.assertEquals("+27.317", DecimalMask.newInstance("d+").format(27.317));
			Assert.assertEquals("+27.317", DecimalMask.newInstance("d+").format(Real.newInstance("27.317r")));
			Assert.assertEquals("+7.317", DecimalMask.newInstance("d+").format(7.317));
			Assert.assertEquals("+7.317", DecimalMask.newInstance("d+").format(Real.newInstance("7.317r")));
			Assert.assertEquals("+0.317", DecimalMask.newInstance("d+").format(0.317));
			Assert.assertEquals("+0.317", DecimalMask.newInstance("d+").format(Real.newInstance("0.317r")));
			Assert.assertEquals("0.0", DecimalMask.newInstance("d+").format(0.0));
			Assert.assertEquals("-0.317", DecimalMask.newInstance("d+").format(-0.317));
			Assert.assertEquals("-0.317", DecimalMask.newInstance("d+").format(Real.newInstance("-0.317r")));
			Assert.assertEquals("-5.517", DecimalMask.newInstance("d+").format(-5.517));
			Assert.assertEquals("-25.517", DecimalMask.newInstance("d+").format(-25.517));
			Assert.assertEquals("+2.7317E19", DecimalMask.newInstance("d+").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("d+").format(-2.7317E19));
			Assert.assertEquals("NaN", DecimalMask.newInstance("d+").format(Double.NaN));
			Assert.assertEquals("NaN", DecimalMask.newInstance("d+").format(rNAN));
			Assert.assertEquals("+Infinity", DecimalMask.newInstance("d+").format(rINF));

			Assert.assertEquals("27.317", DecimalMask.newInstance("d").format(27.317));
			Assert.assertEquals("7.317", DecimalMask.newInstance("d").format(7.317));
			Assert.assertEquals("0.317", DecimalMask.newInstance("d").format(0.317));
			Assert.assertEquals("0.0", DecimalMask.newInstance("d").format(0.0));
			Assert.assertEquals("-0.317", DecimalMask.newInstance("d").format(-0.317));
			Assert.assertEquals("-5.517", DecimalMask.newInstance("d").format(-5.517));
			Assert.assertEquals("-25.517", DecimalMask.newInstance("d").format(-25.517));
			Assert.assertEquals("2.7317E19", DecimalMask.newInstance("d").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("d").format(-2.7317E19));
			Assert.assertEquals("NaN", DecimalMask.newInstance("d").format(Double.NaN));

		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.toString());
		}
	}

	@Test
	public void t50_fixed() {
		try {
			Assert.assertEquals("+27.32", DecimalMask.newInstance("f+03.2").format(27.317));
			Assert.assertEquals("+27.32", DecimalMask.newInstance("f+03.2").format(Real.newInstance("27.317r")));
			Assert.assertEquals("+27.317", DecimalMask.newInstance("f+03").format(Real.newInstance("27.317r")));
			Assert.assertEquals("+07.32", DecimalMask.newInstance("f+03.2").format(7.317));
			Assert.assertEquals("+00.32", DecimalMask.newInstance("f+03.2").format(0.317));
			Assert.assertEquals("+00.32", DecimalMask.newInstance("f+03.2").format(Real.newInstance("0.317r")));
			Assert.assertEquals("+00.317", DecimalMask.newInstance("f+03").format(Real.newInstance("0.317r")));
			Assert.assertEquals("000.00", DecimalMask.newInstance("f+03.2").format(0.0));
			Assert.assertEquals("-00.32", DecimalMask.newInstance("f+03.2").format(-0.317));
			Assert.assertEquals("-00.32", DecimalMask.newInstance("f+03.2").format(Real.newInstance("-0.317r")));
			Assert.assertEquals("-05.52", DecimalMask.newInstance("f+03.2").format(-5.517));
			Assert.assertEquals("-25.52", DecimalMask.newInstance("f+03.2").format(-25.517));
			Assert.assertEquals("+2.7317E19", DecimalMask.newInstance("f+03.2").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f+03.2").format(-2.7317E19));
			Assert.assertEquals("+2.7317E-19", DecimalMask.newInstance("f+03.2").format(2.7317E-19));
			Assert.assertEquals("-2.7317E-19", DecimalMask.newInstance("f+03.2").format(-2.7317E-19));
			Assert.assertEquals("NaN", DecimalMask.newInstance("f+03.2").format(Double.NaN));

			Assert.assertEquals("027.32", DecimalMask.newInstance("f03.2").format(27.317));
			Assert.assertEquals("007.32", DecimalMask.newInstance("f03.2").format(7.317));
			Assert.assertEquals("000.32", DecimalMask.newInstance("f03.2").format(0.317));
			Assert.assertEquals("000.00", DecimalMask.newInstance("f03.2").format(0.0));
			Assert.assertEquals("-00.32", DecimalMask.newInstance("f03.2").format(-0.317));
			Assert.assertEquals("-05.52", DecimalMask.newInstance("f03.2").format(-5.517));
			Assert.assertEquals("-25.52", DecimalMask.newInstance("f03.2").format(-25.517));
			Assert.assertEquals("2.7317E19", DecimalMask.newInstance("f03.2").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f03.2").format(-2.7317E19));
			Assert.assertEquals("NaN", DecimalMask.newInstance("f03.2").format(Double.NaN));

			Assert.assertEquals("+27.32", DecimalMask.newInstance("f+3.2").format(27.317));
			Assert.assertEquals(" +7.32", DecimalMask.newInstance("f+3.2").format(7.317));
			Assert.assertEquals(" +0.32", DecimalMask.newInstance("f+3.2").format(0.317));
			Assert.assertEquals("  0.00", DecimalMask.newInstance("f+3.2").format(0.0));
			Assert.assertEquals(" -0.32", DecimalMask.newInstance("f+3.2").format(-0.317));
			Assert.assertEquals(" -5.52", DecimalMask.newInstance("f+3.2").format(-5.517));
			Assert.assertEquals("-25.52", DecimalMask.newInstance("f+3.2").format(-25.517));
			Assert.assertEquals("+2.7317E19", DecimalMask.newInstance("f+3.2").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f+3.2").format(-2.7317E19));
			Assert.assertEquals("NaN", DecimalMask.newInstance("f+3.2").format(Double.NaN));

			Assert.assertEquals(" 27.32", DecimalMask.newInstance("f3.2").format(27.317));
			Assert.assertEquals("  7.32", DecimalMask.newInstance("f3.2").format(7.317));
			Assert.assertEquals("  0.32", DecimalMask.newInstance("f3.2").format(0.317));
			Assert.assertEquals("  0.00", DecimalMask.newInstance("f3.2").format(0.0));
			Assert.assertEquals(" -0.32", DecimalMask.newInstance("f3.2").format(-0.317));
			Assert.assertEquals(" -5.52", DecimalMask.newInstance("f3.2").format(-5.517));
			Assert.assertEquals("-25.52", DecimalMask.newInstance("f3.2").format(-25.517));
			Assert.assertEquals("2.7317E19", DecimalMask.newInstance("f3.2").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f3.2").format(-2.7317E19));
			Assert.assertEquals("NaN", DecimalMask.newInstance("f3.2").format(Double.NaN));

			Assert.assertEquals("000.00", DecimalMask.newInstance("f+~03.2").format(0.0));
			Assert.assertEquals("000.00", DecimalMask.newInstance("f~03.2").format(0.0));
			Assert.assertEquals("  0.00", DecimalMask.newInstance("f+~3.2").format(0.0));
			Assert.assertEquals("  0.00", DecimalMask.newInstance("f~3.2").format(0.0));

			Assert.assertEquals("+27", DecimalMask.newInstance("f+~03.0").format(27.317));
			Assert.assertEquals("+27", DecimalMask.newInstance("f+~03.0").format(Real.newInstance("27.317r")));
			Assert.assertEquals("+07", DecimalMask.newInstance("f+~03.0").format(7.317));
			Assert.assertEquals("+00", DecimalMask.newInstance("f+~03.0").format(0.317));
			Assert.assertEquals("000", DecimalMask.newInstance("f+~03.0").format(0.0));
			Assert.assertEquals("-00", DecimalMask.newInstance("f+~03.0").format(-0.317));
			Assert.assertEquals("-06", DecimalMask.newInstance("f+~03.0").format(-5.517));
			Assert.assertEquals("-26", DecimalMask.newInstance("f+~03.0").format(-25.517));
			Assert.assertEquals("+2.7317E19", DecimalMask.newInstance("f+~03.0").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f+~03.0").format(-2.7317E19));

			Assert.assertEquals("027", DecimalMask.newInstance("f~03.0").format(27.317));
			Assert.assertEquals("007", DecimalMask.newInstance("f~03.0").format(7.317));
			Assert.assertEquals("000", DecimalMask.newInstance("f~03.0").format(0.317));
			Assert.assertEquals("000", DecimalMask.newInstance("f~03.0").format(0.0));
			Assert.assertEquals("-00", DecimalMask.newInstance("f~03.0").format(-0.317));
			Assert.assertEquals("-06", DecimalMask.newInstance("f~03.0").format(-5.517));
			Assert.assertEquals("-26", DecimalMask.newInstance("f~03.0").format(-25.517));
			Assert.assertEquals("2.7317E19", DecimalMask.newInstance("f~03.0").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f~03.0").format(-2.7317E19));

			Assert.assertEquals("+27", DecimalMask.newInstance("f+~3.0").format(27.317));
			Assert.assertEquals(" +7", DecimalMask.newInstance("f+~3.0").format(7.317));
			Assert.assertEquals(" +0", DecimalMask.newInstance("f+~3.0").format(0.317));
			Assert.assertEquals("  0", DecimalMask.newInstance("f+~3.0").format(0.0));
			Assert.assertEquals(" -0", DecimalMask.newInstance("f+~3.0").format(-0.317));
			Assert.assertEquals(" -6", DecimalMask.newInstance("f+~3.0").format(-5.517));
			Assert.assertEquals("-26", DecimalMask.newInstance("f+~3.0").format(-25.517));
			Assert.assertEquals("+2.7317E19", DecimalMask.newInstance("f+~3.0").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f+~3.0").format(-2.7317E19));

			Assert.assertEquals(" 27", DecimalMask.newInstance("f~3.0").format(27.317));
			Assert.assertEquals("  7", DecimalMask.newInstance("f~3.0").format(7.317));
			Assert.assertEquals("  0", DecimalMask.newInstance("f~3.0").format(0.317));
			Assert.assertEquals("  0", DecimalMask.newInstance("f~3.0").format(0.0));
			Assert.assertEquals(" -0", DecimalMask.newInstance("f~3.0").format(-0.317));
			Assert.assertEquals(" -6", DecimalMask.newInstance("f~3.0").format(-5.517));
			Assert.assertEquals("-26", DecimalMask.newInstance("f~3.0").format(-25.517));
			Assert.assertEquals("2.7317E19", DecimalMask.newInstance("f~3.0").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f~3.0").format(-2.7317E19));

			Assert.assertEquals(" 27", DecimalMask.newInstance("f3.0").format(27.317));
			Assert.assertEquals("  7", DecimalMask.newInstance("f3.0").format(7.317));
			Assert.assertEquals("  0", DecimalMask.newInstance("f3.0").format(0.317));
			Assert.assertEquals("  0", DecimalMask.newInstance("f3.0").format(0.0));
			Assert.assertEquals("  0", DecimalMask.newInstance("f3.0").format(-0.317));
			Assert.assertEquals(" -6", DecimalMask.newInstance("f3.0").format(-5.517));
			Assert.assertEquals("-26", DecimalMask.newInstance("f3.0").format(-25.517));
			Assert.assertEquals("2.7317E19", DecimalMask.newInstance("f3.0").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f3.0").format(-2.7317E19));

			Assert.assertEquals("+27.32", DecimalMask.newInstance("f+.2").format(27.317));
			Assert.assertEquals("+7.32", DecimalMask.newInstance("f+.2").format(7.317));
			Assert.assertEquals("+0.32", DecimalMask.newInstance("f+.2").format(0.317));
			Assert.assertEquals("0.00", DecimalMask.newInstance("f+.2").format(0.0));
			Assert.assertEquals("-0.32", DecimalMask.newInstance("f+.2").format(-0.317));
			Assert.assertEquals("-5.52", DecimalMask.newInstance("f+.2").format(-5.517));
			Assert.assertEquals("-25.52", DecimalMask.newInstance("f+.2").format(-25.517));
			Assert.assertEquals("+2.7317E19", DecimalMask.newInstance("f+.2").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f+.2").format(-2.7317E19));
			Assert.assertEquals("NaN", DecimalMask.newInstance("f+.2").format(Double.NaN));

			Assert.assertEquals("27.32", DecimalMask.newInstance("f.2").format(27.317));
			Assert.assertEquals("7.32", DecimalMask.newInstance("f.2").format(7.317));
			Assert.assertEquals("0.32", DecimalMask.newInstance("f.2").format(0.317));
			Assert.assertEquals("0.00", DecimalMask.newInstance("f.2").format(0.0));
			Assert.assertEquals("-0.32", DecimalMask.newInstance("f.2").format(-0.317));
			Assert.assertEquals("-5.52", DecimalMask.newInstance("f.2").format(-5.517));
			Assert.assertEquals("-25.52", DecimalMask.newInstance("f.2").format(-25.517));
			Assert.assertEquals("2.7317E19", DecimalMask.newInstance("f.2").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f.2").format(-2.7317E19));
			Assert.assertEquals("NaN", DecimalMask.newInstance("f.2").format(Double.NaN));

			Assert.assertEquals("27", DecimalMask.newInstance("f.0").format(27.317));
			Assert.assertEquals("7", DecimalMask.newInstance("f.0").format(7.317));
			Assert.assertEquals("0", DecimalMask.newInstance("f.0").format(0.317));
			Assert.assertEquals("0", DecimalMask.newInstance("f.0").format(0.0));
			Assert.assertEquals("0", DecimalMask.newInstance("f.0").format(-0.317));
			Assert.assertEquals("-6", DecimalMask.newInstance("f.0").format(-5.517));
			Assert.assertEquals("-26", DecimalMask.newInstance("f.0").format(-25.517));
			Assert.assertEquals("2.7317E19", DecimalMask.newInstance("f.0").format(2.7317E19));
			Assert.assertEquals("-2.7317E19", DecimalMask.newInstance("f.0").format(-2.7317E19));
			Assert.assertEquals("NaN", DecimalMask.newInstance("f.0").format(Double.NaN));

		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		} catch (final ArgonFormatException ex) {
			Assert.fail(ex.toString());
		}
	}

	@Test
	public void t60_integral() {
		try {
			Assert.assertEquals("000", DecimalMask.newInstance("f03").format(0));
			Assert.assertEquals("003", DecimalMask.newInstance("f03").format(3));
		} catch (final ArgonApiException ex) {
			Assert.fail(ex.toString());
		}
	}
}
