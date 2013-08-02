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
	private static final WktStructure Kilometres = new WktStructure("UNIT", "kilometres");
	private static final WktStructure USFeet = new WktStructure("UNIT", "US feet");
	private static final WktStructure GCS_Sphere = new WktStructure("GEOGCS", "EPSG:4035");

	private static double deg(char h, int deg, int min) {
		final double sgn = (h == 'S' || h == 'W') ? -1.0 : 1.0;
		return sgn * (deg * 60.0 + min) / 60.0;
	}

	private static double deg(char h, int deg, int min, double sec) {
		final double sgn = (h == 'S' || h == 'W') ? -1.0 : 1.0;
		return sgn * (deg * 3600.0 + min * 60.0 + sec) / 3600.0;
	}

	private static WktStructure geogcs(String spheroidName, double a, double invf) {
		final WktStructure spheroid = new WktStructure("SPHEROID", spheroidName, a, invf);
		final WktStructure datum = new WktStructure("DATUM", "D_" + spheroidName, spheroid);
		final WktStructure gcs = new WktStructure("GEOGCS", "GCS_" + spheroidName, datum, Greenwich, Degrees);
		return gcs;
	}

	private static WktStructure param(String name, double value) {
		return new WktStructure("PARAMETER", name, value);
	}

	@Test
	public void t120_stereoOblique() {
		final WktStructure gcsE = geogcs("Bessel 1841", 6377397.155, 299.15281);
		final WktStructure p = new WktStructure("PROJECTION", "Oblique Stereographic");
		final WktStructure[] params = { param("central_meridian", deg('E', 5, 23, 15.5)),
				param("latitude_of_origin", deg('N', 52, 9, 22.178)), param("scaleFactor", 0.9999079),
				param("falseEasting", 155000.0), param("falseNorthing", 463000.0) };
		final WktStructure specE = new WktStructure("PROJCS", "Stereo Oblique Ref", gcsE, p, params, Metres);
		final WktStructure specS = new WktStructure("PROJCS", "Stereo Oblique Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcsE = WktCoordinateSystemFactory.newCoordinateSystemProjected(specE.format());
			final ProjectedCoordinateSystem pcsS = WktCoordinateSystemFactory.newCoordinateSystemProjected(specS.format());
			final IGalliumProjection pjE = pcsE.newProjection();
			final IGalliumProjection pjS = pcsS.newProjection();
			final double lon = 6.0;
			final double lat = 53.0;
			final GalliumPointD ptE = pjE.transform(lon, lat);
			Assert.assertEquals("Ellipse Easting(m)", 196107.26, ptE.x, 1e-2);
			Assert.assertEquals("Ellipse Northing(m)", 557059.56, ptE.y, 1e-2);
			final GalliumPointD piE = pjE.inverseDegrees(ptE.x, ptE.y);
			Assert.assertEquals("Lon", lon, piE.x, 1e-2);
			Assert.assertEquals("Lat", lat, piE.y, 1e-2);
			final GalliumPointD ptS = pjS.transform(lon, lat);
			Assert.assertEquals("Sphere Easting(m)", 195976.56, ptS.x, 1e-2);
			Assert.assertEquals("Sphere Northing(m)", 556997.63, ptS.y, 1e-2);
			final GalliumPointD piS = pjS.inverseDegrees(ptS.x, ptS.y);
			Assert.assertEquals("Lon", lon, piS.x, 1e-2);
			Assert.assertEquals("Lat", lat, piS.y, 1e-2);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(specE.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(specE.format());
			System.err.println(ex.getMessage());
			Assert.fail("Projection Exception: " + ex.getMessage());
		}
	}

	@Test
	public void t130_stereoEquator() {
		final WktStructure gcsE = geogcs("Bessel 1841", 6377397.155, 299.15281);
		final WktStructure p = new WktStructure("PROJECTION", "Stereographic");
		final WktStructure[] params = { param("central_meridian", deg('E', 170, 0)), param("scaleFactor", 0.995) };
		final WktStructure specE = new WktStructure("PROJCS", "Stereo Equator Ref", gcsE, p, params, Metres);
		final WktStructure specS = new WktStructure("PROJCS", "Stereo Equator Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcsE = WktCoordinateSystemFactory.newCoordinateSystemProjected(specE.format());
			final ProjectedCoordinateSystem pcsS = WktCoordinateSystemFactory.newCoordinateSystemProjected(specS.format());
			final IGalliumProjection pjE = pcsE.newProjection();
			final IGalliumProjection pjS = pcsS.newProjection();
			final double lon = 172.0;
			final double lat = -41.0;
			final GalliumPointD ptE = pjE.transform(lon, lat);
			Assert.assertEquals("Ellipse Easting(m)", 190859.69, ptE.x, 1e-2);
			Assert.assertEquals("Ellipse Northing(m)", -4722273.40, ptE.y, 1e-2);
			final GalliumPointD piE = pjE.inverseDegrees(ptE.x, ptE.y);
			Assert.assertEquals("Lon", lon, piE.x, 1e-2);
			Assert.assertEquals("Lat", lat, piE.y, 1e-2);
			final GalliumPointD ptS = pjS.transform(lon, lat);
			Assert.assertEquals("Sphere Easting(m)", 190356.74, ptS.x, 1e-2);
			Assert.assertEquals("Sphere Northing(m)", -4741460.70, ptS.y, 1e-2);
			final GalliumPointD piS = pjS.inverseDegrees(ptS.x, ptS.y);
			Assert.assertEquals("Lon", lon, piS.x, 1e-2);
			Assert.assertEquals("Lat", lat, piS.y, 1e-2);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(specE.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(specE.format());
			System.err.println(ex.getMessage());
			Assert.fail("Projection Exception: " + ex.getMessage());
		}
	}

	@Test
	public void t140_stereoEquator_Fail() {
		final WktStructure gcsE = geogcs("Bessel 1841", 6377397.155, 299.15281);
		final WktStructure p = new WktStructure("PROJECTION", "Stereographic");
		final WktStructure[] params = { param("central_meridian", deg('E', 170, 0)), param("scaleFactor", 0.995) };
		final WktStructure specE = new WktStructure("PROJCS", "Stereo Equator Ref", gcsE, p, params, Metres);
		final WktStructure specS = new WktStructure("PROJCS", "Stereo Equator Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcsE = WktCoordinateSystemFactory.newCoordinateSystemProjected(specE.format());
			final ProjectedCoordinateSystem pcsS = WktCoordinateSystemFactory.newCoordinateSystemProjected(specS.format());
			try {
				final IGalliumProjection pjE = pcsE.newProjection();
				pjE.transform(-10.0, 0.0);
				Assert.assertTrue("Expecting failure: Outside bounds", false);
			} catch (final GalliumProjectionException ex) {
				System.out.println(specE.format());
				System.out.println("Good exception: " + ex.getMessage());
				Assert.assertTrue("Outside bounds", true);
			}
			try {
				final IGalliumProjection pjS = pcsS.newProjection();
				pjS.transform(-10.0, 0.0);
			} catch (final GalliumProjectionException ex) {
				System.out.println(specS.format());
				System.out.println("Good exception: " + ex.getMessage());
				Assert.assertTrue("Outside bounds", true);
			}
		} catch (final GalliumSyntaxException ex) {
			System.out.println(specE.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		}
	}

	@Test
	public void t150_stereoPolarNorth() {
		final WktStructure gcsE = geogcs("Bessel 1841", 6377397.155, 299.15281);
		final WktStructure p = new WktStructure("PROJECTION", "Stereographic North Pole");
		final WktStructure[] params = { param("central_meridian", deg('W', 96, 0)),
				param("standard_parallel_1", deg('N', 71, 0)) };
		final WktStructure specE = new WktStructure("PROJCS", "Stereo NPolar Ref", gcsE, p, params, Metres);
		final WktStructure specS = new WktStructure("PROJCS", "Stereo NPolar Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcsE = WktCoordinateSystemFactory.newCoordinateSystemProjected(specE.format());
			final ProjectedCoordinateSystem pcsS = WktCoordinateSystemFactory.newCoordinateSystemProjected(specS.format());
			final IGalliumProjection pjE = pcsE.newProjection();
			final IGalliumProjection pjS = pcsS.newProjection();
			final double lon = deg('W', 121, 20, 22.38);
			final double lat = deg('N', 39, 6, 4.508);
			final GalliumPointD ptE = pjE.transform(lon, lat);
			Assert.assertEquals("Ellipse Easting(m)", -2529269.89, ptE.x, 1e-2);
			Assert.assertEquals("Ellipse Northing(m)", -5341166.25, ptE.y, 1e-2);
			final GalliumPointD piE = pjE.inverseDegrees(ptE.x, ptE.y);
			Assert.assertEquals("Lon", lon, piE.x, 1e-2);
			Assert.assertEquals("Lat", lat, piE.y, 1e-2);
			final GalliumPointD ptS = pjS.transform(lon, lat);
			Assert.assertEquals("Sphere Easting(m)", -2524504.51, ptS.x, 1e-2);
			Assert.assertEquals("Sphere Northing(m)", -5331103.00, ptS.y, 1e-2);
			final GalliumPointD piS = pjS.inverseDegrees(ptS.x, ptS.y);
			Assert.assertEquals("Lon", lon, piS.x, 1e-2);
			Assert.assertEquals("Lat", lat, piS.y, 1e-2);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(specE.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(specE.format());
			System.err.println(ex.getMessage());
			Assert.fail("Projection Exception: " + ex.getMessage());
		}
	}

	@Test
	public void t160_stereoPolarSouth() {
		final WktStructure gcsE = geogcs("Bessel 1841", 6377397.155, 299.15281);
		final WktStructure p = new WktStructure("PROJECTION", "Stereographic South Pole");
		final WktStructure[] params = { param("central_meridian", deg('E', 96, 0)),
				param("standard_parallel_1", deg('S', 71, 0)) };
		final WktStructure specE = new WktStructure("PROJCS", "Stereo SPolar Ref", gcsE, p, params, Metres);
		final WktStructure specS = new WktStructure("PROJCS", "Stereo SPolar Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcsE = WktCoordinateSystemFactory.newCoordinateSystemProjected(specE.format());
			final ProjectedCoordinateSystem pcsS = WktCoordinateSystemFactory.newCoordinateSystemProjected(specS.format());
			final IGalliumProjection pjE = pcsE.newProjection();
			final IGalliumProjection pjS = pcsS.newProjection();
			final double lon = deg('E', 121, 20, 22.38);
			final double lat = deg('S', 39, 6, 4.508);
			final GalliumPointD ptE = pjE.transform(lon, lat);
			Assert.assertEquals("Ellipse Easting(m)", 2529269.89, ptE.x, 1e-2);
			Assert.assertEquals("Ellipse Northing(m)", 5341166.25, ptE.y, 1e-2);
			final GalliumPointD piE = pjE.inverseDegrees(ptE.x, ptE.y);
			Assert.assertEquals("Lon", lon, piE.x, 1e-2);
			Assert.assertEquals("Lat", lat, piE.y, 1e-2);
			final GalliumPointD ptS = pjS.transform(lon, lat);
			Assert.assertEquals("Sphere Easting(m)", 2524504.51, ptS.x, 1e-2);
			Assert.assertEquals("Sphere Northing(m)", 5331103.00, ptS.y, 1e-2);
			final GalliumPointD piS = pjS.inverseDegrees(ptS.x, ptS.y);
			Assert.assertEquals("Lon", lon, piS.x, 1e-2);
			Assert.assertEquals("Lat", lat, piS.y, 1e-2);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(specE.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(specE.format());
			System.err.println(ex.getMessage());
			Assert.fail("Projection Exception: " + ex.getMessage());
		}
	}

	@Test
	public void t210_lambertConformalConical2SP_Clarke() {
		final WktStructure gcs = geogcs("Clarke_1866", 6378206.4, 294.9787);
		final WktStructure p = new WktStructure("PROJECTION", "EPSG:9802");
		final WktStructure[] params = { param("falseEasting", 2000000.0), param("standardParallel1", deg('N', 28, 23)),
				param("standardParallel2", deg('N', 30, 17)), param("latitudeOfOrigin", deg('N', 27, 50)),
				param("longitudeOfCenter", deg('W', 99, 0)) };
		final WktStructure spec = new WktStructure("PROJCS", "Lambert Conical Conformal Ref", gcs, p, params, USFeet);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			System.out.println(pcs.toWkt().format());
			final IGalliumProjection pj = pcs.newProjection();
			final double lon = deg('W', 96, 0);
			final double lat = deg('N', 28, 30);
			final GalliumPointD pt = pj.transform(lon, lat);
			Assert.assertEquals("Easting(us-ft)", 2963503.91, pt.x, 1e-2);
			Assert.assertEquals("Northing(us-ft)", 254759.80, pt.y, 1e-2);
			final GalliumPointD pi = pj.inverseDegrees(pt.x, pt.y);
			Assert.assertEquals("Lon", lon, pi.x, 1e-2);
			Assert.assertEquals("Lat", lat, pi.y, 1e-2);
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

	@Test
	public void t220_lambertConformalConical2SP_Sphere() {
		final WktStructure p = new WktStructure("PROJECTION", "EPSG:9802");
		final WktStructure[] params = { param("falseEasting", 2000000.0), param("standardParallel1", deg('N', 28, 23)),
				param("standardParallel2", deg('N', 30, 17)), param("latitudeOfOrigin", deg('N', 27, 50)),
				param("longitudeOfCenter", deg('W', 99, 0)) };
		final WktStructure spec = new WktStructure("PROJCS", "Lambert Conical Conformal Ref", GCS_Sphere, p, params, USFeet);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			final IGalliumProjection pj = pcs.newProjection();
			final double lon = deg('W', 96, 0);
			final double lat = deg('N', 28, 30);
			final GalliumPointD pt = pj.transform(lon, lat);
			Assert.assertEquals("Easting(us-ft)", 2961673.0, pt.x, 1.0);
			Assert.assertEquals("Northing(us-ft)", 255561.0, pt.y, 1.0);
			final GalliumPointD pi = pj.inverseDegrees(pt.x, pt.y);
			Assert.assertEquals("Lon", lon, pi.x, 1e-2);
			Assert.assertEquals("Lat", lat, pi.y, 1e-2);
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

	@Test
	public void t230_lambertConformalConical1SP_Clarke() {
		final WktStructure gcs = geogcs("Clarke_1866", 6378206.4, 294.9787);
		final WktStructure p = new WktStructure("PROJECTION", "EPSG:9801");
		final WktStructure[] params = { param("falseEasting", 250000.0), param("falseNorthing", 150000.0),
				param("latitudeOfOrigin", deg('N', 18, 0)), param("longitudeOfCenter", deg('W', 77, 0)) };
		final WktStructure spec = new WktStructure("PROJCS", "Lambert Conical Conformal Ref", gcs, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			System.out.println(pcs.toWkt().format());
			final IGalliumProjection pj = pcs.newProjection();
			final double lon = deg('W', 76, 56, 37.26);
			final double lat = deg('N', 17, 55, 55.8);
			final GalliumPointD pt = pj.transform(lon, lat);
			Assert.assertEquals("Easting(m)", 255966.58, pt.x, 1e-2);
			Assert.assertEquals("Northing(m)", 142493.51, pt.y, 1e-2);
			final GalliumPointD pi = pj.inverseDegrees(pt.x, pt.y);
			Assert.assertEquals("Lon", lon, pi.x, 1e-2);
			Assert.assertEquals("Lat", lat, pi.y, 1e-2);
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

	@Test
	public void t240_lambertConformalConical1SP_Sphere() {
		final WktStructure p = new WktStructure("PROJECTION", "EPSG:9801");
		final WktStructure[] params = { param("falseEasting", 250000.0), param("falseNorthing", 150000.0),
				param("latitudeOfOrigin", deg('N', 18, 0)), param("longitudeOfCenter", deg('W', 77, 0)) };
		final WktStructure spec = new WktStructure("PROJCS", "Lambert Conical Conformal Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			final IGalliumProjection pj = pcs.newProjection();
			final double lon = deg('W', 76, 56, 37.26);
			final double lat = deg('N', 17, 55, 55.8);
			final GalliumPointD pt = pj.transform(lon, lat);
			Assert.assertEquals("Easting(m)", 255958.0, pt.x, 1.0);
			Assert.assertEquals("Northing(m)", 142458.0, pt.y, 1.0);
			final GalliumPointD pi = pj.inverseDegrees(pt.x, pt.y);
			Assert.assertEquals("Lon", lon, pi.x, 1e-2);
			Assert.assertEquals("Lat", lat, pi.y, 1e-2);
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

	@Test
	public void t290_lambertConformalConical2SP_Fail() {
		final WktStructure gcs = geogcs("Clarke_1866", 6378206.4, 294.9787);
		final WktStructure p = new WktStructure("PROJECTION", "EPSG:9802");
		final WktStructure[] params = { param("falseEasting", 2000000.0), param("standardParallel1", deg('N', 28, 15)),
				param("standardParallel2", deg('S', 28, 15)), param("latitudeOfOrigin", deg('N', 27, 50)),
				param("longitudeOfCenter", deg('W', 99, 0)) };
		final WktStructure spec = new WktStructure("PROJCS", "Lambert Conical Conformal Ref", gcs, p, params, USFeet);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			pcs.newProjection();
			Assert.assertTrue("Expecting failure: Antipodean standard parallel", false);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(spec.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(spec.format());
			System.out.println("Good exception: " + ex.getMessage());
			Assert.assertTrue("Antipodean standard parallel", true);
		}
	}

	@Test
	public void t300_mercator() {
		final WktStructure gcs = geogcs("Krassowski", 6378245.0, 298.3);
		final WktStructure p = new WktStructure("PROJECTION", "Mercator");
		final WktStructure[] params = { param("central_meridian", 51.0), param("standard_parallel_1", 42.0) };
		final WktStructure specM = new WktStructure("PROJCS", "Mercator Ref", gcs, p, params, Metres);
		final WktStructure specK = new WktStructure("PROJCS", "Mercator Ref", gcs, p, params, Kilometres);
		final WktStructure specS = new WktStructure("PROJCS", "Mercator Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcsM = WktCoordinateSystemFactory.newCoordinateSystemProjected(specM.format());
			System.out.println(pcsM.toWkt().format());
			final ProjectedCoordinateSystem pcsK = WktCoordinateSystemFactory.newCoordinateSystemProjected(specK.format());
			final ProjectedCoordinateSystem pcsS = WktCoordinateSystemFactory.newCoordinateSystemProjected(specS.format());
			final IGalliumProjection pjM = pcsM.newProjection();
			final IGalliumProjection pjK = pcsK.newProjection();
			final IGalliumProjection pjS = pcsS.newProjection();
			final GalliumPointD ptM = pjM.transform(53.0, -53);
			final GalliumPointD ptK = pjK.transform(53.0, -53.0);
			final GalliumPointD ptS = pjS.transform(53.0, -53.0);
			Assert.assertEquals("Easting(m)", 165704.29, ptM.x, 1e-2);
			Assert.assertEquals("Northing(m)", -5171848.07, ptM.y, 1e-2);
			Assert.assertEquals("Easting(km)", 165.70429, ptK.x, 1e-2);
			Assert.assertEquals("Northing(km)", -5171.84807, ptK.y, 1e-2);
			Assert.assertEquals("Easting(m)", 165267.87, ptS.x, 1e-2);
			Assert.assertEquals("Northing(m)", -5183571.97, ptS.y, 1e-2);
			final GalliumPointD piM = pjM.inverseDegrees(ptM.x, ptM.y);
			final GalliumPointD piK = pjK.inverseDegrees(ptK.x, ptK.y);
			final GalliumPointD piS = pjS.inverseDegrees(ptS.x, ptS.y);
			Assert.assertEquals("Lon", 53.0, piM.x, 1e-2);
			Assert.assertEquals("Lat", -53.0, piM.y, 1e-2);
			Assert.assertEquals("Lon", 53.0, piK.x, 1e-2);
			Assert.assertEquals("Lat", -53.0, piK.y, 1e-2);
			Assert.assertEquals("Lon", 53.0, piS.x, 1e-2);
			Assert.assertEquals("Lat", -53.0, piS.y, 1e-2);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(specM.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(specM.format());
			System.err.println(ex.getMessage());
			Assert.fail("Projection Exception: " + ex.getMessage());
		}
	}

	@Test
	public void t310_mercator_Fail() {
		final WktStructure p = new WktStructure("PROJECTION", "Mercator");
		final WktStructure[] params = { param("central_meridian", 51.0), param("standard_parallel_1", 42.0) };
		final WktStructure spec = new WktStructure("PROJCS", "Mercator Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			final IGalliumProjection pj = pcs.newProjection();
			pj.transform(0.0, -87.0);
			Assert.assertTrue("Expecting failure: Bounds", false);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(spec.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(spec.format());
			System.out.println("Good exception: " + ex.getMessage());
			Assert.assertTrue("Bounds", true);
		}
	}

	@Test
	public void t400_transverseMercator_OSGB() {
		final String spheroid = "  SPHEROID[\"Airy 1830\",6377563.396,299.3249646,AUTHORITY[\"EPSG\",\"7001\"]]";
		final String datum = " DATUM[\"OSGB_1936\",\n" + spheroid
				+ ",TOWGS84[375,-111,431,0,0,0,0],AUTHORITY[\"EPSG\",\"6277\"]]";
		final String pm = " PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]]";
		final String gu = " UNIT[\"DMSH\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9108\"]]";
		final String gax = " AXIS[\"Lat\",NORTH],AXIS[\"Long\",EAST]";
		final String ga = " AUTHORITY[\"EPSG\",\"4277\"]";
		final String gcs = "GEOGCS[\"OSGB 1936\"" + ",\n" + datum + ",\n" + pm + ",\n" + gu + ",\n" + gax + ",\n" + ga + "\n]";

		final String prj = "PROJECTION[\"Transverse_Mercator\"]";
		final String pp = "PARAMETER[\"latitude_of_origin\",49],PARAMETER[\"central_meridian\",-2],PARAMETER[\"scale_factor\",0.999601272],PARAMETER[\"false_easting\",400000],PARAMETER[\"false_northing\",-100000]";
		final String pu = "UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]]";
		final String pax = "AXIS[\"X\",EAST],AXIS[\"Y\",NORTH]";
		final String pa = "AUTHORITY[\"EPSG\",\"27700\"]";
		final String spec = "PROJCS[\"OSGB 1936 / British National Grid\"" + ",\n" + gcs + ",\n" + prj + ",\n" + pp + ",\n"
				+ pu + ",\n" + pax + ",\n" + pa + "\n]";
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec);
			System.out.println(pcs.toWkt().format());
			final IGalliumProjection pj = pcs.newProjection();
			final GalliumPointD pt = pj.transform(0.5, 50.5);
			Assert.assertEquals("Easting(m)", 577274.98, pt.x, 1e-2);
			Assert.assertEquals("Northing(m)", 69740.49, pt.y, 1e-2);
			final GalliumPointD pi = pj.inverseDegrees(pt.x, pt.y);
			Assert.assertEquals("Lon", 0.5, pi.x, 1e-2);
			Assert.assertEquals("Lat", 50.5, pi.y, 1e-2);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(spec);
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(spec);
			System.err.println(ex.getMessage());
			Assert.fail("Projection Exception: " + ex.getMessage());
		}
	}

	@Test
	public void t410_transverseMercator_SphereEquator() {
		final WktStructure p = new WktStructure("PROJECTION", "Transverse Mercator");
		final WktStructure[] params = { param("latitudeOfOrigin", deg('N', 0, 0)), param("centralMeridian", deg('W', 2, 0)),
				param("scaleFactor", 0.999601272) };
		final WktStructure spec = new WktStructure("PROJCS", "Transverse Mercator Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			final IGalliumProjection pj = pcs.newProjection();
			final GalliumPointD ptN = pj.transform(0.5, 50.5);
			final GalliumPointD ptS = pj.transform(0.5, -50.5);
			Assert.assertEquals("N Easting(m)", 176740.46, ptN.x, 1e-2);
			Assert.assertEquals("N Northing(m)", 5616080.95, ptN.y, 1e-2);
			Assert.assertEquals("S Easting(m)", 176740.46, ptS.x, 1e-2);
			Assert.assertEquals("S Northing(m)", -5616080.95, ptS.y, 1e-2);
			final GalliumPointD piN = pj.inverseDegrees(ptN.x, ptN.y);
			final GalliumPointD piS = pj.inverseDegrees(ptS.x, ptS.y);
			Assert.assertEquals("Lon", 0.5, piN.x, 1e-2);
			Assert.assertEquals("Lat", 50.5, piN.y, 1e-2);
			Assert.assertEquals("Lon", 0.5, piS.x, 1e-2);
			Assert.assertEquals("Lat", -50.5, piS.y, 1e-2);
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

	@Test
	public void t420_transverseMercator_SphereGB() {
		final WktStructure p = new WktStructure("PROJECTION", "Transverse Mercator");
		final WktStructure[] params = { param("latitudeOfOrigin", deg('N', 49, 0)), param("centralMeridian", deg('W', 2, 0)),
				param("scaleFactor", 0.999601272), param("falseEasting", 400000), param("falseNorthing", -100000) };
		final WktStructure spec = new WktStructure("PROJCS", "Transverse Mercator Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			final IGalliumProjection pj = pcs.newProjection();
			final GalliumPointD pt = pj.transform(0.5, 50.5);
			Assert.assertEquals("Easting(m)", 576740.46, pt.x, 1e-2);
			Assert.assertEquals("Northing(m)", 69702.03, pt.y, 1e-2);
			final GalliumPointD pi = pj.inverseDegrees(pt.x, pt.y);
			Assert.assertEquals("Lon", 0.5, pi.x, 1e-2);
			Assert.assertEquals("Lat", 50.5, pi.y, 1e-2);
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

	@Test
	public void t430_transverseMercator_Fail() {
		final WktStructure p = new WktStructure("PROJECTION", "Transverse Mercator");
		final WktStructure[] params = { param("latitudeOfOrigin", deg('N', 49, 0)), param("centralMeridian", deg('W', 2, 0)),
				param("scaleFactor", 0.999601272), param("falseEasting", 400000), param("falseNorthing", -100000) };
		final WktStructure spec = new WktStructure("PROJCS", "Transverse Mercator Ref", GCS_Sphere, p, params, Metres);
		try {
			final ProjectedCoordinateSystem pcs = WktCoordinateSystemFactory.newCoordinateSystemProjected(spec.format());
			final IGalliumProjection pj = pcs.newProjection();
			pj.transform(88, 50.5);
			Assert.assertTrue("Expecting failure: Bounds", false);
		} catch (final GalliumSyntaxException ex) {
			System.out.println(spec.format());
			System.err.println(ex.getMessage());
			Assert.fail("Syntax Exception: " + ex.getMessage());
		} catch (final GalliumProjectionException ex) {
			System.out.println(spec.format());
			System.out.println("Good exception: " + ex.getMessage());
			Assert.assertTrue("Bounds", true);
		}
	}
}
