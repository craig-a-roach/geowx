/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class SpatialPackingSpec {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("orderSpatial", orderSpatial);
		ds.a("descriptorSpatial", descriptorSpatial);
		return ds.s();
	}

	public SpatialPackingSpec(int orderSpatial, int descriptorSpatial) {
		this.orderSpatial = orderSpatial;
		this.descriptorSpatial = descriptorSpatial;
	}
	public final int orderSpatial;
	public final int descriptorSpatial;
}
