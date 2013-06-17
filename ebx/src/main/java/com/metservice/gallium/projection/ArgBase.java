/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class ArgBase {

	public ArgBase(ParameterMap pmapDefault, ParameterMap pmap, GeographicCoordinateSystem gcs, Unit lu)
			throws GalliumProjectionException {
		if (pmapDefault == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");
		if (gcs == null) throw new IllegalArgumentException("object is null");
		if (lu == null) throw new IllegalArgumentException("object is null");
		final Ellipsoid elp = gcs.datum.ellipsoid;
		this.ellipsoid = elp;
		this.projectedUnit = lu;
		this.spherical = elp.isSpherical;
		this.totalScale = lu.fromBase(elp.equatorialRadiusMetres);
		this.e = elp.eccentricity;
		this.es = elp.eccentricity2;
		this.one_es = 1.0 - this.es;
		this.rone_es = 1.0 / this.one_es;
		final AccessorLinear aFalseEasting = pmap.select(ParameterDefinition.False_Easting, pmapDefault).linear();
		this.falseEastingMetres = aFalseEasting.value();
		this.totalFalseEasting = aFalseEasting.projectedUnitsFromMetres(lu);
		final AccessorLinear aFalseNorthing = pmap.select(ParameterDefinition.False_Northing, pmapDefault).linear();
		this.falseNorthingMetres = aFalseNorthing.value();
		this.totalFalseNorthing = aFalseNorthing.projectedUnitsFromMetres(lu);
	}

	public final Ellipsoid ellipsoid;
	public final Unit projectedUnit;

	/**
	 * True if ellipsoid is spherical
	 */
	public final boolean spherical;

	/**
	 * Equatorial radius in projected units
	 */
	public final double totalScale;

	/**
	 * Eccentricty
	 */
	public final double e;

	/**
	 * Eccentricty squared
	 */
	public final double es;

	/**
	 * 1 - (Eccentricity squared)
	 */
	public final double one_es;

	/**
	 * 1 / (1 - (Eccentricity squared))
	 */
	public final double rone_es;

	/**
	 * False easting in metres
	 */
	public final double falseEastingMetres;

	/**
	 * False northing in metres
	 */
	public final double falseNorthingMetres;

	/**
	 * False easting in projected units
	 */
	public final double totalFalseEasting;

	/**
	 * False northing in projected units
	 */
	public final double totalFalseNorthing;
}
