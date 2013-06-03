/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class Datum {

	public static final Datum D_Sphere = newEpsg(6035, "D_Sphere", Ellipsoid.Sphere, null);
	public static final Datum D_WGS_1984 = newEpsg(6326, "D_WGS_1984", Ellipsoid.WGS_1984, GeocentricTranslation.Zero);

	public static Datum newEpsg(int code, String title, Ellipsoid ellipsoid, IDatumTransform oToWgs84) {
		if (ellipsoid == null) throw new IllegalArgumentException("object is null");
		return new Datum(Authority.newEPSG(code), Title.newInstance(title), ellipsoid, oToWgs84);
	}

	public static Datum newEpsg(int code, String title, int codeEllipsoid, IDatumTransform oToWgs84) {
		final Ellipsoid ellipsoid = EllipsoidDictionary.selectByAuthority(Authority.newEPSG(codeEllipsoid));
		return newEpsg(code, title, ellipsoid, oToWgs84);
	}

	public static Datum newInstance(String title, Ellipsoid ellipsoid, IDatumTransform oToWgs84, Authority oAuthority) {
		if (ellipsoid == null) throw new IllegalArgumentException("object is null");
		return new Datum(oAuthority, Title.newInstance(title), ellipsoid, oToWgs84);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("authority", oAuthority);
		ds.a("title", title);
		ds.a("ellipsoid", ellipsoid);
		ds.a("toWgs84", oToWgs84);
		return ds.s();
	}

	private Datum(Authority oAuthority, Title title, Ellipsoid ellipsoid, IDatumTransform oToWgs84) {
		assert title != null;
		assert ellipsoid != null;
		this.oAuthority = oAuthority;
		this.title = title;
		this.ellipsoid = ellipsoid;
		this.oToWgs84 = oToWgs84;
	}
	public final Authority oAuthority;
	public final Title title;
	public final Ellipsoid ellipsoid;
	public final IDatumTransform oToWgs84;
}
