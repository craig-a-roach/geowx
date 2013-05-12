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

	public static final Unit DEGREES = newAngle(1.0, "degree", "deg", "degrees", "\u00B0");
	public static final Unit RADIANS = newAngle(MapMath.RTD, "radian", "rad");

	public static final Unit METERS = newLength(1.0, "meter", "m");
	public static final Unit KILOMETERS = newLength(1000.0, "kilometer", "km");
	public static final Unit NAUTICAL_MILES = newLength(1852.0, "nautical mile", "nm");
	public static final Unit MILES = newLength(1609.344, "mile", "mi");

	private static Unit newInstance(UnitType type, double toBase, String... names) {
		if (type == null) throw new IllegalArgumentException("object is null");
		if (names == null) throw new IllegalArgumentException("object is null");
		final int card = names.length;
		if (card == 0) throw new IllegalArgumentException("missing unit name");
		final String singularFull = names[0];
		final String singularShort = card < 2 ? singularFull : names[1];
		final String pluralFull = card < 3 ? (singularFull + "s") : names[2];
		final String pluralShort = card < 4 ? singularShort : names[3];
		final DualName singular = DualName.newInstance(singularFull, singularShort);
		final DualName plural = DualName.newInstance(pluralFull, pluralShort);
		return new Unit(type, singular, plural, toBase);
	}

	public static Unit newAngle(double toBase, String... names) {
		return newInstance(UnitType.Angle, toBase, names);
	}

	public static Unit newLength(double toBase, String... names) {
		return newInstance(UnitType.Length, toBase, names);
	}

	@Override
	public int compareTo(Unit rhs) {
		final int c0 = type.compareTo(rhs.type);
		if (c0 != 0) return 0;
		final int c1 = pluralName.compareTo(rhs.pluralName);
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
		return type == rhs.type && pluralName.equals(rhs.pluralName);
	}

	public double fromBase(double baseValue) {
		return baseValue / m_scaleToBase;
	}

	@Override
	public int hashCode() {
		return pluralName.hashCode();
	}

	public double toBase(double altValue) {
		return altValue * m_scaleToBase;
	}

	@Override
	public String toString() {
		return pluralName.qcctwFullName();
	}

	private Unit(UnitType type, DualName singular, DualName plural, double scaleToBase) {
		if (type == null) throw new IllegalArgumentException("object is null");
		if (singular == null) throw new IllegalArgumentException("object is null");
		if (plural == null) throw new IllegalArgumentException("object is null");
		this.type = type;
		this.singularName = singular;
		this.pluralName = plural;
		m_scaleToBase = scaleToBase;
	}
	public final UnitType type;
	public final DualName singularName;
	public final DualName pluralName;
	private final double m_scaleToBase;
}
