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
public class CobaltSurfaceTopBottom extends AbstractSurface {

	public static final String PName_type = "t";
	public static final String PName_valueTop = "vt";
	public static final String PName_valueBot = "vb";

	@Override
	protected int subOrder() {
		return 2;
	}

	@Override
	public void addTo(KmlFeatureText kft) {
		kft.addText(m_type.name());
		kft.addText(" ");
		kft.addTextDouble(m_top);
		kft.addText("/");
		kft.addTextDouble(m_bot);
		kft.addText(m_type.unit().zccSuffix());
	}

	@Override
	public int compareTo(ICobaltProduct rhs) {
		if (rhs instanceof CobaltSurfaceTopBottom) {
			final CobaltSurfaceTopBottom r = (CobaltSurfaceTopBottom) rhs;
			final int c0 = m_type.compareByName(r.m_type);
			if (c0 != 0) return c0;
			final int c1 = ArgonCompare.fwd(m_top, r.m_top);
			if (c1 != 0) return c1;
			final int c2 = ArgonCompare.fwd(m_bot, r.m_bot);
			return c2;
		}
		return compareSurface(rhs);
	}

	public boolean equals(CobaltSurfaceTopBottom rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_type == rhs.m_type && m_top == rhs.m_top && m_bot == rhs.m_bot;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof CobaltSurfaceTopBottom)) return false;
		return equals((CobaltSurfaceTopBottom) o);
	}

	@Override
	public int hashCode() {
		return m_hashCode;
	}

	@Override
	public void saveTo(JsonObject dst) {
		m_type.saveTo(dst, PName_type);
		dst.putDouble(PName_valueTop, m_top);
		dst.putDouble(PName_valueBot, m_bot);
	}

	@Override
	public String show() {
		final StringBuilder sb = new StringBuilder();
		sb.append(CobaltNCube.ShowCL);
		sb.append(m_type.name()).append(':').append(m_top).append('/').append(m_bot);
		sb.append(m_type.unit().zccSuffix());
		sb.append(CobaltNCube.ShowCR);
		return sb.toString();
	}

	@Override
	public String toString() {
		return show();
	}

	public static CobaltSurfaceTopBottom newAboveGround(double mTop, double mBot) {
		return new CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar.AboveGround, mTop, mBot);
	}

	public static CobaltSurfaceTopBottom newAboveMSL(double mTop, double mBot) {
		return new CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar.AboveMSL, mTop, mBot);
	}

	public static CobaltSurfaceTopBottom newBelowGround(double cmTop, double cmBot) {
		return new CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar.BelowGround, cmTop, cmBot);
	}

	public static CobaltSurfaceTopBottom newEta(double etaTop, double etaBot) {
		return new CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar.SIGMA, etaTop, etaBot);
	}

	public static CobaltSurfaceTopBottom newHybrid(double levelTop, double levelBot) {
		return new CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar.Hybrid, levelTop, levelBot);
	}

	public static CobaltSurfaceTopBottom newInstance(JsonObject src)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final CobaltSurfaceTypeScalar type = CobaltSurfaceTypeScalar.newInstance(src, PName_type);
		final double valueTop = src.accessor(PName_valueTop).datumDouble();
		final double valueBot = src.accessor(PName_valueBot).datumDouble();
		return new CobaltSurfaceTopBottom(type, valueTop, valueBot);
	}

	public static CobaltSurfaceTopBottom newIsentropic(double KTop, double KBot) {
		return new CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar.Isentropic, KTop, KBot);
	}

	public static CobaltSurfaceTopBottom newIsobaric(double hPaTop, double hPaBot) {
		return new CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar.Isobaric, hPaTop, hPaBot);
	}

	public static CobaltSurfaceTopBottom newPressureDifferenceFromGround(double hPaTop, double hPaBot) {
		return new CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar.PressureDifferenceFromGround, hPaTop, hPaBot);
	}

	public static CobaltSurfaceTopBottom newSigma(double sigmaTop, double sigmaBot) {
		return new CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar.SIGMA, sigmaTop, sigmaBot);
	}

	private CobaltSurfaceTopBottom(CobaltSurfaceTypeScalar type, double top, double bot) {
		assert type != null;
		m_type = type;
		m_top = top;
		m_bot = bot;
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, m_type);
		hc = HashCoder.and(hc, m_top);
		hc = HashCoder.and(hc, m_bot);
		m_hashCode = hc;
	}
	private final CobaltSurfaceTypeScalar m_type;
	private final double m_top;
	private final double m_bot;
	private final int m_hashCode;
}
