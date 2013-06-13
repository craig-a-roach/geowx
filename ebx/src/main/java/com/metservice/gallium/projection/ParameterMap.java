/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import java.util.HashMap;
import java.util.Map;

import com.metservice.argon.Ds;
import com.metservice.argon.text.ArgonJoiner;

/**
 * @author roach
 */
class ParameterMap {

	public static ParameterMap newDefault(Object... zpt) {
		final ParameterMap neo = new ParameterMap();
		for (int iName = 0, iValue = 1; iValue < zpt.length; iName += 2, iValue += 2) {
			final Object oTitle = zpt[iName];
			final Object oValue = zpt[iValue];
			if (oTitle instanceof Title && oValue instanceof Double) {
				final Title title = (Title) oTitle;
				final double value = ((Double) oValue).doubleValue();
				neo.add(new TitleParameter(title, value));
			} else {
				final String s = ArgonJoiner.zComma(zpt);
				final String m = "Malformed default parameter bindings at index " + iName + " of [" + s + "]";
				throw new IllegalArgumentException(m);
			}
		}
		return neo;
	}

	public boolean add(TitleParameter neo) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		return m_mapTitle.put(neo.title, neo) == null;
	}

	public TitleParameter find(Title key) {
		if (key == null) throw new IllegalArgumentException("object is null");
		return m_mapTitle.get(key);
	}

	public TitleParameter find(Title key, ParameterMap def) {
		if (def == null) throw new IllegalArgumentException("object is null");
		final TitleParameter oMatch = find(key);
		return oMatch == null ? def.find(key) : oMatch;
	}

	public TitleParameter select(Title key, ParameterMap def)
			throws GalliumProjectionException {
		final TitleParameter oMatch = find(key, def);
		if (oMatch != null) return oMatch;
		final String m = "No explicit or default value defined for parameter '" + key + "'";
		throw new GalliumProjectionException(m);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("mapTitle", m_mapTitle);
		return ds.s();
	}

	public ParameterMap() {
		m_mapTitle = new HashMap<>(16);
	}
	private final Map<Title, TitleParameter> m_mapTitle;
}
