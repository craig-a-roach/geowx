/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class Ellipsoid implements IWktEmit {

	public static final Ellipsoid Sphere = newSphereEpsg(7035, "Sphere", 6_371_000.0);
	public static final Ellipsoid WGS_1984 = newMinorEpsg(7030, "WGS_1984", 6_378_137.000, 6_356_752.314);
	public static final Ellipsoid GRS_1980 = newMinorEpsg(7019, "GRS_1980", 6_378_137.000, 6_356_752.314);

	private static final double SPHERE_ULP = 0.001;

	private static double eccentricity2(double er, double pr) {
		return 1.0 - ((pr * pr) / (er * er));
	}

	private static double inverseFlattening(double er, double pr) {
		final double diff = er - pr;
		return (diff < SPHERE_ULP) ? 0.0 : (er / diff);
	}

	public static Ellipsoid newInverseFlattening(String title, double semiMajorMetres, double inverseFlattening,
			Authority oAuthority) {
		final double f = inverseFlattening < 0.5 ? 0.0 : (1.0 / inverseFlattening);
		final double semiMinorMetres = semiMajorMetres * (1.0 - f);
		return new Ellipsoid(oAuthority, Title.newInstance(title), semiMajorMetres, semiMinorMetres);
	}

	public static Ellipsoid newMinorEpsg(int code, String title, double semiMajorMetres, double semiMinorMetres) {
		return new Ellipsoid(Authority.newEPSG(code), Title.newInstance(title), semiMajorMetres, semiMinorMetres);
	}

	public static Ellipsoid newSphereEpsg(int code, String title, double radiusMetres) {
		return new Ellipsoid(Authority.newEPSG(code), Title.newInstance(title), radiusMetres);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("authority", oAuthority);
		ds.a("title", title);
		if (isSpherical) {
			ds.a("r(m)", equatorialRadiusMetres);
		} else {
			ds.a("equatorial r(m)", equatorialRadiusMetres);
			ds.a("polar r(m)", polarRadiusMetres);
			ds.a("eccentricity", eccentricity);
			ds.a("inverseFlattening", inverseFlattening);
		}
		return ds.ss();
	}

	@Override
	public WktStructure toWkt() {
		return new WktStructure("SPHEROID", title, equatorialRadiusMetres);
	}

	private Ellipsoid(Authority oAuthority, Title title, double radiusMetres) {
		assert title != null;
		this.oAuthority = oAuthority;
		this.title = title;
		this.isSpherical = true;
		this.equatorialRadiusMetres = radiusMetres;
		this.polarRadiusMetres = radiusMetres;
		this.eccentricity = 0.0;
		this.eccentricity2 = 0.0;
		this.inverseFlattening = 0.0;
	}

	private Ellipsoid(Authority oAuthority, Title title, double equatorialRadiusMetres, double polarRadiusMetres) {
		assert title != null;
		this.oAuthority = oAuthority;
		this.title = title;
		this.isSpherical = false;
		this.equatorialRadiusMetres = equatorialRadiusMetres;
		this.polarRadiusMetres = polarRadiusMetres;
		final double e2 = eccentricity2(equatorialRadiusMetres, polarRadiusMetres);
		this.eccentricity = Math.sqrt(e2);
		this.eccentricity2 = e2;
		this.inverseFlattening = inverseFlattening(equatorialRadiusMetres, polarRadiusMetres);
	}
	public final Authority oAuthority;
	public final Title title;
	public boolean isSpherical;
	public final double equatorialRadiusMetres;
	public final double polarRadiusMetres;
	public final double eccentricity;
	public final double eccentricity2;
	public final double inverseFlattening;
}
