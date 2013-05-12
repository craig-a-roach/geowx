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
class UnitDictionary {

	private static final UnitDictionary Instance = newInstance();

	private static UnitDictionary newInstance() {
		final Builder b = new Builder(32);
		b.add(Unit.DEGREES);
		b.add(Unit.RADIANS);
		b.add(Unit.newAngle(1.0 / 60.0, "arc minute", "min", "arc minutes", "\'"));
		b.add(Unit.newAngle(1.0 / 3600.0, "arc second", "sec", "arc seconds", "\""));

		b.add(Unit.METERS);
		b.add(Unit.KILOMETERS);
		b.add(Unit.NAUTICAL_MILES);
		b.add(Unit.MILES);

		b.add(Unit.newLength(0.01, "centimeter", "cm"));
		b.add(Unit.newLength(0.001, "millimeter", "mm"));
		b.add(Unit.newLength(0.9144, "yard", "yd", "yards", "yds"));
		b.add(Unit.newLength(0.3048, "foot", "ft", "feet"));
		b.add(Unit.newLength(0.0254, "inch", "in", "inches", "ins"));
		b.add(Unit.newLength(1609.347218694437, "U.S. mile", "us-mi"));
		b.add(Unit.newLength(0.914401828803658, "U.S. yard", "us-yd", "U.S. yards", "us-yds"));
		b.add(Unit.newLength(0.304800609601219, "U.S. foot", "us-ft", "U.S. feet"));
		b.add(Unit.newLength(1.0 / 39.37, "U.S. inch", "us-in", "U.S. inches", "us-ins"));

		return new UnitDictionary(b);
	}

	public static Unit findByName(String qcc) {
		return Instance.findByNameImp(qcc);
	}

	private Unit findByNameImp(String qcc) {
		if (qcc == null || qcc.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oqcctw = ArgonText.oqtw(qcc);
		if (oqcctw == null) return null;
		Unit oMatch = m_mapPluralFull.get(oqcctw);
		if (oMatch != null) return oMatch;
		oMatch = m_mapPluralShort.get(oqcctw);
		if (oMatch != null) return oMatch;
		oMatch = m_mapSingularFull.get(oqcctw);
		if (oMatch != null) return oMatch;
		oMatch = m_mapSingularShort.get(oqcctw);
		return oMatch;
	}

	private UnitDictionary(Builder b) {
		assert b != null;
		m_mapSingularFull = b.singularFull;
		m_mapSingularShort = b.singularShort;
		m_mapPluralFull = b.pluralFull;
		m_mapPluralShort = b.pluralShort;
	}

	private final Map<String, Unit> m_mapSingularFull;
	private final Map<String, Unit> m_mapSingularShort;
	private final Map<String, Unit> m_mapPluralFull;
	private final Map<String, Unit> m_mapPluralShort;

	private static class Builder {

		private void put(Map<String, Unit> dst, String key, Unit value) {
			if (dst.put(key, value) != null) throw new IllegalStateException("ambiguous key '" + key + "'");
		}

		void add(Unit u) {
			assert u != null;
			final DualName plural = u.pluralName;
			put(pluralFull, plural.qcctwFullName(), u);
			if (plural.hasDistinctShortName()) {
				put(pluralShort, plural.qcctwShortName(), u);
			}
			final DualName singular = u.singularName;
			put(singularFull, singular.qcctwFullName(), u);
			if (singular.hasDistinctShortName()) {
				put(singularShort, singular.qcctwShortName(), u);
			}
		}

		Builder(int initCap) {
			singularFull = new HashMap<>(initCap);
			singularShort = new HashMap<>(initCap);
			pluralFull = new HashMap<>(initCap);
			pluralShort = new HashMap<>(initCap);
		}
		final Map<String, Unit> singularFull;
		final Map<String, Unit> singularShort;
		final Map<String, Unit> pluralFull;
		final Map<String, Unit> pluralShort;
	}

}
