/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class AccessorRatio {

	private static void validate(ParameterValue src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		if (src.definition.type == UnitType.Ratio) return;
		final String m = src.definition + " is not a ratio";
		throw new IllegalArgumentException(m);
	}

	public double clampedValue(double lo, double hi) {
		return MapMath.clamp(lo, m_src.value, hi);
	}

	@Override
	public String toString() {
		return m_src.toString();
	}

	public double value() {
		return m_src.value;
	}

	public AccessorRatio(ParameterValue src) {
		validate(src);
		m_src = src;
	}
	private final ParameterValue m_src;
}
