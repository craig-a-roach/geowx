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

	public static Zone newDegrees(double degLon, double degLat, String qncId) {
		return new Zone(MapMath.degToRad(degLon), MapMath.degToRad(degLat), qncId);
	}

	public static Zone newLatitudeDegrees(double deg, String qncId) {
		return new Zone(0.0, MapMath.degToRad(deg), qncId);
	}

	public static Zone newLongitudeDegrees(double deg, String qncId) {
		return new Zone(MapMath.degToRad(deg), 0.0, qncId);
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

	private Zone(double radsLon, double radsLat, String qncId) {
		if (qncId == null || qncId.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_radsLon = radsLon;
		m_radsLat = radsLat;
		m_qucId = qncId.toUpperCase();
	}
	private final double m_radsLon;
	private final double m_radsLat;
	private final String m_qucId;
}
