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
public class TestUnit1GcsFactory {

	@Test
	public void t10_gcs() {
		final String spec = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295],AXIS[\"Lat\",NORTH],AXIS[\"Long\",EAST]]";
		try {
			final GeographicCoordinateSystem gcs = WktCoordinateSystemFactory.newCoordinateSystemGeographic(spec);
			System.out.println(gcs);
		} catch (final GalliumSyntaxException ex) {
			Assert.fail("Syntax Exception: " + ex.getMessage());
		}
	}

	@Test
	public void t20_gcs() {
		final String spec = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\"],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]";
		try {
			final GeographicCoordinateSystem gcs = WktCoordinateSystemFactory.newCoordinateSystemGeographic(spec);
			System.out.println(gcs);
		} catch (final GalliumSyntaxException ex) {
			Assert.fail("Syntax Exception: " + ex.getMessage());
		}
	}
}
