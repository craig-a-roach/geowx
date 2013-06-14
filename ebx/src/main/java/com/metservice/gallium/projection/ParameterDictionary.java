/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author roach
 */
class ParameterDictionary {

	private static final ParameterDictionary Instance = newInstance();

	private static ParameterDictionary newInstance() {
		final Builder b = new Builder(128);
		final ParameterDefinition[] values = ParameterDefinition.values();
		for (int i = 0; i < values.length; i++) {
			b.add(values[i]);
		}
		return new ParameterDictionary(b);
	}

	public static ParameterDefinition findByTitle(String nc) {
		return Instance.findByTitleImp(Title.newInstance(nc));
	}

	private ParameterDefinition findByTitleImp(Title t) {
		assert t != null;
		return m_titleMap.get(t);
	}

	private ParameterDictionary(Builder b) {
		assert b != null;
		m_titleMap = b.titleMap;
	}

	private final Map<Title, ParameterDefinition> m_titleMap;

	private static class Builder {

		private void put(Title key, ParameterDefinition value) {
			if (titleMap.put(key, value) != null) throw new IllegalStateException("ambiguous title '" + key + "'");
		}

		void add(ParameterDefinition u) {
			assert u != null;
			put(u.title, u);
		}

		Builder(int initCap) {
			titleMap = new HashMap<>(initCap);
		}
		final Map<Title, ParameterDefinition> titleMap;
	}

}
