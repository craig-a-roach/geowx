/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class XfStereographic extends AbstractProjectionFactory {

	public static final Zone NorthPole = Zone.newLatitudeDegrees("NORTH", 90.0);
	public static final Zone SouthPole = Zone.newLatitudeDegrees("SOUTH", -90.0);

	public static final ParameterMap DefaultMap = ParameterMap.newDefault(ParameterDefinition.False_Easting, 0.0,
			ParameterDefinition.False_Northing, 0.0, ParameterDefinition.Central_Meridian, 0.0,
			ParameterDefinition.Scale_Factor, 1.0);

	public static final ParameterMap DefaultOblique = ParameterMap.newDefault(ParameterDefinition.Latitude_Of_Origin, 0.0);
	public static final ParameterMap DefaultPolar = ParameterMap.newDefault(ParameterDefinition.Standard_Parallel_1, 60.0);

	@Override
	public IGalliumProjection newProjection(ParameterMap pmap, GeographicCoordinateSystem gcs, Unit pu)
			throws GalliumProjectionException {
		final ArgBase argBase = new ArgBase(DefaultMap, pmap, gcs, pu);
		final XaStereographic arg;
		if (oZone == null) {
			final double rads = pmap.select(ParameterDefinition.Latitude_Of_Origin, DefaultOblique).angle().radsFromDeg();
			final double arads = Math.abs(rads);
			if (Math.abs(arads - MapMath.HALFPI) < MapMath.EPS10) {
				final boolean north = rads > 0.0;
				arg = new XaStereographicPolar(DefaultMap, pmap, north, rads);
			} else {
				if (arads < MapMath.EPS10) {
					arg = new XaStereographicEquator(DefaultMap, pmap);
				} else {
					arg = new XaStereographicOblique(DefaultMap, pmap, rads);
				}
			}
		} else {
			final boolean north = oZone.radsLatitude() > 0.0;
			final double rads = pmap.select(ParameterDefinition.Standard_Parallel_1, DefaultPolar).angle().radsFromDeg();
			arg = new XaStereographicPolar(DefaultMap, pmap, north, rads);
		}
		return new XpStereographic(oAuthority, title, argBase, arg);
	}

	public XfStereographic() {
	}
}
