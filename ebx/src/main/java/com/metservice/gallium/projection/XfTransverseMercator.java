/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class XfTransverseMercator extends AbstractProjectionFactory {

	public static final ParameterMap DefaultMap = ParameterMap.newDefault(ParameterDefinition.False_Easting, 0.0,
			ParameterDefinition.False_Northing, 0.0, ParameterDefinition.Central_Meridian, 0.0,
			ParameterDefinition.Scale_Factor, 1.0, ParameterDefinition.Latitude_Of_Origin, 0.0);

	@Override
	public IGalliumProjection newProjection(ParameterMap pmap, GeographicCoordinateSystem gcs, Unit pu)
			throws GalliumProjectionException {
		final ArgBase argBase = new ArgBase(DefaultMap, pmap, gcs, pu);
		final XaTransverseMercator arg = new XaTransverseMercator(DefaultMap, pmap, gcs);
		return new XpTransverseMercator(oAuthority, title, argBase, arg);
	}

	public XfTransverseMercator() {
	}
}
