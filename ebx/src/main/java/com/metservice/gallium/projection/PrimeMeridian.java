/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
class PrimeMeridian {

	public static final PrimeMeridian Greenwich = newInstance("Greenwich", 0.0, null);

	private static final PrimeMeridian[] Table = { Greenwich };

	public static PrimeMeridian findByName(String qcc) {
		if (qcc == null || qcc.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String oqcctw = ArgonText.oqtw(qcc);
		if (oqcctw == null) return null;
		for (int i = 0; i < Table.length; i++) {
			final PrimeMeridian pm = Table[i];
			if (pm.name.qcctwFullName().equals(qcc)) return pm;
			if (pm.name.qcctwShortName().equals(qcc)) return pm;
		}
		return null;
	}

	public static PrimeMeridian newInstance(DualName name, double longitude, Authority oAuthority) {
		if (name == null) throw new IllegalArgumentException("object is null");
		return new PrimeMeridian(name, longitude, oAuthority);
	}

	public static PrimeMeridian newInstance(String fname, double longitude, Authority oAuthority) {
		final DualName name = DualName.newInstance(fname);
		return newInstance(name, longitude, oAuthority);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" longitude ").append(longitude);
		if (oAuthority != null) {
			sb.append(" authority ").append(oAuthority);
		}
		return sb.toString();
	}

	private PrimeMeridian(DualName name, double longitude, Authority oAuthority) {
		assert name != null;
		this.name = name;
		this.longitude = longitude;
		this.oAuthority = oAuthority;
	}
	public final DualName name;
	public final double longitude;
	public final Authority oAuthority;
}
