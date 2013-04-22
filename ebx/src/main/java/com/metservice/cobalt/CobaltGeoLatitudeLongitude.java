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
public class CobaltGeoLatitudeLongitude extends AbstractGeography {

	public static final String PName_nlat = "nlat";
	public static final String PName_wlon = "wlon";
	public static final String PName_slat = "slat";
	public static final String PName_elon = "elon";

	public static CobaltGeoLatitudeLongitude newInstance(double nlat, double wlon, double slat, double elon) {
		return new CobaltGeoLatitudeLongitude(nlat, wlon, slat, elon);
	}

	public static CobaltGeoLatitudeLongitude newInstance(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final double nlat = src.accessor(PName_nlat).datumDouble();
		final double wlon = src.accessor(PName_wlon).datumDouble();
		final double slat = src.accessor(PName_slat).datumDouble();
		final double elon = src.accessor(PName_elon).datumDouble();
		return new CobaltGeoLatitudeLongitude(nlat, wlon, slat, elon);
	}

	@Override
	protected int subOrder() {
		return 0;
	}

	@Override
	public void addTo(KmlFeatureText kft) {
		kft.addTextDouble(m_nlat);
		kft.addText(",");
		kft.addTextDouble(m_wlon);
		kft.addText(" ");
		kft.addTextDouble(m_slat);
		kft.addText(",");
		kft.addTextDouble(m_elon);
	}

	@Override
	public void addTo(KmlGeometry geo) {
		geo.setCoordinates(m_nlat, m_wlon, m_slat, m_elon);
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltGeoLatitudeLongitude) {
			final CobaltGeoLatitudeLongitude r = (CobaltGeoLatitudeLongitude) rhs;
			final int c0 = ArgonCompare.fwd(m_nlat, r.m_nlat);
			if (c0 != 0) return c0;
			final int c1 = ArgonCompare.fwd(m_wlon, r.m_wlon);
			if (c1 != 0) return c1;
			final int c2 = ArgonCompare.fwd(m_slat, r.m_slat);
			if (c2 != 0) return c2;
			final int c3 = ArgonCompare.fwd(m_elon, r.m_elon);
			return c3;
		}
		return compareGrid(rhs);
	}

	public boolean equals(CobaltGeoLatitudeLongitude rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		if (m_hashCode != rhs.m_hashCode) return false;
		if (m_nlat != rhs.m_nlat) return false;
		if (m_wlon != rhs.m_wlon) return false;
		if (m_slat != rhs.m_slat) return false;
		if (m_elon != rhs.m_elon) return false;
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltGeoLatitudeLongitude)) return false;
		return equals((CobaltGeoLatitudeLongitude) o);
	}

	@Override
	public int hashCode() {
		return m_hashCode;
	}

	@Override
	public String projectionType() {
		return "Latitude-Longitude";
	}

	@Override
	public void saveTo(JsonObject dst) {
		dst.putDouble(PName_nlat, m_nlat);
		dst.putDouble(PName_wlon, m_wlon);
		dst.putDouble(PName_slat, m_slat);
		dst.putDouble(PName_elon, m_elon);
	}

	@Override
	public String show() {
		final StringBuilder sb = new StringBuilder();
		sb.append(CobaltNCube.ShowCL);
		sb.append(m_nlat);
		sb.append(",");
		sb.append(m_wlon);
		sb.append(" ");
		sb.append(m_slat);
		sb.append(",");
		sb.append(m_elon);
		sb.append(CobaltNCube.ShowCR);
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	private CobaltGeoLatitudeLongitude(double nlat, double wlon, double slat, double elon) {
		m_nlat = nlat;
		m_wlon = wlon;
		m_slat = slat;
		m_elon = elon;
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, nlat);
		hc = HashCoder.and(hc, wlon);
		hc = HashCoder.and(hc, slat);
		hc = HashCoder.and(hc, elon);
		m_hashCode = hc;
	}
	private final double m_nlat;
	private final double m_wlon;
	private final double m_slat;
	private final double m_elon;
	private final int m_hashCode;
}
