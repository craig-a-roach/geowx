/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class Zone {

	public static Zone newDegrees(String qncId, double degLon, double degLat) {
		return new Zone(qncId, MapMath.degToRad(degLon), MapMath.degToRad(degLat));
	}

	public static Zone newLatitudeDegrees(String qncId, double deg) {
		return new Zone(qncId, 0.0, MapMath.degToRad(deg));
	}

	public static Zone newLongitudeDegrees(String qncId, double deg) {
		return new Zone(qncId, MapMath.degToRad(deg), 0.0);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof Zone)) return false;
		return equals((Zone) o);
	}

	public boolean equals(Zone rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_qucId.equals(rhs.m_qucId);
	}

	@Override
	public int hashCode() {
		return m_qucId.hashCode();
	}

	public double radsLatitude() {
		return m_radsLat;
	}

	public double radsLongitude() {
		return m_radsLon;
	}

	@Override
	public String toString() {
		return m_qucId;
	}

	private Zone(String qncId, double radsLon, double radsLat) {
		if (qncId == null || qncId.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_qucId = qncId.toUpperCase();
		m_radsLon = radsLon;
		m_radsLat = radsLat;
	}
	private final String m_qucId;
	private final double m_radsLon;
	private final double m_radsLat;
}
