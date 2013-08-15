/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

/**
 * @author roach
 */
class XfEquidistantCylindrical extends AbstractProjectionFactory {

	public static final Zone EQ = Zone.newLatitudeDegrees("EQUATOR", 0.0);

	public static final ParameterMap DefaultMap = ParameterMap.newDefault(ParameterDefinition.False_Easting, 0.0,
			ParameterDefinition.False_Northing, 0.0, ParameterDefinition.Central_Meridian, 0.0,
			ParameterDefinition.Latitude_Of_Origin, 0.0, ParameterDefinition.Standard_Parallel_1, 60.0);

	@Override
	public IGalliumProjection newProjection(ParameterMap pmap, GeographicCoordinateSystem gcs, Unit pu)
			throws GalliumProjectionException {
		final ArgBase argBase = new ArgBase(DefaultMap, pmap, gcs, pu);
		final double phits;
		if (oZone == null) {
			final AccessorAngle alatts = pmap.select(ParameterDefinition.Standard_Parallel_1, DefaultMap).angle();
			phits = alatts.validLatitudeRadsFromDeg(false);
		} else {
			phits = oZone.radsLatitude();
		}
		final XaEquidistantCylindrical arg = new XaEquidistantCylindrical(DefaultMap, pmap, phits);
		return new XpEquidistantCylindrical(oAuthority, title, argBase, arg);
	}

	public XfEquidistantCylindrical() {
	}
}
