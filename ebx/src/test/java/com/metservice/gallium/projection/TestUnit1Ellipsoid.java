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
public class TestUnit1Ellipsoid {

	@Test
	public void t10_wgs84() {
		final Ellipsoid oE1 = EllipsoidDictionary.findByTitle("WGS_1984");
		final Ellipsoid oE2 = EllipsoidDictionary.findByAuthority(Authority.newEPSG(7030));
		final Ellipsoid oE3 = EllipsoidDictionary.findByTitle("7030");
		Assert.assertNotNull("WGS_1984 by title", oE1);
		Assert.assertNotNull("WGS_1984 by authority", oE2);
		Assert.assertNotNull("WGS_1984 by title authority", oE3);
		Assert.assertEquals("equator r", 6_378_137.0, oE1.equatorialRadiusMetres, 1e-1);
		Assert.assertEquals("polar r", 6_356_752.314, oE1.polarRadiusMetres, 1e-3);
		Assert.assertEquals("equator r", 6_378_137.0, oE2.equatorialRadiusMetres, 1e-1);
		Assert.assertEquals("equator r", 6_378_137.0, oE3.equatorialRadiusMetres, 1e-1);
	}

	@Test
	public void t20_airy() {
		final Ellipsoid oE = EllipsoidDictionary.findByTitle("Airy_1830");
		Assert.assertNotNull("Found Airy", oE);
		Assert.assertEquals("polar r", 6_356_256.909, oE.polarRadiusMetres, 1e-2);
		Assert.assertEquals("e", 0.0816733743281685, oE.eccentricity, 1e-6);
	}
}
