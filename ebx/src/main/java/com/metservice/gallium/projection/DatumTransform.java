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
class DatumTransform {

	public static final DatumTransform Zero = new DatumTransform(0.0, 0.0, 0.0);

	public static DatumTransform newInstance(ParameterArray src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		return new DatumTransform(src.select(0, 0.0), src.select(1, 0.0), src.select(2, 0.0));
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("deltaX", deltaX);
		ds.a("deltaY", deltaY);
		ds.a("deltaZ", deltaZ);
		return ds.s();
	}

	public DatumTransform(double deltaX, double deltaY, double deltaZ) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		this.deltaZ = deltaZ;
	}
	public final double deltaX;
	public final double deltaY;
	public final double deltaZ;

}
