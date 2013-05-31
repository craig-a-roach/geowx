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
class EllipsoidDictionary {

	// Airy_1830 (7001) 6377563.396 6356256.909 0.00334085 0.00667054
	// GRS_1980 (7019) 6378137.000 6356752.314 0.00335281 0.00669438 (298.257222101)
	// Australian (7003) 6378160.000 6356774.719 0.00335289 0.00669454
	// WGS_1984 (7030) 6378137.000 6356752.314 0.00335281 0.00669438 (298.257223563)
	//
	// Sphere (7035) 6371000.000 6371000.000 0.0 0.0

	private static final EllipsoidDictionary Instance = newInstance();

	private static EllipsoidDictionary newInstance() {
		final Builder b = new Builder(16);
		b.add(Ellipsoid.Sphere);
		b.add(Ellipsoid.WGS_1984);
		b.add(Ellipsoid.GRS_1980);
		b.add(Ellipsoid.newMinorEpsg(7001, "Airy_1830", 6_377_563.396, 6_356_256.909));
		b.add(Ellipsoid.newMinorEpsg(7003, "Australian", 6_378_160.000, 6_356_774.719));
		return new EllipsoidDictionary(b);
	}

	public static Ellipsoid findByAuthority(Authority a) {
		return Instance.findByAuthorityImp(a);
	}

	public static Ellipsoid findByTitle(String nc) {
		return Instance.findByTitleImp(Title.newInstance(nc));
	}

	private Ellipsoid findByAuthorityImp(Authority a) {
		assert a != null;
		return m_authorityMap.get(a);
	}

	private Ellipsoid findByTitleImp(Title t) {
		assert t != null;
		return m_titleMap.get(t);
	}

	private EllipsoidDictionary(Builder b) {
		assert b != null;
		m_authorityMap = b.authorityMap;
		m_titleMap = b.titleMap;
	}

	private final Map<Authority, Ellipsoid> m_authorityMap;
	private final Map<Title, Ellipsoid> m_titleMap;

	private static class Builder {

		void add(Ellipsoid e) {
			assert e != null;
			if (titleMap.put(e.title, e) != null) throw new IllegalStateException("ambiguous title..." + e);
			if (e.oAuthority != null) {
				if (authorityMap.put(e.oAuthority, e) != null)
					throw new IllegalStateException("ambiguous authority..." + e);
			}
		}

		Builder(int initCap) {
			authorityMap = new HashMap<>(initCap);
			titleMap = new HashMap<>(initCap);
		}
		final Map<Authority, Ellipsoid> authorityMap;
		final Map<Title, Ellipsoid> titleMap;
	}
}
