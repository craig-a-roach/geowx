/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.ArgonCompare;
import com.metservice.argon.HashCoder;

/**
 * @author roach
 */
public class KryptonPlaceId implements Comparable<KryptonPlaceId> {

	@Override
	public int compareTo(KryptonPlaceId rhs) {
		final int c0 = ArgonCompare.fwd(latitude, rhs.latitude);
		if (c0 != 0) return c0;
		final int c1 = ArgonCompare.fwd(longitude, rhs.longitude);
		return c1;
	}

	public boolean equals(KryptonPlaceId rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return latitude == rhs.latitude && longitude == rhs.longitude;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof KryptonPlaceId)) return false;
		return equals((KryptonPlaceId) o);
	}

	public String formatCardinal(String ozSep) {
		final StringBuilder sb = new StringBuilder();
		if (latitude < 0.0) {
			sb.append(-latitude).append("S");
		} else if (latitude > 0.0) {
			sb.append(latitude).append("N");
		} else {
			sb.append(latitude);
		}
		if (ozSep != null) {
			sb.append(ozSep);
		}
		if (longitude < 0.0) {
			sb.append(-longitude).append("W");
		} else if (longitude > 0.0) {
			sb.append(longitude).append("E");
		} else {
			sb.append(longitude);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return m_hc;
	}

	@Override
	public String toString() {
		final String card = formatCardinal(" ");
		return (oqcctwName == null) ? card : (card + " (" + oqcctwName + ")");
	}

	public KryptonPlaceId(double lat, double lon, String oqcctwName) {
		this.latitude = lat;
		this.longitude = lon;
		this.oqcctwName = oqcctwName;
		int hc = HashCoder.INIT;
		hc = HashCoder.and(hc, lat);
		hc = HashCoder.and(hc, lon);
		m_hc = hc;
	}
	public final double latitude;
	public final double longitude;
	public final String oqcctwName;
	private final int m_hc;
}
