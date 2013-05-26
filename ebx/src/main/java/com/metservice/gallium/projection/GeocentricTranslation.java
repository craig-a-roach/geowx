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
class GeocentricTranslation implements IDatumTransform {

	public static final GeocentricTranslation Zero = new GeocentricTranslation(0.0, 0.0, 0.0);

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("deltaXmetres", deltaXmetres);
		ds.a("deltaYmetres", deltaYmetres);
		ds.a("deltaZmetres", deltaZmetres);
		return ds.s();
	}

	public GeocentricTranslation(double deltaXmetres, double deltaYmetres, double deltaZmetres) {
		this.deltaXmetres = deltaXmetres;
		this.deltaYmetres = deltaYmetres;
		this.deltaZmetres = deltaZmetres;
	}
	public final double deltaXmetres;
	public final double deltaYmetres;
	public final double deltaZmetres;

}
