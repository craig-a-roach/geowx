/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class ArgCylindrical {

	public ArgCylindrical(ParameterMap pmapDefault, ParameterMap pmap, ArgBase base) throws GalliumProjectionException {
		if (pmapDefault == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");
		if (base == null) throw new IllegalArgumentException("object is null");
		this.projectionLongitudeRads = pmap.select(ParameterDefinition.Central_Meridian, pmapDefault).angle()
				.normalizedLongitudeRadsFromDeg();
	}

	/**
	 * Projection longitude or central meridian in radians.
	 */
	public final double projectionLongitudeRads;

}
