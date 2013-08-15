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
class XaStereographicEquator extends XaStereographic {

	@Override
	public double projectionLatitudeRads() {
		return 0.0;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		super.describe(ds);
		return ds.s();
	}

	public XaStereographicEquator(ParameterMap pmapBaseDefault, ParameterMap pmap) throws GalliumProjectionException {
		super(pmapBaseDefault, pmap);
	}
}
