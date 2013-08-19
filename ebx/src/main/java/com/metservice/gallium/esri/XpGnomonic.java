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
class XpGnomonic extends AbstractProjection {

	@Override
	protected void project(final double lam, final double phi, Builder dst)
			throws ProjectionException {
		m_mode.project(lam, phi, dst);
	}

	@Override
	protected void projectInverse(double x, double y, Builder dst)
			throws ProjectionException {
		final double rh = MapMath.hypot(x, y);
		if (Math.abs(rh) <= EPS10) {
			dst.y = m_phi0;
			dst.x = 0.0;
		} else {
			final double atrh = Math.atan(rh);
			final double sinz = Math.sin(atrh);
			final double cosz = Math.sqrt(1.0 - (sinz * sinz));
			m_mode.projectInverse(x, y, dst, rh, atrh, sinz, cosz);
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

	public XpGnomonic(Authority oAuthority, Title title, ArgBase argBase, XaGnomonic arg) {
		super(oAuthority, title, argBase);
		m_phi0 = arg.projectionLatitudeRads();
		final Mode mode;
		if (arg instanceof XaGnomonicPolar) {
			final XaGnomonicPolar xa = (XaGnomonicPolar) arg;
			mode = new ModePolar(xa);
		} else if (arg instanceof XaGnomonicEquator) {
			mode = new ModeEquator();
		} else if (arg instanceof XaGnomonicOblique) {
			final XaGnomonicOblique xa = (XaGnomonicOblique) arg;
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

		protected final void setLam(double rx, double ry, Builder dst) {
			if (ry == 0.0) {
				dst.x = rx == 0.0 ? 0.0 : (rx < 0.0 ? -MapMath.HALFPI : MapMath.HALFPI);
			} else {
				dst.x = MapMath.atan2(rx, ry);
			}
		}

		protected final void setPhi(double phi, Builder dst) {
			dst.y = phi;
		}

		public abstract void project(final double lam, final double phi, Builder dst)
				throws ProjectionException;

		public abstract void projectInverse(double x, double y, Builder dst, double rh, double atrh, double sinz, double cosz)
				throws ProjectionException;

		protected Mode() {
		}
	}

	private static class ModeEquator extends Mode {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double coslam = Math.cos(lam);
			final double sinlam = Math.sin(lam);
			final double sinphi = Math.sin(phi);
			final double cosphi = Math.cos(phi);
			final double bc = cosphi * coslam;
			if (bc <= EPS10) throw ProjectionException.coordinateOutsideBounds();
			final double bcr = 1.0 / bc;
			dst.y = bcr * sinphi;
			dst.x = bcr * cosphi * sinlam;
		}

		@Override
		public void projectInverse(double x, double y, Builder dst, double rh, double atrh, double sinz, double cosz) {
			double sinphi = y * sinz / rh;
			final double phi;
			if (Math.abs(sinphi) >= 1.0) {
				if (sinphi > 0.0) {
					phi = MapMath.HALFPI;
					sinphi = 1.0;
				} else {
					phi = -MapMath.HALFPI;
					sinphi = -1.0;
				}
			} else {
				phi = Math.asin(sinphi);
			}
			final double ry = cosz * rh;
			final double rx = x * sinz;
			setPhi(phi, dst);
			setLam(rx, ry, dst);
		}

		public ModeEquator() {
		}
	}

	private static class ModeOblique extends Mode {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double sinlam = Math.sin(lam);
			final double cosphi = Math.cos(phi);
			final double bc = (m_sinph0 * sinphi) + (m_cosph0 * cosphi * coslam);
			if (bc <= EPS10) throw ProjectionException.coordinateOutsideBounds();
			final double bcr = 1.0 / bc;
			dst.y = bcr * ((m_cosph0 * sinphi) - (m_sinph0 * cosphi * coslam));
			dst.x = bcr * cosphi * sinlam;
		}

		@Override
		public void projectInverse(final double x, final double y, Builder dst, double rh, double atrh, double sinz, double cosz)
				throws ProjectionException {
			double sinphi = cosz * m_sinph0 + (y * sinz * m_cosph0 / rh);
			final double phi;
			if (Math.abs(sinphi) >= 1.0) {
				if (sinphi > 0.0) {
					phi = MapMath.HALFPI;
					sinphi = 1.0;
				} else {
					phi = -MapMath.HALFPI;
					sinphi = -1.0;
				}
			} else {
				phi = Math.asin(sinphi);
			}
			final double ry = (cosz - (m_sinph0 * sinphi)) * rh;
			final double rx = x * sinz * m_cosph0;
			setPhi(phi, dst);
			setLam(rx, ry, dst);
		}

		public ModeOblique(XaGnomonicOblique xa) {
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
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double sinlam = Math.sin(lam);
			final double cosphi = Math.cos(phi);
			final double bc = m_north ? sinphi : -sinphi;
			if (bc <= EPS10) throw ProjectionException.coordinateOutsideBounds();
			final double bcrcp = 1.0 / bc * cosphi;
			final double cl = m_north ? -coslam : coslam;
			dst.y = bcrcp * cl;
			dst.x = bcrcp * sinlam;
		}

		@Override
		public void projectInverse(double x, double y, Builder dst, double rh, double atrh, double sinz, double cosz) {
			final double phi;
			final double ry;
			if (m_north) {
				phi = MapMath.HALFPI - atrh;
				ry = -y;
			} else {
				phi = atrh - MapMath.HALFPI;
				ry = y;
			}
			setPhi(phi, dst);
			setLam(x, ry, dst);
		}

		public ModePolar(XaGnomonicPolar xa) {
			m_north = xa.north;
		}
		private final boolean m_north;
	}
}
