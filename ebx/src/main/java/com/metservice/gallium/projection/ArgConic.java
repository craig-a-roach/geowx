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
class ArgConic {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("scaleFactor", scaleFactor);
		return ds.s();
	}

	public ArgConic(ParameterMap pmapDefault, ParameterMap pmap, GeographicCoordinateSystem gcs)
			throws GalliumProjectionException {
		if (pmapDefault == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");

		final ParameterValue oScaleFactor = pmap.find(ParameterDefinition.Scale_Factor);
		if (oScaleFactor == null) {
			final AccessorAngle aStandardParallel = pmap.select(ParameterDefinition.Standard_Parallel_1, pmapDefault).angle();
			final double phits = aStandardParallel.radsFromDeg();
			final Ellipsoid elp = gcs.datum.ellipsoid;
			if (elp.isSpherical) {
				this.scaleFactor = Math.cos(phits);
			} else {
				this.scaleFactor = MapMath.msfn(Math.sin(phits), Math.cos(phits), elp.eccentricity2);
			}
		} else {
			this.scaleFactor = oScaleFactor.ratio().clampedValue(0.01, 1.0);
		}
	}
	public final double scaleFactor;
}
