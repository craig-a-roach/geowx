/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.CArgon;
import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
abstract class AbstractPrognosis implements ICobaltPrognosis {

	protected static Elapsed elapsed_ssec(int ssec) {
		return Elapsed.newInstance(ssec * CArgon.SEC_TO_MS);
	}

	protected abstract int subOrder();

	public final int comparePrognosis(ICobaltProduct rhs) {
		if (rhs instanceof AbstractPrognosis) {
			final AbstractPrognosis r = (AbstractPrognosis) rhs;
			return ArgonCompare.fwd(subOrder(), r.subOrder());
		}
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	@Override
	public final CobaltDimensionName dimensionName() {
		return CobaltDimensionName.Prognosis;
	}

	@Override
	public final CobaltDimensionSet dimensionSet() {
		return CobaltDimensionSet.Prognosis;
	}
}
