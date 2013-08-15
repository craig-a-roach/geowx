/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.gallium.esri.Authority;
import com.metservice.gallium.esri.Datum;
import com.metservice.gallium.esri.DatumDictionary;

/**
 * @author roach
 */
public class TestUnit1Datum {

	@Test
	public void t10_wgs84() {
		final Datum oD = DatumDictionary.findByTitle("D_WGS_1984");
		Assert.assertNotNull("D_WGS_1984", oD);
		Assert.assertTrue("EPSG:7030", oD.ellipsoid.oAuthority.equals(Authority.newEPSG(7030)));
		final String wkt = "DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.25722014342756,AUTHORITY[\"EPSG\",\"7030\"]],TOWGS84[0.0,0.0,0.0],AUTHORITY[\"EPSG\",\"6326\"]]";
		Assert.assertEquals("WKT", wkt, oD.toWkt().formatFlat());
	}

	@Test
	public void t20_airy() {
		final Datum oD = DatumDictionary.findByAuthority(Authority.newEPSG(6277));
		Assert.assertNotNull("D_OSGB_1936", oD);
		Assert.assertEquals("polar r", 6_356_256.909, oD.ellipsoid.polarRadiusMetres, 1e-2);
		Assert.assertNotNull("D_OSGB_1936 to WGS84", oD.oToWgs84);
	}

	@Test
	public void t30_nzgd() {
		final Datum oD = DatumDictionary.findByAuthority(Authority.newEPSG(6167));
		Assert.assertNotNull("D_NZGD_2000", oD);
	}

}
