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
class UnitDictionary {

	private static final UnitDictionary Instance = newInstance();

	private static UnitDictionary newInstance() {
		final Builder b = new Builder(128);
		b.add(Unit.RADIANS);
		b.add(Unit.DEGREES);
		b.add(Unit.newAngleEpsg(9103, MapMath.DTR / 60.0, "arc minute", "arc minutes", "min", "\'"));
		b.add(Unit.newAngleEpsg(9104, MapMath.DTR / 3600.0, "arc second", "arc seconds", "sec", "\""));

		b.add(Unit.METERS);
		b.add(Unit.KILOMETERS);
		b.add(Unit.NAUTICAL_MILES);
		b.add(Unit.MILES);

		b.add(Unit.newLength(0.01, "centimeter", "centimeters", "cm"));
		b.add(Unit.newLength(0.001, "millimeter", "millimeters", "mm"));
		b.add(Unit.newLength(0.9144, "yard", "yards", "yd"));
		b.add(Unit.newLengthEpsg(9002, 0.3048, "foot", "feet", "ft"));
		b.add(Unit.newLength(0.0254, "inch", "inches", "in"));
		b.add(Unit.newLengthEpsg(9035, 1609.3472186944375, "U.S. mile", "U.S. miles", "us-mi"));
		b.add(Unit.newLength(0.914401828803658, "U.S. yard", "U.S. yards", "us-yd"));
		b.add(Unit.newLengthEpsg(9003, 0.30480060960121924, "U.S. foot", "U.S. feet", "us-ft"));
		b.add(Unit.newLength(1.0 / 39.37, "U.S. inch", "U.S. inches", "us-in"));

		return new UnitDictionary(b);
	}

	public static Unit findByAuthority(Authority a) {
		return Instance.findByAuthorityImp(a);
	}

	public static Unit findByTitle(String nc) {
		return Instance.findByTitleImp(Title.newInstance(nc));
	}

	private Unit findByAuthorityImp(Authority a) {
		assert a != null;
		return m_authorityMap.get(a);
	}

	private Unit findByTitleImp(Title t) {
		assert t != null;
		return m_titleMap.get(t);
	}

	private UnitDictionary(Builder b) {
		assert b != null;
		m_authorityMap = b.authorityMap;
		m_titleMap = b.titleMap;
	}

	private final Map<Authority, Unit> m_authorityMap;
	private final Map<Title, Unit> m_titleMap;

	private static class Builder {

		private void put(Authority key, Unit value) {
			if (authorityMap.put(key, value) != null) throw new IllegalStateException("ambiguous authority '" + key + "'");
		}

		private void put(Title key, Unit value) {
			if (titleMap.put(key, value) != null) throw new IllegalStateException("ambiguous title '" + key + "'");
		}

		void add(Unit u) {
			assert u != null;
			put(u.singularTitle, u);
			put(u.pluralTitle, u);
			put(u.abbrTitle, u);
			for (int i = 0; i < u.zptAlt.length; i++) {
				put(u.zptAlt[i], u);
			}
			if (u.oAuthority != null) {
				put(u.oAuthority, u);
			}
		}

		Builder(int initCap) {
			authorityMap = new HashMap<>(initCap);
			titleMap = new HashMap<>(initCap);
		}
		final Map<Authority, Unit> authorityMap;
		final Map<Title, Unit> titleMap;
	}

}
