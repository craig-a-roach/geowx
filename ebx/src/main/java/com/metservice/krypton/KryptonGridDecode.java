/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;
import com.metservice.cobalt.ICobaltGeography;
import com.metservice.cobalt.ICobaltResolution;

/**
 * @author roach
 */
public class KryptonGridDecode {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("arrayFactory", arrayFactory);
		ds.a("geography", geography);
		ds.a("resolution", resolution);
		ds.a("projector", oProjector);
		return ds.s();
	}

	public KryptonGridDecode(KryptonArrayFactory af, ICobaltGeography geo, ICobaltResolution res, IKryptonGeoProjector oProjector) {
		if (af == null) throw new IllegalArgumentException("object is null");
		if (geo == null) throw new IllegalArgumentException("object is null");
		if (res == null) throw new IllegalArgumentException("object is null");
		this.arrayFactory = af;
		this.geography = geo;
		this.resolution = res;
		this.oProjector = oProjector;
	}

	public final KryptonArrayFactory arrayFactory;
	public final ICobaltGeography geography;
	public final ICobaltResolution resolution;
	public final IKryptonGeoProjector oProjector;
}
