/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class SimplePackingSpecGenerator {

	// out = (R + (in * 2^E)) / 10^D
	// (out * 10^DD) - R = in * 2^E
	// in = ((out * 10^D) - R) / 2^E

	public SimplePackingSpec newSpec(float[] xptData) {
		if (xptData == null) throw new IllegalArgumentException("object is null");
		final int len = xptData.length;
		if (len == 0) throw new IllegalArgumentException("empty array");
		float vmin = xptData[0];
		float vmax = vmin;
		for (int i = 1; i < len; i++) {
			final float v = xptData[i];
			if (Float.isNaN(v)) {
				continue;
			}
			if (v < vmin) {
				vmin = v;
			}
			if (v > vmax) {
				vmax = v;
			}
		}
		return null;
	}

}
