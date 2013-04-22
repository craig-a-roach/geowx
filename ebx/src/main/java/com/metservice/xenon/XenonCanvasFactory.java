/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.xenon;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.metservice.argon.Ds;
import com.metservice.argon.json.JsonAccessor;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.text.ArgonJoiner;

/**
 * @author roach
 */
public class XenonCanvasFactory {

	public static final String SAFE_FONT = "sansserif";

	private static void verifySafeFont(Set<String> availableFontNames)
			throws XenonPlatformException {
		assert availableFontNames != null;
		if (!availableFontNames.contains(SAFE_FONT)) {
			final String m = "Required built-in font '" + SAFE_FONT + "' is not available";
			throw new XenonPlatformException(m);
		}
	}

	private static Set<String> xsSafeAvailableFontNames()
			throws XenonPlatformException {
		final Set<String> zs = zsAvailableFontNames();
		verifySafeFont(zs);
		return zs;
	}

	private static Set<String> zsAvailableFontNames() {
		final GraphicsEnvironment lge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final String[] qncNames = lge.getAvailableFontFamilyNames();
		final Set<String> ss = new HashSet<String>(qncNames.length);
		for (int i = 0; i < qncNames.length; i++) {
			ss.add(qncNames[i].toLowerCase());
		}
		return ss;
	}

	public static XenonCanvas newCanvas(int pixelWidth, int pixelHeight, JsonObject spec) {
		if (spec == null) throw new IllegalArgumentException("object is null");
		final int pixelW = Math.max(1, pixelWidth);
		final int pixelH = Math.max(1, pixelHeight);
		final JsonAccessor aBackgroun = spec.accessor(CXenonProps.backgroundColor);
		return new XenonCanvas();
	}

	public static XenonCanvasFactory newInstance(XenonCanvasFactoryCfg cfg)
			throws XenonPlatformException {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final Set<String> xsFontNames = xsSafeAvailableFontNames();
		return new XenonCanvasFactory(cfg, xsFontNames);
	}

	public String describeFontFamilyNames() {
		final List<String> xl = new ArrayList<String>(m_xsFontFamilyNames);
		Collections.sort(xl);
		return ArgonJoiner.zComma(xl);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("fontFamilyNames", describeFontFamilyNames());
		ds.a("cfg", m_cfg);
		return ds.s();
	}

	private XenonCanvasFactory(XenonCanvasFactoryCfg cfg, Set<String> xsFontFamilyNames) {
		assert cfg != null;
		assert xsFontFamilyNames != null;
		m_cfg = cfg;
		m_xsFontFamilyNames = xsFontFamilyNames;
	}
	private final XenonCanvasFactoryCfg m_cfg;
	private final Set<String> m_xsFontFamilyNames;
}
