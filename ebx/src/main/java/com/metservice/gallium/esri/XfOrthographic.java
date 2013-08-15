/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

/**
 * @author roach
 */
class XfOrthographic extends AbstractProjectionFactory {

	public static final ParameterMap DefaultMap = ParameterMap.newDefault(ParameterDefinition.False_Easting, 0.0,
			ParameterDefinition.False_Northing, 0.0, ParameterDefinition.Central_Meridian, -75.0,
			ParameterDefinition.Latitude_Of_Origin, 40.0);

	@Override
	public IGalliumProjection newProjection(ParameterMap pmap, GeographicCoordinateSystem gcs, Unit pu)
			throws GalliumProjectionException {
		final ArgBase argBase = new ArgBase(DefaultMap, pmap, gcs, pu);
		final XaOrthographic arg;
		final double phi0 = pmap.select(ParameterDefinition.Latitude_Of_Origin, DefaultMap).angle().radsFromDeg();
		final double aphi0 = Math.abs(phi0);
		if (Math.abs(aphi0 - MapMath.HALFPI) < MapMath.EPS10) {
			final boolean north = phi0 > 0.0;
			arg = new XaOrthographicPolar(north);
		} else {
			if (aphi0 < MapMath.EPS10) {
				arg = new XaOrthographicEquator();
			} else {
				arg = new XaOrthographicOblique(phi0);
			}
		}
		return new XpOrthographic(oAuthority, title, argBase, arg);
	}

	public XfOrthographic() {
	}
}
