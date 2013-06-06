/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import java.util.HashMap;
import java.util.Map;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class ParameterMap {

	public boolean add(TitleParameter neo) {
		if (neo == null) throw new IllegalArgumentException("object is null");
		return m_mapTitle.put(neo.title, neo) == null;
	}

	public TitleParameter find(Title key) {
		if (key == null) throw new IllegalArgumentException("object is null");
		return m_mapTitle.get(key);
	}

	public TitleParameter select(Title key, TitleParameter def) {
		if (def == null) throw new IllegalArgumentException("object is null");
		final TitleParameter oMatch = find(key);
		return oMatch == null ? def : oMatch;
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
