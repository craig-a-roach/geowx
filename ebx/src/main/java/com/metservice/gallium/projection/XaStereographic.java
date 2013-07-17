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
class XaStereographic {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("mode", mode);
		ds.a("scaleFactor", scaleFactor);
		ds.a("projectionLatitude(deg)", MapMath.radToDeg(projectionLatitudeRads));
		return ds.s();
	}

	public XaStereographic(ParameterMap pmapDefault, ParameterMap pmap, Zone oZone) throws GalliumProjectionException {
		if (oZone == null) {
			final ParameterValue oLatOrigin = pmap.find(ParameterDefinition.Latitude_Of_Origin);
			this.projectionLatitudeRads = oLatOrigin == null ? 0.0 : oLatOrigin.angle().radsFromDeg();
		} else {
			this.projectionLatitudeRads = oZone.radsLatitude();
		}
		this.scaleFactor = pmap.select(ParameterDefinition.Scale_Factor, pmapDefault).ratio().clampedValue(0.01, 1.0);
		final double absLatRads = Math.abs(projectionLatitudeRads);
		if (Math.abs(absLatRads - MapMath.HALFPI) < MapMath.EPS10) {
			mode = this.projectionLatitudeRads < 0.0 ? Mode.SOUTH_POLE : Mode.NORTH_POLE;
		} else {
			mode = absLatRads > MapMath.EPS10 ? Mode.OBLIQUE : Mode.EQUATOR;
		}
	}

	public final double projectionLatitudeRads;
	public final double scaleFactor;
	public final Mode mode;

	public static enum Mode {
		NORTH_POLE, SOUTH_POLE, EQUATOR, OBLIQUE;
	}
}
