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
public class CobaltSurfaceUnary extends AbstractSurface {

	public static final String PName_type = "t";
	public static final String PName_value = "v";

	@Override
	protected int subOrder() {
		return 1;
	}

	@Override
	public void addTo(KmlFeatureText kft) {
		kft.addText(m_type.name());
		kft.addText(" ");
		kft.addTextDouble(m_value);
		kft.addText(m_type.unit().zccSuffix());
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltSurfaceUnary) {
			final CobaltSurfaceUnary r = (CobaltSurfaceUnary) rhs;
			final int c0 = m_type.compareByName(r.m_type);
			if (c0 != 0) return c0;
			final int c1 = ArgonCompare.fwd(m_value, r.m_value);
			return c1;
		}
		return compareSurface(rhs);
	}

	public boolean equals(CobaltSurfaceUnary rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return (m_type == rhs.m_type) && (m_value == rhs.m_value);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltSurfaceUnary)) return false;
		return equals((CobaltSurfaceUnary) o);
	}

	@Override
	public int hashCode() {
		return m_hashCode;
	}

	@Override
	public void saveTo(JsonObject dst) {
		m_type.saveTo(dst, PName_type);
		dst.putDouble(PName_value, m_value);
	}

	@Override
	public String show() {
		final StringBuilder sb = new StringBuilder();
		sb.append(CobaltNCube.ShowCL);
		sb.append(m_type.name()).append(':').append(m_value);
		sb.append(m_type.unit().zccSuffix());
		sb.append(CobaltNCube.ShowCR);
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	public static CobaltSurfaceUnary newAboveGround(double m) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.AboveGround, m);
	}

	public static CobaltSurfaceUnary newAboveMSL(double m) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.AboveMSL, m);
	}

	public static CobaltSurfaceUnary newBelowGround(double cm) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.BelowGround, cm);
	}

	public static CobaltSurfaceUnary newBelowMSL(double m) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.BelowMSL, m);
	}

	public static CobaltSurfaceUnary newEta(double eta) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.ETA, eta);
	}

	public static CobaltSurfaceUnary newHybrid(double level) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.Hybrid, level);
	}

	public static CobaltSurfaceUnary newInstance(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final CobaltSurfaceTypeScalar type = CobaltSurfaceTypeScalar.newInstance(src, PName_type);
		final double value = src.accessor(PName_value).datumDouble();
		return new CobaltSurfaceUnary(type, value);
	}

	public static CobaltSurfaceUnary newIsentropic(double K) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.Isentropic, K);
	}

	public static CobaltSurfaceUnary newIsobaric(double hPa) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.Isobaric, hPa);
	}

	public static CobaltSurfaceUnary newIsothermal(double K) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.Isothermal, K);
	}

	public static CobaltSurfaceUnary newPotentialVorticity(double PV) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.PotentialVorticity, PV);
	}

	public static CobaltSurfaceUnary newPressureDifferenceFromGround(double hPa) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.PressureDifferenceFromGround, hPa);
	}

	public static CobaltSurfaceUnary newSigma(double sigma) {
		return new CobaltSurfaceUnary(CobaltSurfaceTypeScalar.SIGMA, sigma);
	}

	private CobaltSurfaceUnary(CobaltSurfaceTypeScalar type, double value) {
		assert type != null;
		m_type = type;
		m_value = value;
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, m_type);
		hc = HashCoder.and(hc, m_value);
		m_hashCode = hc;
	}
	private final CobaltSurfaceTypeScalar m_type;
	private final double m_value;
	private final int m_hashCode;
}
