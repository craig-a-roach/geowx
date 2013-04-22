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
public class CobaltGeoMercator extends AbstractGeography {

	public static final String PName_lat1 = "lat1";
	public static final String PName_lon1 = "lon1";
	public static final String PName_lat2 = "lat2";
	public static final String PName_lon2 = "lon2";
	public static final String PName_latin = "latin";

	public static CobaltGeoMercator newInstance(double lat1, double lon1, double lat2, double lon2, double latin) {
		return new CobaltGeoMercator(lat1, lon1, lat2, lon2, latin);
	}

	public static CobaltGeoMercator newInstance(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final double lat1 = src.accessor(PName_lat1).datumDouble();
		final double lon1 = src.accessor(PName_lon1).datumDouble();
		final double lat2 = src.accessor(PName_lat2).datumDouble();
		final double lon2 = src.accessor(PName_lon2).datumDouble();
		final double latin = src.accessor(PName_latin).datumDouble();
		return new CobaltGeoMercator(lat1, lon1, lat2, lon2, latin);
	}

	@Override
	protected int subOrder() {
		return 1;
	}

	@Override
	public void addTo(KmlFeatureText kft) {
		kft.addTextDouble(m_lat1);
		kft.addText(",");
		kft.addTextDouble(m_lon1);
		kft.addText(" ");
		kft.addTextDouble(m_lat2);
		kft.addText(",");
		kft.addTextDouble(m_lon2);
		kft.addText(" ");
		kft.addTextDouble(m_latin);
	}

	@Override
	public void addTo(KmlGeometry geo) {
		geo.setCoordinates(m_lat1, m_lon1, m_lat2, m_lon2);
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltGeoMercator) {
			final CobaltGeoMercator r = (CobaltGeoMercator) rhs;
			final int c0 = ArgonCompare.fwd(m_lat1, r.m_lat1);
			if (c0 != 0) return c0;
			final int c1 = ArgonCompare.fwd(m_lon1, r.m_lon1);
			if (c1 != 0) return c1;
			final int c2 = ArgonCompare.fwd(m_lat2, r.m_lat2);
			if (c2 != 0) return c2;
			final int c3 = ArgonCompare.fwd(m_lon2, r.m_lon2);
			if (c3 != 0) return c3;
			final int c4 = ArgonCompare.fwd(m_latin, r.m_latin);
			return c4;
		}
		return compareGrid(rhs);
	}

	public boolean equals(CobaltGeoMercator rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		if (m_hashCode != rhs.m_hashCode) return false;
		if (m_lat1 != rhs.m_lat1) return false;
		if (m_lon1 != rhs.m_lon1) return false;
		if (m_lat2 != rhs.m_lat2) return false;
		if (m_lon2 != rhs.m_lon2) return false;
		if (m_latin != rhs.m_latin) return false;
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltGeoMercator)) return false;
		return equals((CobaltGeoMercator) o);
	}

	@Override
	public int hashCode() {
		return m_hashCode;
	}

	@Override
	public String projectionType() {
		return "Mercator";
	}

	@Override
	public void saveTo(JsonObject dst) {
		dst.putDouble(PName_lat1, m_lat1);
		dst.putDouble(PName_lon1, m_lon1);
		dst.putDouble(PName_lat2, m_lat2);
		dst.putDouble(PName_lon2, m_lon2);
		dst.putDouble(PName_latin, m_latin);
	}

	@Override
	public String show() {
		final StringBuilder sb = new StringBuilder();
		sb.append(CobaltNCube.ShowCL);
		sb.append(m_lat1);
		sb.append(",");
		sb.append(m_lon1);
		sb.append(" ");
		sb.append(m_lat2);
		sb.append(",");
		sb.append(m_lon2);
		sb.append(" ");
		sb.append(m_latin);
		sb.append(CobaltNCube.ShowCR);
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	private CobaltGeoMercator(double lat1, double lon1, double lat2, double lon2, double latin) {
		m_lat1 = lat1;
		m_lon1 = lon1;
		m_lat2 = lat2;
		m_lon2 = lon2;
		m_latin = latin;
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, lat1);
		hc = HashCoder.and(hc, lon1);
		hc = HashCoder.and(hc, lat2);
		hc = HashCoder.and(hc, lon2);
		hc = HashCoder.and(hc, latin);
		m_hashCode = hc;
	}
	private final double m_lat1;
	private final double m_lon1;
	private final double m_lat2;
	private final double m_lon2;
	private final double m_latin;

	private final int m_hashCode;
}
