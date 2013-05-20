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
class EllipsoidDictionary {

	private static final EllipsoidDictionary Instance = newInstance();

	private static EllipsoidDictionary newInstance() {
		final Builder b = new Builder(16);
		b.add(Ellipsoid.Sphere);
		b.add(Ellipsoid.Sphere_ARC_INFO);
		b.add(Ellipsoid.WGS_1984);
		b.add(Ellipsoid.newInverseFlattening("Airy_1830", 6_377_563.396, 299.3249646, "airy"));
		b.add(Ellipsoid.newInverseFlattening("Airy_Modified", 6_377_340.189, 299.3249646, "mod_airy"));

		b.add(Ellipsoid.newInverseFlattening("NAD83: GRS 1980 (IUGG, 1980)", 6_378_137.0, 298.257222101, "NAD83"));
		b.add(Ellipsoid.newInverseFlattening("GRS 1980 (IUGG, 1980)", 6_378_137.0, 298.257222101, "GRS80"));
		b.add(Ellipsoid.newInverseFlattening("WGS 72", 6_378_135.0, 298.26, "WGS72"));

		b.add(Ellipsoid.newMinor("New International 1967", 6_378_157.5, 6_356_772.2, "new_intl"));
		b.add(Ellipsoid.newMinor("Australian", 6_378_160.0, 6_356_774.719, "australian"));
		return new EllipsoidDictionary(b);
	}

	public static Ellipsoid findByName(String qcc) {
		return Instance.findByNameImp(qcc);
	}

	private Ellipsoid findByNameImp(String qcc) {
		if (qcc == null || qcc.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oqcctw = ArgonText.oqtw(qcc);
		if (oqcctw == null) return null;
		final Ellipsoid oMatch = m_mapNameFull.get(oqcctw);
		if (oMatch != null) return oMatch;
		return m_mapNameShort.get(oqcctw);
	}

	private EllipsoidDictionary(Builder b) {
		assert b != null;
		m_mapNameFull = b.nf;
		m_mapNameShort = b.ns;
	}

	private final Map<String, Ellipsoid> m_mapNameFull;
	private final Map<String, Ellipsoid> m_mapNameShort;

	private static class Builder {

		private void put(Map<String, Ellipsoid> dst, String key, Ellipsoid value) {
			if (dst.put(key, value) != null) throw new IllegalStateException("ambiguous key '" + key + "'");
		}

		void add(Ellipsoid e) {
			assert e != null;
			final DualName n = e.name;
			put(nf, n.qcctwFullName(), e);
			if (n.hasDistinctShortName()) {
				put(ns, n.qcctwShortName(), e);
			}
		}

		Builder(int initCap) {
			nf = new HashMap<>(initCap);
			ns = new HashMap<>(initCap);
		}
		final Map<String, Ellipsoid> nf;
		final Map<String, Ellipsoid> ns;
	}
}
