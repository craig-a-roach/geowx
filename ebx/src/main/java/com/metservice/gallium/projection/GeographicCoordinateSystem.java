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
class GeographicCoordinateSystem {

	public static final GeographicCoordinateSystem GCS_Sphere = newGreenwichDegreesEpsg(4035, "GCS_Sphere", Datum.D_Sphere);
	public static final GeographicCoordinateSystem GCS_WGS84 = newGreenwichDegreesEpsg(4326, "GCS_WGS_1984", Datum.D_WGS_1984);

	public static GeographicCoordinateSystem newGreenwichDegreesEpsg(int code, String title, Datum datum) {
		return new GeographicCoordinateSystem(Authority.newEPSG(code), Title.newInstance(title), datum,
				PrimeMeridian.Greenwich, Unit.DEGREES);
	}

	public static GeographicCoordinateSystem newGreenwichDegreesEpsg(int code, String title, int codeDatum) {
		final Datum datum = DatumDictionary.selectByAuthority(Authority.newEPSG(codeDatum));
		return newGreenwichDegreesEpsg(code, title, datum);
	}

	public static GeographicCoordinateSystem newInstance(String title, Datum datum, PrimeMeridian primeMeridian,
			Unit angularUnit, Authority oAuthority) {
		if (datum == null) throw new IllegalArgumentException("object is null");
		if (primeMeridian == null) throw new IllegalArgumentException("object is null");
		if (angularUnit == null) throw new IllegalArgumentException("object is null");
		return new GeographicCoordinateSystem(oAuthority, Title.newInstance(title), datum, primeMeridian, angularUnit);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("authority", oAuthority);
		ds.a("title", title);
		ds.a("datum", datum);
		ds.a("primeMeridian", primeMeridian);
		ds.a("angularUnit", angularUnit);
		return ds.s();
	}

	private GeographicCoordinateSystem(Authority oAuthority, Title title, Datum datum, PrimeMeridian primeMeridian,
			Unit angularUnit) {
		assert title != null;
		assert datum != null;
		assert primeMeridian != null;
		assert angularUnit != null;
		this.oAuthority = oAuthority;
		this.title = title;
		this.datum = datum;
		this.primeMeridian = primeMeridian;
		this.angularUnit = angularUnit;
	}
	public final Authority oAuthority;
	public final Title title;
	public final Datum datum;
	public final PrimeMeridian primeMeridian;
	public final Unit angularUnit;
}
