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

	public static final Datum D_Sphere = newInstance("D_Sphere", Ellipsoid.Sphere, null);
	public static final Datum D_Sphere_ARC_INFO = newInstance("D_Sphere_ARC_INFO", Ellipsoid.Sphere_ARC_INFO, null);
	public static final Datum D_WGS_1984 = newInstance("D_WGS_1984", Ellipsoid.WGS_1984, DatumTransform.Zero);

	public static Datum createInstance(String fname, String ellipsoidName, DatumTransform oToWgs84) {
		final Ellipsoid oEllipsoid = EllipsoidDictionary.findByName(ellipsoidName);
		if (oEllipsoid == null) return null;
		final DualName name = DualName.newInstance(fname);
		return new Datum(name, oEllipsoid, oToWgs84, null);
	}

	public static Datum newInstance(String fname, Ellipsoid ellipsoid, DatumTransform oToWgs84) {
		if (ellipsoid == null) throw new IllegalArgumentException("object is null");
		final DualName name = DualName.newInstance(fname);
		return new Datum(name, ellipsoid, oToWgs84, null);
	}

	public static Datum newInstance(String fname, Ellipsoid ellipsoid, ParameterArray oParamsToWgs84, Authority oAuthority) {
		if (ellipsoid == null) throw new IllegalArgumentException("object is null");
		final DualName name = DualName.newInstance(fname);
		final DatumTransform oToWgs84 = oParamsToWgs84 == null ? null : DatumTransform.newInstance(oParamsToWgs84);
		return new Datum(name, ellipsoid, oToWgs84, oAuthority);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("name", name);
		ds.a("ellipsoid", ellipsoid);
		ds.a("toWgs84", oToWgs84);
		ds.a("authority", oAuthority);
		return ds.s();
	}

	private Datum(DualName name, Ellipsoid ellipsoid, DatumTransform oToWgs84, Authority oAuthority) {
		assert name != null;
		assert ellipsoid != null;
		this.name = name;
		this.ellipsoid = ellipsoid;
		this.oToWgs84 = oToWgs84;
		this.oAuthority = oAuthority;
	}
	public final DualName name;
	public final Ellipsoid ellipsoid;
	public final DatumTransform oToWgs84;
	public final Authority oAuthority;
}
