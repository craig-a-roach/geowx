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
class GeographicCoordinateSystem implements IGalliumGeographicCoordinateSystem {

	// GCS_NZGD_2000 (4167) D_NZGD_2000 (6167)
	// GCS_OSGB_1936 (4277) D_OSGB_1936 (6277)
	//
	// GCS_NAD_1983_NSRS2007 (4759) -> D_NAD_1983_NSRS2007 (6759)
	//
	//
	// GCS_Australian_1984 (4203) D_Australian_1984 (6203)
	//
	//
	// GCS_Sphere (4035) D_Sphere (6035)
	// GCS_WGS_1984 (4326) D_WGS_1984 (6326)

	public static final GeographicCoordinateSystem GCS_Sphere = newInstance("GCS_Sphere", Datum.D_Sphere,
			PrimeMeridian.Greenwich, Unit.DEGREES, Authority.newEPSG(4035));
	public static final GeographicCoordinateSystem GCS_WGS84 = newInstance("GCS_WGS_1984", Datum.D_WGS_1984,
			PrimeMeridian.Greenwich, Unit.DEGREES, Authority.newEPSG(4326));

	public static GeographicCoordinateSystem createGreenwichDegrees(String qccName, String datumName, Authority oAuthority) {
		final Datum oDatum = DatumDictionary.findByName(datumName);
		if (oDatum == null) return null;
		final DualName name = DualName.newInstance(qccName);
		return new GeographicCoordinateSystem(name, oDatum, PrimeMeridian.Greenwich, Unit.DEGREES, oAuthority);
	}

	public static GeographicCoordinateSystem newInstance(String qccName, Datum datum, PrimeMeridian primeMeridian,
			Unit angularUnit, Authority oAuthority) {
		if (qccName == null || qccName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (datum == null) throw new IllegalArgumentException("object is null");
		if (primeMeridian == null) throw new IllegalArgumentException("object is null");
		if (angularUnit == null) throw new IllegalArgumentException("object is null");
		final DualName name = DualName.newInstance(qccName);
		return new GeographicCoordinateSystem(name, datum, primeMeridian, angularUnit, oAuthority);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("name", name);
		ds.a("datum", datum);
		ds.a("primeMeridian", primeMeridian);
		ds.a("angularUnit", angularUnit);
		ds.a("authority", oAuthority);
		return ds.s();
	}

	private GeographicCoordinateSystem(DualName name, Datum datum, PrimeMeridian primeMeridian, Unit angularUnit,
			Authority oAuthority) {
		assert name != null;
		assert datum != null;
		assert primeMeridian != null;
		assert angularUnit != null;
		this.name = name;
		this.datum = datum;
		this.primeMeridian = primeMeridian;
		this.angularUnit = angularUnit;
		this.oAuthority = oAuthority;
	}
	public final DualName name;
	public final Datum datum;
	public final PrimeMeridian primeMeridian;
	public final Unit angularUnit;
	public final Authority oAuthority;
}
