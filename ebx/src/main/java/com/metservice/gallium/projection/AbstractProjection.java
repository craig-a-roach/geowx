/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.gallium.GalliumBoundingBoxF;
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

	private void transformInverseRadians(double srcXpu, double srcYpu, GalliumPointD.Builder dst)
			throws ProjectionException {
		if (dst == null) throw new IllegalArgumentException("object is null");
		final double x = (srcXpu - argBase.totalFalseEasting) / argBase.totalScale;
		final double y = (srcYpu - argBase.totalFalseNorthing) / argBase.totalScale;
		projectInverse(x, y, dst);
		final double xRads = MapMath.clamp(-Math.PI, dst.x, Math.PI);
		final double lonRads = argBase.projectionLongitudeRads;
		dst.x = lonRads == 0.0 ? xRads : MapMath.normalizeLongitude(xRads + lonRads);
	}

	private void transformNonrectilinear(GalliumBoundingBoxF src, GalliumBoundingBoxF.BuilderD dst)
			throws ProjectionException {
		assert src != null;
		assert dst != null;
		final float rx = src.xLo();
		final float ry = src.yLo();
		final float rw = src.width();
		final float rh = src.height();
		for (int ix = 0; ix < 7; ix++) {
			final double x = rx + rw * ix / 6;
			for (int iy = 0; iy < 7; iy++) {
				final double y = ry + rh * iy / 6;
				final GalliumPointD.Builder out = GalliumPointD.newBuilder();
				transform(x, y, out);
				if (ix == 0 && iy == 0) {
					dst.init(out.y, out.x);
				} else {
					dst.add(out.y, out.x);
				}
			}
		}
	}

	private void transformRectilinear(GalliumBoundingBoxF srcDeg, GalliumBoundingBoxF.BuilderD dst)
			throws ProjectionException {
		assert srcDeg != null;
		assert dst != null;
		final float rx = srcDeg.xLo();
		final float ry = srcDeg.yLo();
		final float rw = srcDeg.width();
		final float rh = srcDeg.height();
		for (int ix = 0; ix < 2; ix++) {
			final double x = rx + rw * ix;
			for (int iy = 0; iy < 2; iy++) {
				final double y = ry + rh * iy;
				final GalliumPointD.Builder out = GalliumPointD.newBuilder();
				transform(x, y, out);
				if (ix == 0 && iy == 0) {
					dst.init(out.y, out.x);
				} else {
					dst.add(out.y, out.x);
				}
			}
		}
	}

	protected abstract boolean inside(double lam, double phi)
			throws ProjectionException;

	protected abstract void project(double lam, double phi, GalliumPointD.Builder dst)
			throws ProjectionException;

	protected abstract void projectInverse(double x, double y, GalliumPointD.Builder dst)
			throws ProjectionException;

	public final Authority getAuthority() {
		return m_oAuthority;
	}

	@Override
	public GalliumPointD inverseDegrees(double xPU, double yPU)
			throws GalliumProjectionException {
		final GalliumPointD.Builder dst = GalliumPointD.newBuilder();
		transformInverse(xPU, yPU, dst);
		return new GalliumPointD(dst);
	}

	@Override
	public boolean isInside(double srcLonDeg, double srcLatDeg)
			throws GalliumProjectionException {
		try {
			final double yRads = srcLatDeg * DTR;
			if (yRads < -MapMath.HALFPI) return false;
			if (yRads > MapMath.HALFPI) return false;
			final double xRads = srcLonDeg * DTR;
			final double lonRads = argBase.projectionLongitudeRads;
			final double xNormRads = lonRads == 0.0 ? xRads : MapMath.normalizeLongitude(xRads - lonRads);
			return inside(xNormRads, yRads);
		} catch (final ProjectionException ex) {
			final String m = "Failed to determine inside lon " + srcLonDeg + ", lat " + srcLatDeg + " (deg)..."
					+ ex.getMessage();
			throw new GalliumProjectionException(m);
		}
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
	public final GalliumPointD transform(double srcLonDeg, double srcLatDeg)
			throws GalliumProjectionException {
		try {
			final GalliumPointD.Builder dst = GalliumPointD.newBuilder();
			transform(srcLonDeg, srcLatDeg, dst);
			return new GalliumPointD(dst);
		} catch (final ProjectionException ex) {
			final String m = "Failed to transform lon " + srcLonDeg + ", lat " + srcLatDeg + " (deg)..." + ex.getMessage();
			throw new GalliumProjectionException(m);
		}
	}

	public GalliumBoundingBoxF transform(GalliumBoundingBoxF srcDeg)
			throws GalliumProjectionException {
		if (srcDeg == null) throw new IllegalArgumentException("object is null");
		try {
			final GalliumBoundingBoxF.BuilderD bb = GalliumBoundingBoxF.newBuilderD();
			if (isRectilinear()) {
				transformRectilinear(srcDeg, bb);
			} else {
				transformNonrectilinear(srcDeg, bb);
			}
			return GalliumBoundingBoxF.newInstance(bb);
		} catch (final ProjectionException ex) {
			final String m = "Failed to transform bounding box " + srcDeg + " (deg)..." + ex.getMessage();
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
