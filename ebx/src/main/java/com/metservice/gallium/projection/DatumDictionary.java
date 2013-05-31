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
class DatumDictionary {

	// D_NAD_1983_NSRS2007 (6759) GRS_1980 To_WGS_1984_1 0,0,0
	//
	// D_NZGD_2000 (6167) GRS_1980 To_WGS_1984_1 0,0,0
	//
	// D_OSGB_1936 (6277) Airy_1830 To_WGS_1984_1 375,-111,431
	//
	// D_WGS_1984 (6326) WGS_1984 0,0,0
	//
	// D_Australian_1984 (6203) Australian
	//
	// D_Sphere (6035) Sphere

	private static final DatumDictionary Instance = newInstance();

	private static DatumDictionary newInstance() {
		final Builder b = new Builder(16);
		b.add(Datum.D_Sphere);
		b.add(Datum.D_Sphere_ARC_INFO);
		b.add(Datum.D_WGS_1984);
		b.add(Datum.createInstance("D_OSGB_1936", "Airy_1830", 375.0, -111.0, 431.0));

		b.add(Datum.createInstance("NAD83", "NAD83", 0.0, 0.0, 0.0));
		b.add(Datum.createInstance("European Datum 1979", "New International 1967", -86.0, -98.0, -119.0));
		b.add(Datum.createInstance("Geodetic Datum 1949", "New International 1967", 84.0, -22.0, 209.0));
		b.add(Datum.createInstance("Australian Geodetic 1966", "Australian", -133.0, -48.0, 148.0));
		b.add(Datum.createInstance("Australian Geodetic 1984", "Australian", -134.0, -48.0, 149.0));

		return new DatumDictionary(b);
	}

	public static Datum findByName(String qcc) {
		return Instance.findByNameImp(qcc);
	}

	private Datum findByNameImp(String qcc) {
		if (qcc == null || qcc.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oqcctw = ArgonText.oqtw(qcc);
		if (oqcctw == null) return null;
		final Datum oMatch = m_mapNameFull.get(oqcctw);
		if (oMatch != null) return oMatch;
		return m_mapnameShort.get(oqcctw);
	}

	private DatumDictionary(Builder b) {
		assert b != null;
		m_mapNameFull = b.nf;
		m_mapnameShort = b.ns;
	}

	private final Map<String, Datum> m_mapNameFull;
	private final Map<String, Datum> m_mapnameShort;

	private static class Builder {

		void add(Datum oDatum) {
			if (oDatum != null) {
				final DualName n = oDatum.name;
				nf.put(n.qcctwFullName(), oDatum);
				if (n.hasDistinctShortName()) {
					ns.put(n.qcctwShortName(), oDatum);
				}
			}
		}

		Builder(int initCap) {
			nf = new HashMap<>(initCap);
			ns = new HashMap<>(initCap);
		}
		final Map<String, Datum> nf;
		final Map<String, Datum> ns;
	}
}
