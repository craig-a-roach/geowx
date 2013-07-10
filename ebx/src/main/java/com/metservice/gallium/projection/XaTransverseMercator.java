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
class XaTransverseMercator {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("scaleFactor", scaleFactor);
		ds.a("projectionLatitude(deg)", MapMath.radToDeg(projectionLatitudeRads));
		return ds.s();
	}

	public XaTransverseMercator(ParameterMap pmapDefault, ParameterMap pmap, GeographicCoordinateSystem gcs)
			throws GalliumProjectionException {
		if (pmapDefault == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");

		final AccessorRatio aScaleFactor = pmap.select(ParameterDefinition.Scale_Factor, pmapDefault).ratio();
		this.scaleFactor = aScaleFactor.clampedValue(0.01, 1.0);
		final AccessorAngle aLatitideOfOrigin = pmap.select(ParameterDefinition.Latitude_Of_Origin, pmapDefault).angle();
		this.projectionLatitudeRads = aLatitideOfOrigin.normalizedLatitudeRadsFromDeg();
	}
	public final double scaleFactor;
	public final double projectionLatitudeRads;
}
