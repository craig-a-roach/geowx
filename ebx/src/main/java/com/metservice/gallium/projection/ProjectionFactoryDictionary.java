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
class ProjectionFactoryDictionary {

	private static final ProjectionFactoryDictionary Instance = newInstance();

	private static ProjectionFactoryDictionary newInstance() {
		final Builder b = new Builder(128);
		b.add(ProjectionSelector.newEpsg(9804, "Mercator_1SP", ProjectionFactoryMercator.class, 1));
		b.add(ProjectionSelector.newEpsg(9807, "Transverse_Mercator", ProjectionFactoryTransverseMercator.class));
		return new ProjectionFactoryDictionary(b);
	}

	public static ProjectionSelector findByAuthority(Authority a) {
		return Instance.findByAuthorityImp(a);
	}

	public static ProjectionSelector findByTitle(String nc) {
		return Instance.findByTitleImp(Title.newInstance(nc));
	}

	private ProjectionSelector findByAuthorityImp(Authority a) {
		assert a != null;
		return m_authorityMap.get(a);
	}

	private ProjectionSelector findByTitleImp(Title t) {
		assert t != null;
		return m_titleMap.get(t);
	}

	private ProjectionFactoryDictionary(Builder b) {
		assert b != null;
		m_authorityMap = b.authorityMap;
		m_titleMap = b.titleMap;
	}
	final Map<Authority, ProjectionSelector> m_authorityMap;
	final Map<Title, ProjectionSelector> m_titleMap;

	private static class Builder {

		void add(ProjectionSelector s) {
			assert s != null;
			if (titleMap.put(s.title, s) != null) throw new IllegalStateException("ambiguous title..." + s);
			if (s.oAuthority != null) {
				if (authorityMap.put(s.oAuthority, s) != null)
					throw new IllegalStateException("ambiguous authority..." + s);
			}
		}

		Builder(int initCap) {
			authorityMap = new HashMap<>(initCap);
			titleMap = new HashMap<>(initCap);
		}
		final Map<Authority, ProjectionSelector> authorityMap;
		final Map<Title, ProjectionSelector> titleMap;
	}
}
