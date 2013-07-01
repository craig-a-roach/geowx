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
public class GeographicCoordinateSystemDictionary {

	private static final GeographicCoordinateSystemDictionary Instance = newInstance();

	private static GeographicCoordinateSystemDictionary newInstance() {
		final Builder b = new Builder(32);
		b.add(GeographicCoordinateSystem.GCS_Sphere);
		b.add(GeographicCoordinateSystem.GCS_WGS84);
		b.add(GeographicCoordinateSystem.newGreenwichDegreesEpsg(4759, "GCS_NAD_1983_NSRS2007", 6759));
		b.add(GeographicCoordinateSystem.newGreenwichDegreesEpsg(4277, "GCS_OSGB_1936", 6277));
		b.add(GeographicCoordinateSystem.newGreenwichDegreesEpsg(4203, "GCS_Australian_1984", 6203));
		b.add(GeographicCoordinateSystem.newGreenwichDegreesEpsg(4167, "GCS_NZGD_2000", 6167));
		b.add(GeographicCoordinateSystem.newGreenwichDegreesEpsg(4004, "GCS_Bessel_1841", 6004));
		return new GeographicCoordinateSystemDictionary(b);
	}

	public static GeographicCoordinateSystem findByAuthority(Authority a) {
		return Instance.findByAuthorityImp(a);
	}

	public static GeographicCoordinateSystem findByTitle(String nc) {
		return Instance.findByTitleImp(Title.newInstance(nc));
	}

	private GeographicCoordinateSystem findByAuthorityImp(Authority a) {
		assert a != null;
		return m_authorityMap.get(a);
	}

	private GeographicCoordinateSystem findByTitleImp(Title t) {
		assert t != null;
		final GeographicCoordinateSystem oMatch = m_titleMap.get(t);
		if (oMatch != null) return oMatch;
		final Authority oAuth = Authority.createInstance(t);
		return oAuth == null ? null : findByAuthorityImp(oAuth);
	}

	private GeographicCoordinateSystemDictionary(Builder b) {
		assert b != null;
		m_authorityMap = b.authorityMap;
		m_titleMap = b.titleMap;
	}
	private final Map<Authority, GeographicCoordinateSystem> m_authorityMap;
	private final Map<Title, GeographicCoordinateSystem> m_titleMap;

	private static class Builder {

		void add(GeographicCoordinateSystem oGCS) {
			if (oGCS == null) return;
			if (titleMap.put(oGCS.title, oGCS) != null) throw new IllegalStateException("ambiguous title..." + oGCS);
			if (oGCS.oAuthority != null) {
				if (authorityMap.put(oGCS.oAuthority, oGCS) != null)
					throw new IllegalStateException("ambiguous authority..." + oGCS);
			}
		}

		Builder(int initCap) {
			authorityMap = new HashMap<>(initCap);
			titleMap = new HashMap<>(initCap);
		}
		final Map<Authority, GeographicCoordinateSystem> authorityMap;
		final Map<Title, GeographicCoordinateSystem> titleMap;
	}

}
