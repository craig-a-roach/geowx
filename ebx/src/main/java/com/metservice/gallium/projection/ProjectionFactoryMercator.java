/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class ProjectionFactoryMercator extends AbstractProjectionFactory {

	public static final ParameterMap DefaultMap = ParameterMap.newDefault(ParameterDefinition.False_Easting, 0.0,
			ParameterDefinition.False_Northing, 0.0, ParameterDefinition.Central_Meridian, 0.0);

	@Override
	public IGalliumProjection newProjection(ParameterMap pmap, GeographicCoordinateSystem gcs, Unit lu)
			throws GalliumProjectionException {

		final ArgBase aBase = new ArgBase(DefaultMap, pmap, gcs, lu);
		final ArgCylindrical aCylindrical = new ArgCylindrical(DefaultMap, pmap, aBase);
		return new ProjectionMercator(oAuthority, title, aCylindrical);
	}

	public ProjectionFactoryMercator() {
	}
}
