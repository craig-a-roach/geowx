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

	public static final Zone NorthPole = Zone.newLatitudeDegrees(90.0, "NORTH");
	public static final Zone SouthPole = Zone.newLatitudeDegrees(-90.0, "SOUTH");

	public static final ParameterMap DefaultMap = ParameterMap.newDefault(ParameterDefinition.False_Easting, 0.0,
			ParameterDefinition.False_Northing, 0.0, ParameterDefinition.Central_Meridian, 0.0,
			ParameterDefinition.Scale_Factor, 1.0);

	@Override
	public IGalliumProjection newProjection(ParameterMap pmap, GeographicCoordinateSystem gcs, Unit pu)
			throws GalliumProjectionException {
		final ArgBase argBase = new ArgBase(DefaultMap, pmap, gcs, pu);
		final XaStereographic arg = new XaStereographic(DefaultMap, pmap, oZone);
		return new XpStereographic(oAuthority, title, argBase, arg);
	}

	public XfStereographic() {
	}
}
