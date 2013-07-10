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

	protected static final double EPS10 = MapMath.EPS10;
	protected static final double RTD = MapMath.RTD;
	protected static final double DTR = MapMath.DTR;

	private static void rtd(GalliumPointD.Builder dst) {
		assert dst != null;
		dst.x = dst.x * RTD;
		dst.y = dst.y * RTD;
	}

	private double longitudeAbsolute(double radsRel)
			throws ProjectionException {
		final double radsRef = argBase.projectionLongitudeRads;
		if (radsRef == 0.0) return radsRel;
		return MapMath.normalizeLongitude(radsRef + radsRel);
	}

	private double longitudeRelative(double radsAbs)
			throws ProjectionException {
		final double radsRef = argBase.projectionLongitudeRads;
		if (radsRef == 0.0) return radsAbs;
		return MapMath.normalizeLongitude(radsAbs - radsRef);
	}

	private void transform(double srcXdeg, double srcYdeg, GalliumPointD.Builder dst)
			throws ProjectionException {
		if (dst == null) throw new IllegalArgumentException("object is null");
		final double xRads = longitudeRelative(srcXdeg * DTR);
		final double yRads = srcYdeg * DTR;
		project(xRads, yRads, dst);
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
		final double yRads = MapMath.clamp(-MapMath.HALFPI, dst.y, MapMath.HALFPI);
		dst.x = longitudeAbsolute(xRads);
		dst.y = yRads;
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
		try {
			transformInverseRadians(xPU, yPU, dst);
			rtd(dst);
			return new GalliumPointD(dst);
		} catch (final ProjectionException ex) {
			final Unit pu = argBase.projectedUnit;
			final String m = "Failed to determine inverse of x=" + xPU + ", y=" + yPU + " (" + pu + ")..." + ex.getMessage();
			throw new GalliumProjectionException(m);
		}
	}

	@Override
	public boolean isInside(double srcLonDeg, double srcLatDeg)
			throws GalliumProjectionException {
		try {
			final double yRads = srcLatDeg * DTR;
			if (yRads < -MapMath.HALFPI) return false;
			if (yRads > MapMath.HALFPI) return false;
			final double xRads = longitudeRelative(srcLonDeg * DTR);
			return inside(xRads, yRads);
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
	public double totalScale() {
		return argBase.totalScale;
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
			final GalliumBoundingBoxF.BuilderD dst = GalliumBoundingBoxF.newBuilderD();
			final boolean isRectlinear = isRectilinear();
			final float rxDeg = srcDeg.xLo();
			final float ryDeg = srcDeg.yLo();
			final float rwDeg = srcDeg.width();
			final float rhDeg = srcDeg.height();
			final int imax = isRectlinear ? 1 : 6;
			for (int ix = 0; ix <= imax; ix++) {
				final double x = rxDeg + rwDeg * ix / imax;
				for (int iy = 0; iy <= imax; iy++) {
					final double y = ryDeg + rhDeg * iy / imax;
					final GalliumPointD.Builder out = GalliumPointD.newBuilder();
					transform(x, y, out);
					if (ix == 0 && iy == 0) {
						dst.init(out.y, out.x);
					} else {
						dst.add(out.y, out.x);
					}
				}
			}
			return GalliumBoundingBoxF.newInstance(dst);
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
