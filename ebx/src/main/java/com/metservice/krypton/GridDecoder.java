/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.cobalt.CobaltGeoLatitudeLongitude;
import com.metservice.cobalt.CobaltGeoMercator;
import com.metservice.cobalt.CobaltResolution;

/**
 * @author roach
 */
class GridDecoder {

	public KryptonGridDecode newGrid(SectionGD1Type00Reader r) {
		if (r == null) throw new IllegalArgumentException("object is null");
		final double lat1 = r.latitude1();
		final double lon1 = r.longitude1();
		final double lat2 = r.latitude2();
		final double lon2 = r.longitude2();
		final KryptonThinGrid oThin = r.createThinGrid();
		final int nx = oThin == null ? r.NX() : oThin.nx;
		final int ny = oThin == null ? r.NY() : oThin.ny;
		final double dx = oThin == null ? r.DX() : oThin.dx;
		final double dy = oThin == null ? r.DY() : oThin.dy;
		final KryptonArrayFactory af = new KryptonArrayFactory(nx, ny);
		final GridScan scan = new GridScan(af, r.scanningMode());
		final GeoProjectorLatitudeLongitude prj = new GeoProjectorLatitudeLongitude(scan, dx, dy, lat1, lon1, lat2, lon2, oThin);
		final CobaltGeoLatitudeLongitude geo = CobaltGeoLatitudeLongitude.newInstance(prj.nlat, prj.wlon, prj.slat, prj.elon);
		final CobaltResolution resolution = CobaltResolution.newDegrees(dy, dx);
		return new KryptonGridDecode(af, geo, resolution, prj);
	}

	public KryptonGridDecode newGrid(SectionGD1Type01Reader r) {
		if (r == null) throw new IllegalArgumentException("object is null");
		final double lat1 = r.latitude1();
		final double lon1 = r.longitude1();
		final double lat2 = r.latitude2();
		final double lon2 = r.longitude2();
		final double latin = r.latitudeCylinderIntersection();
		final double dx = r.DX();
		final double dy = r.DY();
		final KryptonArrayFactory af = new KryptonArrayFactory(r.NX(), r.NY());
		final CobaltGeoMercator geo = CobaltGeoMercator.newInstance(lat1, lon1, lat2, lon2, latin);
		final CobaltResolution resolution = CobaltResolution.newMetres(dy, dx);
		return new KryptonGridDecode(af, geo, resolution, null);
	}

	public KryptonGridDecode newGrid(SectionGD2Template00Reader r)
			throws KryptonCodeException {
		if (r == null) throw new IllegalArgumentException("object is null");
		final String Template = "3.0";
		final int resolutionFlags = r.resolutionFlags();
		final boolean resolutionGivenX = (resolutionFlags & 0x20) != 0;
		final boolean resolutionGivenY = (resolutionFlags & 0x10) != 0;
		if (!resolutionGivenX) {
			final String m = "No X (" + resolutionFlags + ")";
			throw new KryptonCodeException(CSection.GD2(Template, "resolutionFlags"), m);
		}
		if (!resolutionGivenY) {
			final String m = "No Y (" + resolutionFlags + ")";
			throw new KryptonCodeException(CSection.GD2(Template, "resolutionFlags"), m);
		}

		final double angleUnit = r.angleUnit();
		final double lat1 = angleUnit * r.latitude1();
		final double lon1 = angleUnit * r.longitude1();
		final double lat2 = angleUnit * r.latitude2();
		final double lon2 = angleUnit * r.longitude2();
		final int nx = r.NX();
		final int ny = r.NY();
		final double dx = angleUnit * r.DX();
		final double dy = angleUnit * r.DY();
		final KryptonArrayFactory af = new KryptonArrayFactory(nx, ny);
		final GridScan scan = new GridScan(af, r.scanningMode());
		final GeoProjectorLatitudeLongitude prj = new GeoProjectorLatitudeLongitude(scan, dx, dy, lat1, lon1, lat2, lon2, null);
		final CobaltGeoLatitudeLongitude geo = CobaltGeoLatitudeLongitude.newInstance(prj.nlat, prj.wlon, prj.slat, prj.elon);
		final CobaltResolution resolution = CobaltResolution.newDegrees(dy, dx);
		return new KryptonGridDecode(af, geo, resolution, prj);
	}

	public GridDecoder() {
	}
}
