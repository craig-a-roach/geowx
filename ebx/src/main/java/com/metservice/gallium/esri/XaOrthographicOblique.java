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
class XaOrthographicOblique extends XaOrthographic {

	@Override
	public double projectionLatitudeRads() {
		return projectionLatitudeRads;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("projectionLatitude(deg)", MapMath.radToDeg(projectionLatitudeRads));
		return ds.s();
	}

	public XaOrthographicOblique(double projectionLatitudeRads) {
		this.projectionLatitudeRads = projectionLatitudeRads;
	}
	public final double projectionLatitudeRads;
}
