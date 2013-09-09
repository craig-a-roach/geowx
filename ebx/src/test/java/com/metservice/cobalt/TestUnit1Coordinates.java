/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1Coordinates {

	ICobaltGeography G1() {
		return CobaltGeoLatitudeLongitude.newInstance(81.0, -53.0, 24.0, 51.0);
	}

	ICobaltGeography G2() {
		return CobaltGeoLatitudeLongitude.newInstance(65.0, -21.0, 35.0, 45.0);
	}

	@Test
	public void t10_grid() {
		final ICobaltGeography g1a = G1();
		final ICobaltGeography g1b = G1();
		final ICobaltGeography g2 = G2();
		Assert.assertTrue(g1a.equals(g1b));
		Assert.assertFalse(g1a.equals(g2));
	}
}
