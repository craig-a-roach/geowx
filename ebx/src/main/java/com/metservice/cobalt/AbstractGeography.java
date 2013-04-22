package com.metservice.cobalt;

import com.metservice.argon.ArgonCompare;

abstract class AbstractGeography implements ICobaltGeography {

	protected abstract int subOrder();

	public final int compareGrid(ICobaltProduct rhs) {
		if (rhs instanceof AbstractGeography) {
			final AbstractGeography r = (AbstractGeography) rhs;
			return ArgonCompare.fwd(subOrder(), r.subOrder());
		}
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	@Override
	public final CobaltDimensionName dimensionName() {
		return CobaltDimensionName.Geography;
	}

	@Override
	public final CobaltDimensionSet dimensionSet() {
		return CobaltDimensionSet.Geography;
	}
}
