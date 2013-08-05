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
class XaEquidistantCylindrical {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("rc", rc);
		return ds.s();
	}

	public XaEquidistantCylindrical(ParameterMap pmapDefault, ParameterMap pmap, GeographicCoordinateSystem gcs)
			throws GalliumProjectionException {
		if (pmapDefault == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");

		final double phits = pmap.select(ParameterDefinition.Standard_Parallel_1, pmapDefault).angle().radsFromDeg();
		this.rc = Math.cos(phits);
	}
	public final double rc;
}
