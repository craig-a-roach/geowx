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
		final Datum oD = DatumDictionary.findByName("WGS84");
		Assert.assertNotNull("WGS84", oD);
		Assert.assertEquals("equator r", 6_378_137.0, oD.ellipsoid.equatorialRadiusMetres, 1e-1);
		Assert.assertEquals("polar r", 6_356_752.3142, oD.ellipsoid.polarRadiusMetres, 1e-3);
	}

	@Test
	public void t20_airy() {
		final Ellipsoid oE = EllipsoidDictionary.findByName("Airy 1830");
		Assert.assertNotNull("Found Airy", oE);
		Assert.assertEquals("polar r", 6_356_256.910, oE.polarRadiusMetres, 1e-2);
		Assert.assertEquals("e", 0.08167337241474341, oE.eccentricity, 1e-6);
		final Datum oD = DatumDictionary.findByName("Ordnance Survey 1936");
		Assert.assertNotNull("Ordnance Survey 1936", oD);
		Assert.assertEquals("polar r", 6_356_256.910, oD.ellipsoid.polarRadiusMetres, 1e-2);
		Assert.assertEquals("deltaZ", 431.0, oD.deltaZ, 1e-2);
	}

	@Test
	public void t30_euro() {
		final Datum oD = DatumDictionary.findByName("European Datum 1979");
		Assert.assertNotNull("European Datum 1979", oD);
		Assert.assertEquals("polar r", 6_356_772.2, oD.ellipsoid.polarRadiusMetres, 1e-1);
	}

}
