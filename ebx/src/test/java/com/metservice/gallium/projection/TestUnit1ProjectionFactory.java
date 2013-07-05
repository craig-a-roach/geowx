/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
public class TestUnit1ProjectionFactory {

	private static final WktStructure Greenwich = new WktStructure("PRIMEM", "Greenwich");
	private static final WktStructure Degrees = new WktStructure("UNIT", "degrees");
	private static final WktStructure Metres = new WktStructure("UNIT", "metres");

	private static WktStructure param(String name, double value) {
		return new WktStructure("PARAMETER", name, value);
	}

	@Test
	public void t10_mercator() {
		final WktStructure spheroid = new WktStructure("SPHEROID", "Krassowski", 6378245.0, 298.3);
		final WktStructure datum = new WktStructure("DATUM", "D_Krassowski", spheroid);
		final WktStructure gcs = new WktStructure("GEOGCS", "GCS_Krassowski", datum, Greenwich, Degrees);
		final WktStructure p = new WktStructure("PROJECTION", "Mercator");
		final WktStructure[] params = { param("central_meridian", 51.0), param("standard_parallel_1", 42.0) };
		final WktStructure spec = new WktStructure("PROJCS", "Mercator Ref", gcs, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			System.out.println(pcs.toWkt().format());
			final IGalliumProjection pj = pcs.newProjection();
			final GalliumPointD pt = pj.transform(53.0, -53.0);
			Assert.assertEquals("Easting(m)", 165704.29, pt.x, 1e-2);
			Assert.assertEquals("Northing(m)", -5171848.07, pt.y, 1e-2);
			final GalliumPointD pti = pj.inverseDegrees(pt.x, pt.y);
			Assert.assertEquals("Lon", 53.0, pti.x, 1e-2);
			Assert.assertEquals("Lat", -53.0, pti.y, 1e-2);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(spec.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(spec.format());
			System.err.println(ex.getMessage());
			Assert.fail("Projection Exception: " + ex.getMessage());
		}
	}

	public void t90_transverseMercator() {
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
		final String pax = "AXIS[\"X\",EAST],AXIS[\"Y\",NORTH]";
		final String pa = "AUTHORITY[\"EPSG\",\"27700\"]";
		final String spec = "PROJCS[\"OSGB 1936 / British National Grid\"" + ",\n" + gcs + ",\n" + pj + ",\n" + pp + ",\n" + pu
				+ ",\n" + pax + ",\n" + pa + "\n]";
		System.out.println(spec);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec);
			System.out.println(pcs);
		} catch (final GalliumSyntaxException ex) {
			Assert.fail("Syntax Exception: " + ex.getMessage());
		}
	}
}
