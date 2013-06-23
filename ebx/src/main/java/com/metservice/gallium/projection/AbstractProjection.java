/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
abstract class AbstractProjection implements IGalliumProjection {

	public final Authority getAuthority() {
		return m_oAuthority;
	}

	public final Title title() {
		return m_title;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_title);
		if (m_oAuthority != null) {
			sb.append(" (");
			sb.append(m_oAuthority);
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public final GalliumPointD transform(double lonDeg, double latDeg)
			throws GalliumProjectionException {
		final GalliumPointD.Builder dst = GalliumPointD.newBuilder();
		transform(lonDeg, latDeg, dst);
		return new GalliumPointD(dst);
	}

	protected AbstractProjection(Authority oAuthority, Title title) {
		if (title == null) throw new IllegalArgumentException("object is null");
		m_oAuthority = oAuthority;
		m_title = title;
	}
	private final Authority m_oAuthority;
	private final Title m_title;
}
