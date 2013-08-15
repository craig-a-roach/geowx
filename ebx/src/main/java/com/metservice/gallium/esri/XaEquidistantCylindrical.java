/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class XaEquidistantCylindrical {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("cosphits", cosphits);
		ds.a("projectionLatitudeRads", projectionLatitudeRads);
		return ds.s();
	}

	public XaEquidistantCylindrical(ParameterMap pmapDefault, ParameterMap pmap, double phits) throws GalliumProjectionException {
		if (pmapDefault == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");
		this.cosphits = Math.cos(phits);
		final AccessorAngle aLatitudeOfOrigin = pmap.select(ParameterDefinition.Latitude_Of_Origin, pmapDefault).angle();
		this.projectionLatitudeRads = aLatitudeOfOrigin.validLatitudeRadsFromDeg(true);
	}
	public final double cosphits;
	public final double projectionLatitudeRads;
}
