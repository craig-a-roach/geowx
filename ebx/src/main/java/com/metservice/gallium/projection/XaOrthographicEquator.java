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
class XaOrthographicEquator extends XaOrthographic {

	@Override
	public double projectionLatitudeRads() {
		return 0.0;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		return ds.s();
	}

	public XaOrthographicEquator() {
	}
}
