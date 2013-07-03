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
class PrimeMeridian implements IWktEmit {

	public static final PrimeMeridian Greenwich = newInstance("Greenwich", 0.0, Authority.newEPSG(8901));

	private static final PrimeMeridian[] Table = { Greenwich };

	public static PrimeMeridian findByAuthority(Authority a) {
		if (a == null) throw new IllegalArgumentException("object is null");
		for (int i = 0; i < Table.length; i++) {
			final PrimeMeridian pm = Table[i];
			if (pm.oAuthority != null && pm.oAuthority.equals(a)) return pm;
		}
		return null;
	}

	public static PrimeMeridian findByTitle(String nc) {
		final Title target = Title.newInstance(nc);
		for (int i = 0; i < Table.length; i++) {
			final PrimeMeridian pm = Table[i];
			if (pm.title.equals(target)) return pm;
		}
		final Authority oAuth = Authority.createInstance(target);
		return oAuth == null ? null : findByAuthority(oAuth);
	}

	public static PrimeMeridian newInstance(String title, double longitude, Authority oAuthority) {
		return new PrimeMeridian(oAuthority, Title.newInstance(title), longitude);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("authority", oAuthority);
		ds.a("title", title);
		ds.a("longitude", longitude);
		return ds.ss();
	}

	@Override
	public WktStructure toWkt() {
		return new WktStructure("PRIMEM", title, longitude, oAuthority);
	}

	private PrimeMeridian(Authority oAuthority, Title title, double longitude) {
		assert title != null;
		this.oAuthority = oAuthority;
		this.title = title;
		this.longitude = longitude;
	}
	public final Authority oAuthority;
	public final Title title;
	public final double longitude;
}
