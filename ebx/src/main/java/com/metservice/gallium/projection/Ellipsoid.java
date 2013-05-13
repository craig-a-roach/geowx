/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class Ellipsoid {

	public static final Ellipsoid SPHERE = newSphere("sphere", 6_371_000.0, "Sphere");
	public static final Ellipsoid WGS84 = newInverseFlattening("WGS84", 6_378_137.0, 298.257223563, "WGS_1984");

	private static double eccentricity2(double er, double pr) {
		return 1.0 - ((pr * pr) / (er * er));
	}

	public static Ellipsoid newInverseFlattening(DualName name, double semiMajorMetres, double inverseFlattening,
			Authority oAuthority) {
		final double f = 1.0 / Math.max(1.0, inverseFlattening);
		final double semiMinorMetres = semiMajorMetres * (1.0 - f);
		return new Ellipsoid(name, semiMajorMetres, semiMinorMetres, oAuthority);
	}

	public static Ellipsoid newInverseFlattening(String fname, double semiMajorMetres, double inverseFlattening,
			Authority oAuthority) {
		final DualName name = DualName.newInstance(fname);
		return newInverseFlattening(name, semiMajorMetres, inverseFlattening, oAuthority);
	}

	public static Ellipsoid newInverseFlattening(String sname, double semiMajorMetres, double inverseFlattening, String fname) {
		final DualName name = DualName.newInstance(fname, sname);
		return newInverseFlattening(name, semiMajorMetres, inverseFlattening, null);
	}

	public static Ellipsoid newMinor(DualName name, double semiMajorMetres, double semiMinorMetres, Authority oAuthority) {
		return new Ellipsoid(name, semiMajorMetres, semiMinorMetres, oAuthority);
	}

	public static Ellipsoid newMinor(String sname, double semiMajorMetres, double semiMinorMetres, String fname) {
		final DualName name = DualName.newInstance(fname, sname);
		return newMinor(name, semiMajorMetres, semiMinorMetres, null);
	}

	public static Ellipsoid newSphere(String sname, double radiusMetres, String fname) {
		final DualName name = DualName.newInstance(fname, sname);
		return newMinor(name, radiusMetres, radiusMetres, null);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" equatorial r(m) ").append(equatorialRadiusMetres);
		sb.append(" polar r(m) ").append(polarRadiusMetres);
		if (oAuthority != null) {
			sb.append(" authority ").append(oAuthority);
		}
		return sb.toString();
	}

	private Ellipsoid(DualName name, double equatorialRadiusMetres, double polarRadiusMetres, Authority oAuthority) {
		assert name != null;
		this.name = name;
		this.equatorialRadiusMetres = equatorialRadiusMetres;
		this.polarRadiusMetres = polarRadiusMetres;
		final double e2 = eccentricity2(equatorialRadiusMetres, polarRadiusMetres);
		this.eccentricity = Math.sqrt(e2);
		this.eccentricity2 = e2;
		this.oAuthority = oAuthority;
	}
	public final DualName name;
	public final double equatorialRadiusMetres;
	public final double polarRadiusMetres;
	public final double eccentricity;
	public final double eccentricity2;
	public final Authority oAuthority;
}
