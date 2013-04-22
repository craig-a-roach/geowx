/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public class CobaltSurfaceZero extends AbstractSurface {

	public static final String PName_type = "t";

	public static final CobaltSurfaceZero Ground = new CobaltSurfaceZero(CobaltSurfaceTypeZero.Ground);
	public static final CobaltSurfaceZero CloudBase = new CobaltSurfaceZero(CobaltSurfaceTypeZero.CloudBase);
	public static final CobaltSurfaceZero CloudTops = new CobaltSurfaceZero(CobaltSurfaceTypeZero.CloudTops);
	public static final CobaltSurfaceZero IsothermC0 = new CobaltSurfaceZero(CobaltSurfaceTypeZero.IsothermC0);
	public static final CobaltSurfaceZero AdiabaticCondensationLifted = new CobaltSurfaceZero(
			CobaltSurfaceTypeZero.AdiabaticCondensationLifted);
	public static final CobaltSurfaceZero MaximumWind = new CobaltSurfaceZero(CobaltSurfaceTypeZero.MaximumWind);
	public static final CobaltSurfaceZero Tropopause = new CobaltSurfaceZero(CobaltSurfaceTypeZero.Tropopause);
	public static final CobaltSurfaceZero NominalTopOfAtmosphere = new CobaltSurfaceZero(
			CobaltSurfaceTypeZero.NominalTopOfAtmosphere);
	public static final CobaltSurfaceZero SeaBottom = new CobaltSurfaceZero(CobaltSurfaceTypeZero.SeaBottom);

	public static final CobaltSurfaceZero MSL = new CobaltSurfaceZero(CobaltSurfaceTypeZero.MSL);
	public static final CobaltSurfaceZero EntireAtmosphereAsSingleLayer = new CobaltSurfaceZero(
			CobaltSurfaceTypeZero.EntireAtmosphereAsSingleLayer);
	public static final CobaltSurfaceZero EntireOceanAsSingleLayer = new CobaltSurfaceZero(
			CobaltSurfaceTypeZero.EntireOceanAsSingleLayer);
	public static final CobaltSurfaceZero HighestTroposphericFreezingLevel = new CobaltSurfaceZero(
			CobaltSurfaceTypeZero.HighestTroposphericFreezingLevel);
	public static final CobaltSurfaceZero BoundaryCloudBottom = new CobaltSurfaceZero(CobaltSurfaceTypeZero.BoundaryCloudBottom);
	public static final CobaltSurfaceZero BoundaryCloudTop = new CobaltSurfaceZero(CobaltSurfaceTypeZero.BoundaryCloudTop);
	public static final CobaltSurfaceZero BoundaryCloudLayer = new CobaltSurfaceZero(CobaltSurfaceTypeZero.BoundaryCloudLayer);
	public static final CobaltSurfaceZero LowCloudBottom = new CobaltSurfaceZero(CobaltSurfaceTypeZero.LowCloudBottom);
	public static final CobaltSurfaceZero LowCloudTop = new CobaltSurfaceZero(CobaltSurfaceTypeZero.LowCloudTop);
	public static final CobaltSurfaceZero LowCloudLayer = new CobaltSurfaceZero(CobaltSurfaceTypeZero.LowCloudLayer);
	public static final CobaltSurfaceZero MiddleCloudBottom = new CobaltSurfaceZero(CobaltSurfaceTypeZero.MiddleCloudBottom);
	public static final CobaltSurfaceZero MiddleCloudTop = new CobaltSurfaceZero(CobaltSurfaceTypeZero.MiddleCloudTop);
	public static final CobaltSurfaceZero MiddleCloudLayer = new CobaltSurfaceZero(CobaltSurfaceTypeZero.MiddleCloudLayer);
	public static final CobaltSurfaceZero HighCloudBottom = new CobaltSurfaceZero(CobaltSurfaceTypeZero.HighCloudBottom);
	public static final CobaltSurfaceZero HighCloudTop = new CobaltSurfaceZero(CobaltSurfaceTypeZero.HighCloudTop);
	public static final CobaltSurfaceZero HighCloudLayer = new CobaltSurfaceZero(CobaltSurfaceTypeZero.HighCloudLayer);
	public static final CobaltSurfaceZero PlanetaryBoundaryLayer = new CobaltSurfaceZero(
			CobaltSurfaceTypeZero.PlanetaryBoundaryLayer);
	public static final CobaltSurfaceZero ConvectiveCloudBottom = new CobaltSurfaceZero(
			CobaltSurfaceTypeZero.ConvectiveCloudBottom);
	public static final CobaltSurfaceZero ConvectiveCloudTop = new CobaltSurfaceZero(CobaltSurfaceTypeZero.ConvectiveCloudTop);
	public static final CobaltSurfaceZero ConvectiveCloudLayer = new CobaltSurfaceZero(CobaltSurfaceTypeZero.ConvectiveCloudLayer);

	public static CobaltSurfaceZero newInstance(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final CobaltSurfaceTypeZero type = CobaltSurfaceTypeZero.newInstance(src, PName_type);
		return new CobaltSurfaceZero(type);
	}

	@Override
	protected int subOrder() {
		return 0;
	}

	@Override
	public void addTo(KmlFeatureText kft) {
		kft.addText(m_type.name());
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltSurfaceZero) {
			final CobaltSurfaceZero r = (CobaltSurfaceZero) rhs;
			final int c0 = m_type.compareByName(r.m_type);
			return c0;
		}
		return compareSurface(rhs);
	}

	public boolean equals(CobaltSurfaceZero rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_type == rhs.m_type;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltSurfaceZero)) return false;
		return equals((CobaltSurfaceZero) o);
	}

	@Override
	public int hashCode() {
		return m_type.hashCode();
	}

	@Override
	public void saveTo(JsonObject dst) {
		m_type.saveTo(dst, PName_type);
	}

	@Override
	public String show() {
		final StringBuilder sb = new StringBuilder();
		sb.append(CobaltNCube.ShowCL);
		sb.append(m_type.name());
		sb.append(CobaltNCube.ShowCR);
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	private CobaltSurfaceZero(CobaltSurfaceTypeZero type) {
		assert type != null;
		m_type = type;
	}

	private final CobaltSurfaceTypeZero m_type;
}
