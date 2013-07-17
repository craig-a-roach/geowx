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
class XaLambertConformalConic {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("projectionLatitude0(deg)", MapMath.radToDeg(projectionLatitudeRads0));
		ds.a("projectionLatitude1(deg)", MapMath.radToDeg(projectionLatitudeRads1));
		ds.a("projectionLatitude2(deg)", MapMath.radToDeg(projectionLatitudeRads2));
		ds.a("scaleFactor", scaleFactor);
		return ds.s();
	}

	public XaLambertConformalConic(ParameterMap pmapDefault, ParameterMap pmap) throws GalliumProjectionException {
		if (pmapDefault == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");

		final double phi0 = pmap.select(ParameterDefinition.Latitude_Of_Origin, pmapDefault).angle().radsFromDeg();
		final double phi1;
		final double phi2;
		final ParameterValue oStandardParallel2 = pmap.find(ParameterDefinition.Standard_Parallel_2);
		if (oStandardParallel2 == null) {
			phi1 = phi0;
			phi2 = phi0;
		} else {
			phi1 = pmap.select(ParameterDefinition.Standard_Parallel_1, pmapDefault).angle().radsFromDeg();
			phi2 = oStandardParallel2.angle().radsFromDeg();
		}

		if (Math.abs(phi1 + phi2) < MapMath.EPS10) {
			final double d1 = MapMath.radToDeg(phi1);
			final double d2 = MapMath.radToDeg(phi2);
			final String m = "Antipodean standard parallels " + d1 + " and " + d2;
			throw new GalliumProjectionException(m);
		}

		this.projectionLatitudeRads0 = phi0;
		this.projectionLatitudeRads1 = phi1;
		this.projectionLatitudeRads2 = phi2;
		this.scaleFactor = pmap.select(ParameterDefinition.Scale_Factor, pmapDefault).ratio().clampedValue(0.01, 1.0);
	}
	public final double projectionLatitudeRads0;
	public final double projectionLatitudeRads1;
	public final double projectionLatitudeRads2;
	public final double scaleFactor;
}
