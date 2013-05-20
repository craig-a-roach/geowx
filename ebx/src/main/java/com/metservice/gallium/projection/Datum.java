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

	public static final Datum D_Sphere = newInstance("D_Sphere", Ellipsoid.Sphere);
	public static final Datum D_Sphere_ARC_INFO = newInstance("D_Sphere_ARC_INFO", Ellipsoid.Sphere_ARC_INFO);
	public static final Datum D_WGS_1984 = newInstance("D_WGS_1984", Ellipsoid.WGS_1984);

	public static Datum createInstance(String fname, String ellipsoidName, double deltaX, double deltaY, double deltaZ) {
		final Ellipsoid oEllipsoid = EllipsoidDictionary.findByName(ellipsoidName);
		if (oEllipsoid == null) return null;
		final DualName name = DualName.newInstance(fname);
		return new Datum(name, oEllipsoid, deltaX, deltaY, deltaZ, null);
	}

	public static Datum newInstance(String fname, Ellipsoid ellipsoid) {
		if (ellipsoid == null) throw new IllegalArgumentException("object is null");
		final DualName name = DualName.newInstance(fname);
		return new Datum(name, ellipsoid, 0.0, 0.0, 0.0, null);
	}

	public static Datum newInstance(String fname, Ellipsoid ellipsoid, ParameterArray oTransform, Authority oAuthority) {
		if (ellipsoid == null) throw new IllegalArgumentException("object is null");
		final DualName name = DualName.newInstance(fname);
		double deltaX = 0.0;
		double deltaY = 0.0;
		double deltaZ = 0.0;
		if (oTransform != null) {
			deltaX = oTransform.select(0, 0.0);
			deltaY = oTransform.select(1, 0.0);
			deltaZ = oTransform.select(2, 0.0);
		}
		return new Datum(name, ellipsoid, deltaX, deltaY, deltaZ, oAuthority);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("name", name);
		ds.a("ellipsoid", ellipsoid);
		ds.a("deltaX", deltaX);
		ds.a("deltaY", deltaY);
		ds.a("deltaZ", deltaZ);
		ds.a("authority", oAuthority);
		return ds.s();
	}

	private Datum(DualName name, Ellipsoid ellipsoid, double deltaX, double deltaY, double deltaZ, Authority oAuthority) {
		assert name != null;
		assert ellipsoid != null;
		this.name = name;
		this.ellipsoid = ellipsoid;
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		this.deltaZ = deltaZ;
		this.oAuthority = oAuthority;
	}
	public final DualName name;
	public final Ellipsoid ellipsoid;
	public final double deltaX;
	public final double deltaY;
	public final double deltaZ;
	public final Authority oAuthority;
}
