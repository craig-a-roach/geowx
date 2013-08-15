/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import java.util.HashMap;
import java.util.Map;

/**
 * @author roach
 */
class EllipsoidDictionary {

	private static final EllipsoidDictionary Instance = newInstance();

	private static EllipsoidDictionary newInstance() {
		final Builder b = new Builder(16);
		b.add(Ellipsoid.Sphere);
		b.add(Ellipsoid.WGS_1984);
		b.add(Ellipsoid.GRS_1980);
		b.add(Ellipsoid.newMinorEpsg(7001, "Airy_1830", 6_377_563.396, 6_356_256.909));
		b.add(Ellipsoid.newMinorEpsg(7003, "Australian", 6_378_160.000, 6_356_774.719));
		b.add(Ellipsoid.newMinorEpsg(7004, "Bessel_1841", 6_377_397.155, 6_356_078.963));
		return new EllipsoidDictionary(b);
	}

	public static Ellipsoid findByAuthority(Authority a) {
		return Instance.findByAuthorityImp(a);
	}

	public static Ellipsoid findByTitle(String nc) {
		return Instance.findByTitleImp(Title.newInstance(nc));
	}

	public static Ellipsoid selectByAuthority(Authority a) {
		final Ellipsoid oMatch = findByAuthority(a);
		if (oMatch == null) throw new IllegalArgumentException("Eliipsoid " + a + " not in dictionary");
		return oMatch;
	}

	private Ellipsoid findByAuthorityImp(Authority a) {
		assert a != null;
		return m_authorityMap.get(a);
	}

	private Ellipsoid findByTitleImp(Title t) {
		assert t != null;
		final Ellipsoid oMatch = m_titleMap.get(t);
		if (oMatch != null) return oMatch;
		final Authority oAuth = Authority.createInstance(t);
		return oAuth == null ? null : findByAuthorityImp(oAuth);
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
