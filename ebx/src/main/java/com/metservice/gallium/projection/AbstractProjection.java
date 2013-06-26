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

	protected static final double EPS10 = 1e-10;
	protected static final double RTD = MapMath.RTD;
	protected static final double DTR = MapMath.DTR;

	private void transform(double srcXdeg, double srcYdeg, GalliumPointD.Builder dst)
			throws ProjectionException {
		if (dst == null) throw new IllegalArgumentException("object is null");
		final double xRads = srcXdeg * DTR;
		final double yRads = srcYdeg * DTR;
		final double lonRads = argBase.projectionLongitudeRads;
		final double xNormRads = lonRads == 0.0 ? xRads : MapMath.normalizeLongitude(xRads - lonRads);
		project(xNormRads, yRads, dst);
		dst.x = (argBase.totalScale * dst.x) + argBase.totalFalseEasting;
		dst.y = (argBase.totalScale * dst.y) + argBase.totalFalseNorthing;
	}

	protected abstract void project(double x, double y, GalliumPointD.Builder dst)
			throws ProjectionException;

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
		try {
			final GalliumPointD.Builder dst = GalliumPointD.newBuilder();
			transform(lonDeg, latDeg, dst);
			return new GalliumPointD(dst);
		} catch (final ProjectionException ex) {
			final String m = "Failed to transform lon " + lonDeg + ", lat " + latDeg + " (deg)..." + ex.getMessage();
			throw new GalliumProjectionException(m);
		}
	}

	protected AbstractProjection(Authority oAuthority, Title title, ArgBase argBase) {
		if (title == null) throw new IllegalArgumentException("object is null");
		if (argBase == null) throw new IllegalArgumentException("object is null");
		m_oAuthority = oAuthority;
		m_title = title;
		this.argBase = argBase;
	}
	private final Authority m_oAuthority;
	private final Title m_title;
	protected final ArgBase argBase;
}
