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
public class TestUnit1WktFactory {

	@Test
	public void t05_pcs() {
		final String spheroid = "  SPHEROID[\"Airy 1830\",6377563.396,299.3249646,AUTHORITY[\"EPSG\",\"7001\"]]";
		final String datum = " DATUM[\"OSGB_1936\",\n" + spheroid
				+ ",TOWGS84[375,-111,431,0,0,0,0],AUTHORITY[\"EPSG\",\"6277\"]]";
		final String pm = " PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]]";
		final String gu = " UNIT[\"DMSH\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9108\"]]";
		final String gax = " AXIS[\"Lat\",NORTH],AXIS[\"Long\",EAST]";
		final String ga = " AUTHORITY[\"EPSG\",\"4277\"]";
		final String gcs = "GEOGCS[\"OSGB 1936\"" + ",\n" + datum + ",\n" + pm + ",\n" + gu + ",\n" + gax + ",\n" + ga + "\n]";

		final String pj = "PROJECTION[\"Transverse_Mercator\"]";
		final String pp = "PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.999601272],PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000]";
		final String pu = "UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]]";
		final String pax = "AXIS[\"E\",EAST],AXIS[\"N\",NORTH]";
		final String pa = "AUTHORITY[\"EPSG\",\"27700\"]";
		final String pcs = "PROJCS[\"OSGB 1936 / British National Grid\"" + ",\n" + gcs + ",\n" + pj + ",\n" + pp + ",\n" + pu
				+ ",\n" + pax + ",\n" + pa + "\n]";
		System.out.println(pcs);
		try {
			final IGalliumCoordinateSystem cs = GalliumCoordinateSystemFactory.newCoordinateSystemFromWKT(pcs);
			System.out.println(cs);
		} catch (final GalliumSyntaxException ex) {
			Assert.fail("Syntax Exception: " + ex.getMessage());
		}
	}

	// @Test
	public void t10_gcs() {
		final String spec = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295],AXIS[\"Lat\",NORTH],AXIS[\"Long\",EAST]]";
		try {
			final IGalliumCoordinateSystem cs = GalliumCoordinateSystemFactory.newCoordinateSystemFromWKT(spec);
			System.out.println(cs);
		} catch (final GalliumSyntaxException ex) {
			Assert.fail("Syntax Exception: " + ex.getMessage());
		}
	}

	// @Test
	public void t20_gcs() {
		final String spec = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\"],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]";
		try {
			final IGalliumCoordinateSystem cs = GalliumCoordinateSystemFactory.newCoordinateSystemFromWKT(spec);
			System.out.println(cs);
		} catch (final GalliumSyntaxException ex) {
			Assert.fail("Syntax Exception: " + ex.getMessage());
		}
	}
}
