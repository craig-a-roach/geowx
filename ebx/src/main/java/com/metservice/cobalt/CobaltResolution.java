/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.HashCoder;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class CobaltResolution implements ICobaltResolution {

	public static final String PName_unit = "u";
	public static final String PName_x = "x";
	public static final String PName_y = "y";

	public static final CobaltResolution DEG1500 = new CobaltResolution(CobaltResolutionUnit.Degrees, 1.5, 1.5);
	public static final CobaltResolution DEG1000 = new CobaltResolution(CobaltResolutionUnit.Degrees, 1.0, 1.0);
	public static final CobaltResolution DEG0500 = new CobaltResolution(CobaltResolutionUnit.Degrees, 0.5, 0.5);
	public static final CobaltResolution DEG0250 = new CobaltResolution(CobaltResolutionUnit.Degrees, 0.25, 0.25);
	public static final CobaltResolution DEG0125 = new CobaltResolution(CobaltResolutionUnit.Degrees, 0.125, 0.125);

	@Override
	public void addTo(KmlFeatureText kft) {
		kft.addTextDouble(m_y);
		kft.addText(",");
		kft.addTextDouble(m_x);
		kft.addText(m_unit.qccSuffix());
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltResolution) {
			final CobaltResolution r = (CobaltResolution) rhs;
			final int c0 = m_unit.compareByName(r.m_unit);
			if (c0 != 0) return c0;
			final int c1 = ArgonCompare.fwd(m_y, r.m_y);
			if (c1 != 0) return c1;
			final int c2 = ArgonCompare.fwd(m_x, r.m_x);
			return c2;
		}
		throw new IllegalArgumentException("invalid rhs>" + rhs + "<");
	}

	@Override
	public CobaltDimensionName dimensionName() {
		return CobaltDimensionName.Resolution;
	}

	@Override
	public CobaltDimensionSet dimensionSet() {
		return CobaltDimensionSet.Resolution;
	}

	public boolean equals(CobaltResolution rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return (m_unit == rhs.m_unit) && (m_y == rhs.m_y) && (m_x == rhs.m_x);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltResolution)) return false;
		return equals((CobaltResolution) o);
	}

	@Override
	public int hashCode() {
		return m_hashCode;
	}

	@Override
	public void saveTo(JsonObject dst) {
		m_unit.saveTo(dst, PName_unit);
		dst.putDouble(PName_y, m_y);
		dst.putDouble(PName_x, m_x);
	}

	@Override
	public String show() {
		final StringBuilder sb = new StringBuilder();
		sb.append(CobaltNCube.ShowCL);
		sb.append(m_y).append(':').append(m_x);
		sb.append(m_unit.qccSuffix());
		sb.append(CobaltNCube.ShowCR);
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	public static CobaltResolution newDegrees(double y, double x) {
		if (y == x) {
			if (y == 0.125) return DEG0125;
			if (y == 0.25) return DEG0250;
			if (y == 0.5) return DEG0500;
			if (y == 1.0) return DEG1000;
			if (y == 1.5) return DEG1500;
		}
		return new CobaltResolution(CobaltResolutionUnit.Degrees, y, x);
	}

	public static CobaltResolution newInstance(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final CobaltResolutionUnit type = CobaltResolutionUnit.newInstance(src, PName_unit);
		final double y = src.accessor(PName_y).datumDouble();
		final double x = src.accessor(PName_x).datumDouble();
		return new CobaltResolution(type, y, x);
	}

	public static CobaltResolution newMetres(double y, double x) {
		return new CobaltResolution(CobaltResolutionUnit.Metres, y, x);
	}

	private CobaltResolution(CobaltResolutionUnit unit, double y, double x) {
		assert unit != null;
		m_unit = unit;
		m_y = y;
		m_x = x;
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, m_unit);
		hc = HashCoder.and(hc, m_y);
		hc = HashCoder.and(hc, m_x);
		m_hashCode = hc;
	}
	private final CobaltResolutionUnit m_unit;
	private final double m_y;
	private final double m_x;

	private final int m_hashCode;
}
