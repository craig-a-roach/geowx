/*
 * Copyright 2006 Jerry Huxtable
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.metservice.gallium.projection;

import java.text.NumberFormat;

/**
 * @author roach
 */
class Unit {

	public final static int ANGLE_UNIT = 0;
	public final static int LENGTH_UNIT = 1;
	public final static int AREA_UNIT = 2;
	public final static int VOLUME_UNIT = 3;

	public static final NumberFormat Format = newFormat();

	private static NumberFormat newFormat() {
		final NumberFormat format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		format.setGroupingUsed(false);
		return format;
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
		return m_value == rhs.m_value;
	}

	public String format(double n) {
		return Format.format(n) + " " + abbreviation;
	}

	public String format(double n, boolean abbrev) {
		if (abbrev) return Format.format(n) + " " + abbreviation;
		return Format.format(n);
	}

	public String format(double x, double y) {
		return format(x, y, true);
	}

	public String format(double x, double y, boolean abbrev) {
		if (abbrev) return Format.format(x) + "/" + Format.format(y) + " " + abbreviation;
		return Format.format(x) + "/" + Format.format(y);
	}

	public double fromBase(double n) {
		return n / m_value;
	}

	public double parse(String s)
			throws NumberFormatException {
		try {
			return Format.parse(s).doubleValue();
		} catch (final java.text.ParseException e) {
			throw new NumberFormatException(e.getMessage());
		}
	}

	public double toBase(double n) {
		return n * m_value;
	}

	@Override
	public String toString() {
		return plural;
	}

	public double value() {
		return m_value;
	}

	public Unit(String name, String plural, String abbreviation, double value) {
		this.name = name;
		this.plural = plural;
		this.abbreviation = abbreviation;
		m_value = value;
	}
	public final String name;
	public final String plural;
	public final String abbreviation;
	private final double m_value;
}
