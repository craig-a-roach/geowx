/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class GeoProjectorLatitudeLongitude implements IKryptonGeoProjector {

	private static final double WeightEpsilon = 1.0e-9;

	private static final boolean hasWeight(double w) {
		return w >= WeightEpsilon;
	}

	@Override
	public float bilinearInterpolate(KryptonArray array, double longitude, double latititude) {
		if (array == null) throw new IllegalArgumentException("object is null");
		final double lon = UGeo.angle360(longitude);
		final double lat = UGeo.clamp90(latititude);
		final int i = (int) (UGeo.angle360((lon - wlon)) / dlon) + 1;
		final int j = (int) ((lat - slat) / dlat) + 1;
		final double lonSW = UGeo.angle360(wlon + (i - 1) * dlon);
		final double latSW = slat + (j - 1) * dlat;
		final int iE = m_scan.eastNeighbour(i);
		final int jN = m_scan.northNeighbour(j);

		final double u = UGeo.angle360(lon - lonSW) / dlon;
		final double v = (lat - latSW) / dlat;

		final float vSW = array.value(m_scan.arrayIndex(i, j));
		final double wSW = (1.0 - u) * (1.0 - v);
		if (UGrib.isBdsUndefined(vSW) && hasWeight(wSW)) return Float.NaN;

		final float vNW = array.value(m_scan.arrayIndex(i, jN));
		final double wNW = (1.0 - u) * v;
		if (UGrib.isBdsUndefined(vNW) && hasWeight(wNW)) return Float.NaN;

		final float vSE = array.value(m_scan.arrayIndex(iE, j));
		final double wSE = u * (1.0 - v);
		if (UGrib.isBdsUndefined(vSE) && hasWeight(wSE)) return Float.NaN;

		final float vNE = array.value(m_scan.arrayIndex(iE, jN));
		final double wNE = u * v;
		if (UGrib.isBdsUndefined(vNE) && hasWeight(wNE)) return Float.NaN;

		final double result = (vSW * wSW) + (vNW * wNW) + (vSE * wSE) + (vNE * wNE);
		return (float) result;
	}

	@Override
	public boolean isThin() {
		return m_oThin != null;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("N", nlat);
		ds.a("W", wlon);
		ds.a("S", slat);
		ds.a("E", elon);
		ds.a("dlon", dlon);
		ds.a("dlat", dlat);
		ds.a("scan", m_scan);
		ds.a("thin", m_oThin);
		return ds.s();
	}

	public GeoProjectorLatitudeLongitude(GridScan scan, double dx, double dy, double lat1, double lon1, double lat2, double lon2,
			KryptonThinGrid oThin) {
		this.dlon = dx;
		this.dlat = dy;
		this.wlon = UGeo.angle360(scan.wlon(lon1, lon2));
		this.elon = UGeo.angle360(scan.elon(lon1, lon2));
		this.slat = UGeo.clamp90(scan.slat(lat1, lat2));
		this.nlat = UGeo.clamp90(scan.nlat(lat1, lat2));
		m_scan = scan;
		m_oThin = oThin;
	}
	public final double dlon;
	public final double dlat;
	public final double wlon;
	public final double elon;
	public final double slat;
	public final double nlat;
	private final GridScan m_scan;
	private final KryptonThinGrid m_oThin;
}
