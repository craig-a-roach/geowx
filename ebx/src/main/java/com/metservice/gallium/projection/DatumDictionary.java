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
class DatumDictionary {

	private static final DatumDictionary Instance = newInstance();

	private static DatumDictionary newInstance() {
		final Builder b = new Builder(16);
		b.add(Datum.D_Sphere);
		b.add(Datum.D_WGS_1984);
		b.add(Datum.newEpsg(6759, "D_NAD_1983_NSRS2007", Ellipsoid.GRS_1980, GeocentricTranslation.Zero));
		b.add(Datum.newEpsg(6277, "D_OSGB_1936", 7001, new GeocentricTranslation(375.0, -111.0, 431.0)));
		b.add(Datum.newEpsg(6203, "D_Australian_1984", 7003, null));
		b.add(Datum.newEpsg(6167, "D_NZGD_2000", Ellipsoid.GRS_1980, GeocentricTranslation.Zero));

		return new DatumDictionary(b);
	}

	public static Datum findByAuthority(Authority a) {
		return Instance.findByAuthorityImp(a);
	}

	public static Datum findByTitle(String nc) {
		return Instance.findByTitleImp(Title.newInstance(nc));
	}

	public static Datum selectByAuthority(Authority a) {
		final Datum oMatch = findByAuthority(a);
		if (oMatch == null) throw new IllegalArgumentException("Datum " + a + " not in dictionary");
		return oMatch;
	}

	private Datum findByAuthorityImp(Authority a) {
		assert a != null;
		return m_authorityMap.get(a);
	}

	private Datum findByTitleImp(Title t) {
		assert t != null;
		final Datum oMatch = m_titleMap.get(t);
		if (oMatch != null) return oMatch;
		final Authority oAuth = Authority.createInstance(t);
		return oAuth == null ? null : findByAuthorityImp(oAuth);
	}

	private DatumDictionary(Builder b) {
		assert b != null;
		m_authorityMap = b.authorityMap;
		m_titleMap = b.titleMap;
	}

	private final Map<Authority, Datum> m_authorityMap;
	private final Map<Title, Datum> m_titleMap;

	private static class Builder {

		void add(Datum oD) {
			if (oD == null) return;
			if (titleMap.put(oD.title, oD) != null) throw new IllegalStateException("ambiguous title..." + oD);
			if (oD.oAuthority != null) {
				if (authorityMap.put(oD.oAuthority, oD) != null)
					throw new IllegalStateException("ambiguous authority..." + oD);
			}
		}

		Builder(int initCap) {
			authorityMap = new HashMap<>(initCap);
			titleMap = new HashMap<>(initCap);
		}
		final Map<Authority, Datum> authorityMap;
		final Map<Title, Datum> titleMap;
	}
}
