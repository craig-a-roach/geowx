/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.DecimalMask;

/**
 * @author roach
 */
class UNeonProfile {

	public static int pct(long q, long d) {
		if (d == 0L) return 0;
		final long lpct = Math.round(q * 100.0 / d);
		if (lpct < 0L) return 0;
		if (lpct > 100L) return 100;
		return (int) lpct;

	}

	public static int pctClass(int pct) {
		if (pct >= 40) return 4;
		if (pct >= 15) return 3;
		if (pct >= 5) return 2;
		if (pct >= 1) return 1;
		return 0;
	}

	public static String qTiming(long ns) {
		return MaskMs.format(ns * MS_PER_NS) + "ms";
	}

	private UNeonProfile() {
	}

	public static final DecimalMask MaskMs = DecimalMask.newFixed(false, false, 0, false, false, Integer.valueOf(3));
	public static final double MS_PER_NS = 1.0e-6;
}
