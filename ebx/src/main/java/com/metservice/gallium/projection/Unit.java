/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class Unit implements Comparable<Unit> {

	public static final Unit RADIANS = newAngleEpsg(9101, 1.0, "radian", "radians", "rad");
	public static final Unit DEGREES = newAngleEpsg(9102, MapMath.DTR, "degree", "degrees", "deg", "\u00B0");

	public static final Unit METERS = newLength(1.0, "meter", "meters", "m");
	public static final Unit KILOMETERS = newLength(1000.0, "kilometer", "kilometres", "km");
	public static final Unit NAUTICAL_MILES = newLength(1852.0, "nautical mile", "nautical miles", "nm");
	public static final Unit MILES = newLength(1609.344, "mile", "miles", "mi");

	private static Unit newInstance(UnitType type, Authority oAuthority, double toBase, String... names) {
		if (type == null) throw new IllegalArgumentException("object is null");
		if (names == null) throw new IllegalArgumentException("object is null");
		final int card = names.length;
		if (card == 0) throw new IllegalArgumentException("missing unit title");
		final Title singular = Title.newInstance(names[0]);
		final Title plural = card < 2 ? singular : Title.newInstance(names[1]);
		final Title abbr = card < 3 ? singular : Title.newInstance(names[2]);
		final Title oAlt = card < 4 ? null : Title.newInstance(names[3]);
		return new Unit(type, oAuthority, singular, plural, abbr, oAlt, toBase);
	}

	public static Unit newAngleEpsg(int code, double toBase, String... names) {
		return newInstance(UnitType.Angle, Authority.newEPSG(code), toBase, names);
	}

	public static Unit newLength(double toBase, String... names) {
		return newInstance(UnitType.Length, null, toBase, names);
	}

	@Override
	public int compareTo(Unit rhs) {
		final int c0 = type.compareTo(rhs.type);
		if (c0 != 0) return 0;
		final int c1 = singularTitle.compareTo(rhs.singularTitle);
		return c1;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof Unit)) return false;
		return equals((Unit) o);
	}

	public boolean equals(Unit rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return type == rhs.type && singularTitle.equals(rhs.singularTitle);
	}

	public double fromBase(double baseValue) {
		return baseValue / m_scaleToBase;
	}

	@Override
	public int hashCode() {
		return singularTitle.hashCode();
	}

	public double toBase(double altValue) {
		return altValue * m_scaleToBase;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(singularTitle);
		if (oAuthority != null) {
			sb.append(" authority ").append(oAuthority);
		}
		if (m_scaleToBase != 1.0) {
			sb.append(" toBase ").append(m_scaleToBase);
		}
		return sb.toString();
	}

	private Unit(UnitType type, Authority oAuthority, Title singular, Title plural, Title abbr, Title oAlt, double scaleToBase) {
		if (type == null) throw new IllegalArgumentException("object is null");
		if (singular == null) throw new IllegalArgumentException("object is null");
		if (plural == null) throw new IllegalArgumentException("object is null");
		if (abbr == null) throw new IllegalArgumentException("object is null");
		this.type = type;
		this.oAuthority = oAuthority;
		this.singularTitle = singular;
		this.pluralTitle = plural;
		this.abbrTitle = abbr;
		this.oAltTitle = oAlt;
		m_scaleToBase = scaleToBase;
	}
	public final UnitType type;
	public final Authority oAuthority;
	public final Title singularTitle;
	public final Title pluralTitle;
	public final Title abbrTitle;
	public final Title oAltTitle;
	private final double m_scaleToBase;
}
