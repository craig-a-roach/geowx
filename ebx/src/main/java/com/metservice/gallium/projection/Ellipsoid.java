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
class Ellipsoid {

	public static final Ellipsoid Sphere = newSphereEpsg(7035, "Sphere", 6_371_000.0);
	public static final Ellipsoid WGS_1984 = newMinorEpsg(7030, "WGS_1984", 6_378_137.000, 6_356_752.314);
	public static final Ellipsoid GRS_1980 = newMinorEpsg(7019, "GRS_1980", 6_378_137.000, 6_356_752.314);

	private static double eccentricity2(double er, double pr) {
		return 1.0 - ((pr * pr) / (er * er));
	}

	public static Ellipsoid newInverseFlattening(String title, double semiMajorMetres, double inverseFlattening,
			Authority oAuthority) {
		final double f = 1.0 / Math.max(1.0, inverseFlattening);
		final double semiMinorMetres = semiMajorMetres * (1.0 - f);
		return new Ellipsoid(oAuthority, Title.newInstance(title), semiMajorMetres, semiMinorMetres);
	}

	public static Ellipsoid newMinorEpsg(int code, String title, double semiMajorMetres, double semiMinorMetres) {
		return new Ellipsoid(Authority.newEPSG(code), Title.newInstance(title), semiMajorMetres, semiMinorMetres);
	}

	public static Ellipsoid newSphereEpsg(int code, String title, double radiusMetres) {
		return newMinorEpsg(code, title, radiusMetres, radiusMetres);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("authority", oAuthority);
		ds.a("title", title);
		ds.a("equatorial r(m)", equatorialRadiusMetres);
		ds.a("polar r(m)", polarRadiusMetres);
		ds.a("eccentricity", eccentricity);
		return ds.ss();
	}

	private Ellipsoid(Authority oAuthority, Title title, double equatorialRadiusMetres, double polarRadiusMetres) {
		assert title != null;
		this.oAuthority = oAuthority;
		this.title = title;
		this.equatorialRadiusMetres = equatorialRadiusMetres;
		this.polarRadiusMetres = polarRadiusMetres;
		final double e2 = eccentricity2(equatorialRadiusMetres, polarRadiusMetres);
		this.eccentricity = Math.sqrt(e2);
		this.eccentricity2 = e2;
	}
	public final Authority oAuthority;
	public final Title title;
	public final double equatorialRadiusMetres;
	public final double polarRadiusMetres;
	public final double eccentricity;
	public final double eccentricity2;
}
