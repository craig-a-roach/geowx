/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1Unit {

	@Test
	public void t10_ft() {
		final Unit oU = UnitDictionary.findByName("ft");
		Assert.assertNotNull("feet", oU);
		Assert.assertEquals("4 feet to metres", 1.2192, oU.toBase(4.0), 1e-3);
	}

	@Test
	public void t20_deg() {
		final Unit oU = UnitDictionary.findByName("\u00B0");
		Assert.assertNotNull("degrees", oU);
		Assert.assertTrue("eq", oU.equals(Unit.DEGREES));
		Assert.assertEquals("to deg", Math.PI, oU.toBase(180.0), 1e-6);
	}

	@Test
	public void t30_min() {
		final Unit oU = UnitDictionary.findByName("\'");
		Assert.assertNotNull("arc minutes", oU);
		Assert.assertEquals("to deg", (Math.PI / 180.0) * 0.75, oU.toBase(45.0), 1e-6);
	}

	@Test
	public void t40_rad() {
		final Unit oU = UnitDictionary.findByName("radian");
		Assert.assertNotNull("radians", oU);
	}
}
