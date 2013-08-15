/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import com.metservice.gallium.GalliumPointD.Builder;

/**
 * @author roach
 */
class XpOrthographic extends AbstractProjection {

	@Override
	protected void project(final double lam, final double phi, Builder dst)
			throws ProjectionException {
		m_mode.project(lam, phi, dst);
	}

	@Override
	protected void projectInverse(double x, double y, Builder dst)
			throws ProjectionException {
		final double rh = MapMath.hypot(x, y);
		double sinc = rh;
		if (sinc > 1.0) {
			if ((sinc - 1.0) > EPS10) throw ProjectionException.coordinateOutsideBounds();
			sinc = 1.0;
		}
		if (Math.abs(rh) <= EPS10) {
			dst.y = m_phi0;
			dst.x = 0.0;
		} else {
			final double cosc = Math.sqrt(1.0 - (sinc * sinc));
			m_mode.projectInverse(x, y, dst, rh, sinc, cosc);
		}
	}

	@Override
	public boolean hasInverse() {
		return true;
	}

	@Override
	public boolean isRectilinear() {
		return false;
	}

	public XpOrthographic(Authority oAuthority, Title title, ArgBase argBase, XaOrthographic arg) {
		super(oAuthority, title, argBase);
		m_phi0 = arg.projectionLatitudeRads();
		final Mode mode;
		if (arg instanceof XaOrthographicPolar) {
			final XaOrthographicPolar xa = (XaOrthographicPolar) arg;
			mode = new ModePolar(xa);
		} else if (arg instanceof XaOrthographicEquator) {
			mode = new ModeEquator();
		} else if (arg instanceof XaOrthographicOblique) {
			final XaOrthographicOblique xa = (XaOrthographicOblique) arg;
			mode = new ModeOblique(xa);
		} else {
			final String m = "Unsupported projection mode " + arg.getClass().getName();
			throw new IllegalStateException(m);
		}
		m_mode = mode;
	}
	private final double m_phi0;
	private final Mode m_mode;

	private static abstract class Mode {

		protected final void setObliqueX(double rx, double ry, Builder dst) {
			if (ry == 0.0) {
				dst.x = rx == 0.0 ? 0.0 : (rx < 0.0 ? -MapMath.HALFPI : MapMath.HALFPI);
			} else {
				dst.x = MapMath.atan2(rx, ry);
			}
		}

		protected final void setObliqueY(double siny, Builder dst) {
			if (Math.abs(siny) >= 1.0) {
				dst.y = siny < 0.0 ? -MapMath.HALFPI : MapMath.HALFPI;
			} else {
				dst.y = MapMath.asin(siny);
			}
		}

		public abstract void project(final double lam, final double phi, Builder dst)
				throws ProjectionException;

		public abstract void projectInverse(double x, double y, Builder dst, double rh, double sinc, double cosc)
				throws ProjectionException;

		protected Mode() {
		}
	}

	private static class ModeEquator extends Mode {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double sinlam = Math.sin(lam);
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double cosphi = Math.cos(phi);
			final double cospl = cosphi * coslam;
			if (cospl < -EPS10) throw ProjectionException.coordinateOutsideBounds();
			dst.y = sinphi;
			dst.x = cosphi * sinlam;
		}

		@Override
		public void projectInverse(double x, double y, Builder dst, double rh, double sinc, double cosc) {
			final double siny = y * sinc / rh;
			final double ry = cosc * rh;
			final double rx = x * sinc;
			setObliqueY(siny, dst);
			setObliqueX(rx, ry, dst);
		}

		public ModeEquator() {
		}
	}

	private static class ModeOblique extends Mode {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double sinlam = Math.sin(lam);
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double cosphi = Math.cos(phi);
			final double cospl = cosphi * coslam;
			final double bc = (m_sinph0 * sinphi) + (m_cosph0 * cospl);
			if (bc < -EPS10) throw ProjectionException.coordinateOutsideBounds();
			dst.y = (m_cosph0 * sinphi) - (m_sinph0 * cospl);
			dst.x = cosphi * sinlam;
		}

		@Override
		public void projectInverse(final double x, final double y, Builder dst, double rh, double sinc, double cosc)
				throws ProjectionException {
			final double siny = (cosc * m_sinph0) + (y * sinc * m_cosph0 / rh);
			final double ry = (cosc - m_sinph0 * siny) * rh;
			final double rx = x * sinc * m_cosph0;
			setObliqueY(siny, dst);
			setObliqueX(rx, ry, dst);
		}

		public ModeOblique(XaOrthographicOblique xa) {
			final double phi0 = xa.projectionLatitudeRads;
			m_sinph0 = Math.sin(phi0);
			m_cosph0 = Math.cos(phi0);
		}
		private final double m_sinph0;
		private final double m_cosph0;
	}

	private static class ModePolar extends Mode {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double bc = Math.abs(phi - m_phi0) - EPS10;
			if (bc > MapMath.HALFPI) throw ProjectionException.coordinateOutsideBounds();
			final double sinlam = Math.sin(lam);
			final double coslam = m_north ? -Math.cos(lam) : Math.cos(lam);
			final double cosphi = Math.cos(phi);
			dst.y = cosphi * coslam;
			dst.x = cosphi * sinlam;
		}

		@Override
		public void projectInverse(double x, double y, Builder dst, double rh, double sinc, double cosc) {
			if (m_north) {
				dst.y = MapMath.acos(sinc);
				dst.x = MapMath.atan2(x, -y);
			} else {
				dst.y = -MapMath.acos(sinc);
				dst.x = MapMath.atan2(x, y);
			}
		}

		public ModePolar(XaOrthographicPolar xa) {
			m_north = xa.north;
			m_phi0 = xa.projectionLatitudeRads();
		}
		private final boolean m_north;
		private final double m_phi0;
	}
}
