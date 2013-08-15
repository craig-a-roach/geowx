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
abstract class XaStereographic {

	public void describe(Ds ds) {
		ds.a("scaleFactor", scaleFactor);
	}

	public abstract double projectionLatitudeRads();

	protected XaStereographic(ParameterMap pmapDefault, ParameterMap pmap) throws GalliumProjectionException {
		this.scaleFactor = pmap.select(ParameterDefinition.Scale_Factor, pmapDefault).ratio().clampedValue(0.01, 1.0);
	}

	public final double scaleFactor;
}
