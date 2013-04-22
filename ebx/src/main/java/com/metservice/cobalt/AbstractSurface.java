/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.ArgonCompare;

/**
 * @author roach
 */
abstract class AbstractSurface implements ICobaltSurface {

	protected abstract int subOrder();

	public final int compareSurface(ICobaltProduct rhs) {
		if (rhs instanceof AbstractSurface) {
			final AbstractSurface r = (AbstractSurface) rhs;
			return ArgonCompare.fwd(subOrder(), r.subOrder());
		}
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	@Override
	public final CobaltDimensionName dimensionName() {
		return CobaltDimensionName.Surface;
	}

	@Override
	public final CobaltDimensionSet dimensionSet() {
		return CobaltDimensionSet.Surface;
	}
}
