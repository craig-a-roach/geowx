/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class AccessorAngle {

	private static void validate(ParameterValue src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		if (src.definition.type == UnitType.Angle) return;
		final String m = src.definition + " is not an angle";
		throw new IllegalArgumentException(m);
	}

	private String fail(String op, ProjectionException ex) {
		return "Failed to " + op + " " + m_src + "..." + ex.getMessage();
	}

	public double normalizedLatitudeRadsFromDeg()
			throws GalliumProjectionException {
		try {
			return MapMath.normalizeLatitude(radsFromDeg());
		} catch (final ProjectionException ex) {
			final String m = fail("normalize latitude", ex);
			throw new GalliumProjectionException(m);
		}
	}

	public double normalizedLongitudeRadsFromDeg()
			throws GalliumProjectionException {
		try {
			return MapMath.normalizeLongitude(radsFromDeg());
		} catch (final ProjectionException ex) {
			final String m = fail("normalize longitude", ex);
			throw new GalliumProjectionException(m);
		}
	}

	public double radsFromDeg() {
		return MapMath.degToRad(m_src.value);
	}

	public double value() {
		return m_src.value;
	}

	public AccessorAngle(ParameterValue src) {
		validate(src);
		m_src = src;
	}
	private final ParameterValue m_src;
}
