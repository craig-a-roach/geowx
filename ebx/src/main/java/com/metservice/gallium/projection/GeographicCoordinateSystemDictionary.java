/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import java.util.HashMap;
import java.util.Map;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public class GeographicCoordinateSystemDictionary {

	private static final GeographicCoordinateSystemDictionary Instance = newInstance();

	private static GeographicCoordinateSystemDictionary newInstance() {
		final Builder b = new Builder(16);
		b.add(GeographicCoordinateSystem.GCS_Sphere);
		b.add(GeographicCoordinateSystem.GCS_WGS84);
		// GCS_Australian_1984 4203
		b.add(GeographicCoordinateSystem.createGreenwichDegrees("GCS_OSGB_1936", "D_OSGB_1936", Authority.newEPSG(4277)));
		return new GeographicCoordinateSystemDictionary(b);
	}

	public static GeographicCoordinateSystem findByAuthority(Authority a) {
		return Instance.findByAuthorityImp(a);
	}

	public static GeographicCoordinateSystem findByName(String qcc) {
		return Instance.findByNameImp(qcc);
	}

	private GeographicCoordinateSystem findByAuthorityImp(Authority a) {
		if (a == null) throw new IllegalArgumentException("object is null");
		final String qcctwCode = a.qcctwQualifiedCode();
		return m_authorityMap.get(qcctwCode);
	}

	private GeographicCoordinateSystem findByNameImp(String qcc) {
		if (qcc == null || qcc.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oqcctw = ArgonText.oqtw(qcc);
		if (oqcctw == null) return null;
		return m_nameMap.get(oqcctw);
	}

	private GeographicCoordinateSystemDictionary(Builder b) {
		assert b != null;
		m_nameMap = b.nameMap;
		m_authorityMap = b.authorityMap;
	}
	private final Map<String, GeographicCoordinateSystem> m_nameMap;
	private final Map<String, GeographicCoordinateSystem> m_authorityMap;

	private static class Builder {

		void add(GeographicCoordinateSystem oGCS) {
			if (oGCS != null) {
				final DualName n = oGCS.name;
				nameMap.put(n.qcctwFullName(), oGCS);
				if (oGCS.oAuthority != null) {
					final String qcctwCode = oGCS.oAuthority.qcctwQualifiedCode();
					authorityMap.put(qcctwCode, oGCS);
				}
			}
		}

		Builder(int initCap) {
			nameMap = new HashMap<>(initCap);
			authorityMap = new HashMap<>(initCap);
		}
		final Map<String, GeographicCoordinateSystem> nameMap;
		final Map<String, GeographicCoordinateSystem> authorityMap;
	}

}
