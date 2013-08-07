/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.gallium.GalliumPointD.Builder;

/**
 * @author roach
 */
class XpStereographic extends AbstractProjection {

	private static final int NITER = 8;
	private static final double CONV = MapMath.EPS10;

	private static double ssfn(final double phit, final double sinphi, double e) {
		final double sinphie = sinphi * e;
		final double sinphier = (1.0 - sinphie) / (1.0 + sinphie);
		return Math.tan(0.5 * (MapMath.HALFPI + phit)) * Math.pow(sinphier, 0.5 * e);
	}

	@Override
	protected void project(final double lam, final double phi, Builder dst)
			throws ProjectionException {
		m_mode.project(lam, phi, dst);
	}

	@Override
	protected void projectInverse(double x, double y, Builder dst)
			throws ProjectionException {
		m_mode.projectInverse(x, y, dst);
	}

	@Override
	public boolean hasInverse() {
		return true;
	}

	@Override
	public boolean isRectilinear() {
		return false;
	}

	public XpStereographic(Authority oAuthority, Title title, ArgBase argBase, XaStereographic arg) {
		super(oAuthority, title, argBase);
		final boolean spherical = argBase.spherical;
		final Mode mode;
		if (arg instanceof XaStereographicPolar) {
			final XaStereographicPolar xa = (XaStereographicPolar) arg;
			if (spherical) {
				mode = new ModeSphericalPolar(argBase, xa);
			} else {
				mode = new ModeEllipsoidPolar(argBase, xa);
			}
		} else if (arg instanceof XaStereographicEquator) {
			final XaStereographicEquator xa = (XaStereographicEquator) arg;
			if (spherical) {
				mode = new ModeSphericalEquator(argBase, xa);
			} else {
				mode = new ModeEllipsoidEquator(argBase, xa);
			}
		} else if (arg instanceof XaStereographicOblique) {
			final XaStereographicOblique xa = (XaStereographicOblique) arg;
			if (spherical) {
				mode = new ModeSphericalOblique(argBase, xa);
			} else {
				mode = new ModeEllipsoidOblique(argBase, xa);
			}
		} else {
			final String m = "Unsupported projection mode " + arg.getClass().getName();
			throw new IllegalStateException(m);
		}
		m_mode = mode;
	}
	private final Mode m_mode;

	private static interface Mode {

		void project(final double lam, final double phi, Builder dst)
				throws ProjectionException;

		void projectInverse(double x, double y, Builder dst)
				throws ProjectionException;
	}

	private static abstract class ModeEllipsoid implements Mode {

		protected final void solveInverse(final double x, final double y, Builder dst, final double rho, final double tp,
				final double phi, final double halfe, double halfpi, boolean posphi)
				throws ProjectionException {
			double phiEx = phi;
			double delta = Double.NaN;
			int iter = 0;
			while (iter < NITER) {
				final double sinphi = m_e * Math.sin(phiEx);
				final double rsinphi = (1.0 + sinphi) / (1.0 - sinphi);
				final double phiNeo = (2.0 * Math.atan(tp * Math.pow(rsinphi, halfe))) - halfpi;
				delta = Math.abs(phiNeo - phiEx);
				if (delta < CONV) {
					dst.x = (x == 0.0 && y == 0.0) ? 0.0 : MapMath.atan2(x, y);
					dst.y = posphi ? phiNeo : -phiNeo;
					return;
				}
				phiEx = phiNeo;
				iter++;
			}
			throw ProjectionException.noConverge(iter, delta);
		}

		protected ModeEllipsoid(ArgBase argBase) {
			assert argBase != null;
			m_e = argBase.e;
		}
		protected final double m_e;
	}

	private static class ModeEllipsoidEquator extends ModeEllipsoid {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double sinlam = Math.sin(lam);
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double ssfn = ssfn(phi, sinphi, m_e);
			final double X = 2.0 * Math.atan(ssfn) - MapMath.HALFPI;
			final double sinX = Math.sin(X);
			final double cosX = Math.cos(X);
			final double aa = 1.0 + (cosX * coslam);
			if (!MapMath.validDivisorEPS8(aa)) throw ProjectionException.coordinateOutsideBounds();
			final double A = m_akm1 / aa;
			dst.x = A * cosX * sinlam;
			dst.y = A * sinX;
		}

		@Override
		public void projectInverse(double x, double y, Builder dst)
				throws ProjectionException {
			final double rho = MapMath.hypot(x, y);
			final double atr = 2.0 * MapMath.atan2(rho, m_akm1);
			final double cosatr = Math.cos(atr);
			final double sinatr = Math.sin(atr);
			final double sinphi;
			if (rho == 0.0) {
				sinphi = 0.0;
			} else {
				sinphi = y * sinatr / rho;
			}
			final double phi = MapMath.asin(sinphi);
			final double tp = Math.tan(0.5 * (MapMath.HALFPI + phi));
			final double xs = x * sinatr;
			final double ys = rho * cosatr;
			final double halfe = 0.5 * m_e;
			final double halfpi = MapMath.HALFPI;
			solveInverse(xs, ys, dst, rho, tp, phi, halfe, halfpi, true);
		}

		public ModeEllipsoidEquator(ArgBase argBase, XaStereographicEquator xa) {
			super(argBase);
			m_akm1 = 2.0 * xa.scaleFactor;
		}
		private final double m_akm1;
	}

	private static class ModeEllipsoidOblique extends ModeEllipsoid {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double sinlam = Math.sin(lam);
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double ssfn = ssfn(phi, sinphi, m_e);
			final double X = 2.0 * Math.atan(ssfn) - MapMath.HALFPI;
			final double sinX = Math.sin(X);
			final double cosX = Math.cos(X);
			final double aa = m_cosX * (1.0 + (m_sinX * sinX) + (m_cosX * cosX * coslam));
			if (!MapMath.validDivisorEPS8(aa)) throw ProjectionException.coordinateOutsideBounds();
			final double A = m_akm1 / aa;
			dst.x = A * cosX * sinlam;
			dst.y = A * ((m_cosX * sinX) - (m_sinX * cosX * coslam));
		}

		@Override
		public void projectInverse(final double x, final double y, Builder dst)
				throws ProjectionException {
			final double rho = MapMath.hypot(x, y);
			final double atr = 2.0 * MapMath.atan2(rho * m_cosX, m_akm1);
			final double cosatr = Math.cos(atr);
			final double sinatr = Math.sin(atr);
			final double sinphi;
			if (rho == 0.0) {
				sinphi = cosatr * m_sinX;
			} else {
				sinphi = (cosatr * m_sinX) + (y * sinatr * m_cosX / rho);
			}
			final double phi = MapMath.asin(sinphi);
			final double tp = Math.tan(0.5 * (MapMath.HALFPI + phi));
			final double xs = x * sinatr;
			final double ys = (rho * m_cosX * cosatr) - (y * m_sinX * sinatr);
			final double halfe = 0.5 * m_e;
			final double halfpi = MapMath.HALFPI;
			solveInverse(xs, ys, dst, rho, tp, phi, halfe, halfpi, true);
		}

		public ModeEllipsoidOblique(ArgBase argBase, XaStereographicOblique xa) {
			super(argBase);
			final double e = argBase.e;
			final double phi0 = xa.projectionLatitudeRads;
			final double sinphi0 = Math.sin(phi0);
			final double cosphi0 = Math.cos(phi0);
			final double ssfn = ssfn(phi0, sinphi0, e);
			final double X = 2.0 * Math.atan(ssfn) - MapMath.HALFPI;
			final double te = sinphi0 * e;
			m_akm1 = 2.0 * xa.scaleFactor * cosphi0 / Math.sqrt(1.0 - te * te);
			m_sinX = Math.sin(X);
			m_cosX = Math.cos(X);
		}
		private final double m_akm1;
		private final double m_sinX;
		private final double m_cosX;
	}

	private static class ModeEllipsoidPolar extends ModeEllipsoid {

		@Override
		public void project(double lam, double phi, Builder dst)
				throws ProjectionException {
			final double sinlam = Math.sin(lam);
			final double coslam = m_north ? Math.cos(lam) : -Math.cos(lam);
			final double sinphi = m_north ? Math.sin(phi) : -Math.sin(phi);
			final double sphi = m_north ? phi : -phi;
			final double t = m_akm1 * MapMath.tsfn(sphi, sinphi, m_e);
			dst.x = t * sinlam;
			dst.y = -t * coslam;
		}

		@Override
		public void projectInverse(double x, double y, Builder dst)
				throws ProjectionException {
			final double sy = m_north ? -y : y;
			final double rho = MapMath.hypot(x, y);
			final double tp = -rho / m_akm1;
			final double phi = MapMath.HALFPI - (2.0 * Math.atan(tp));
			final double halfe = -0.5 * m_e;
			final double halfpi = -MapMath.HALFPI;
			final boolean posphi = m_north;
			solveInverse(x, sy, dst, rho, tp, phi, halfe, halfpi, posphi);
		}

		public ModeEllipsoidPolar(ArgBase argBase, XaStereographicPolar xa) {
			super(argBase);
			final double e = argBase.e;
			final double scaleFactor = xa.scaleFactor;
			final double phits = Math.abs(xa.trueScaleLatitudeAbsRads);
			if (Math.abs(phits - MapMath.HALFPI) < EPS10) {
				m_akm1 = 2.0 * scaleFactor / Math.sqrt(Math.pow(1 + e, 1 + e) * Math.pow(1 - e, 1 - e));
			} else {
				final double costs = Math.cos(phits);
				final double sints = Math.sin(phits);
				final double tsfn = MapMath.tsfn(phits, sints, e);
				final double sinte = sints * e;
				m_akm1 = costs / tsfn / Math.sqrt(1.0 - (sinte * sinte));
			}
			m_north = xa.north;
		}
		private final boolean m_north;
		private final double m_akm1;
	}

	private static abstract class ModeSpherical implements Mode {

		protected ModeSpherical() {
		}
	}

	private static class ModeSphericalEquator extends ModeSpherical {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double sinlam = Math.sin(lam);
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double cosphi = Math.cos(phi);
			final double aa = 1.0 + (cosphi * coslam);
			if (!MapMath.validDivisorEPS8(aa)) throw ProjectionException.coordinateOutsideBounds();
			final double A = m_akm1 / aa;
			dst.x = A * cosphi * sinlam;
			dst.y = A * sinphi;
		}

		@Override
		public void projectInverse(double x, double y, Builder dst) {
			final double rh = MapMath.hypot(x, y);
			final double c = 2.0 * Math.atan(rh / m_akm1);
			final double sinc = Math.sin(c);
			final double cosc = Math.cos(c);
			if (Math.abs(rh) <= EPS10) {
				dst.y = 0.0;
			} else {
				dst.y = MapMath.asin(y * sinc / rh);
			}
			if (x == 0.0 && cosc == 0.0) {
				dst.x = 0.0;
			} else {
				dst.x = MapMath.atan2(x * sinc, cosc * rh);
			}
		}

		public ModeSphericalEquator(ArgBase argBase, XaStereographicEquator xa) {
			m_akm1 = 2.0 * xa.scaleFactor;
		}
		private final double m_akm1;
	}

	private static class ModeSphericalOblique extends ModeSpherical {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double sinlam = Math.sin(lam);
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double cosphi = Math.cos(phi);
			final double aa = 1.0 + (m_sinph0 * sinphi) + (m_cosph0 * cosphi * coslam);
			if (!MapMath.validDivisorEPS8(aa)) throw ProjectionException.coordinateOutsideBounds();
			final double A = m_akm1 / aa;
			dst.x = A * cosphi * sinlam;
			dst.y = A * ((m_cosph0 * sinphi) - (m_sinph0 * cosphi * coslam));
		}

		@Override
		public void projectInverse(double x, double y, Builder dst) {
			final double rh = MapMath.hypot(x, y);
			final double c = 2.0 * Math.atan(rh / m_akm1);
			final double sinc = Math.sin(c);
			final double cosc = Math.cos(c);
			final double sinphi;
			if (Math.abs(rh) <= EPS10) {
				sinphi = m_sinph0;
				dst.y = m_phi0;
			} else {
				sinphi = (cosc * m_sinph0) + (y * sinc * m_cosph0 / rh);
				dst.y = MapMath.asin(sinphi);
			}
			final double c1 = cosc - (m_sinph0 * sinphi);
			if (x == 0.0 && c1 == 0.0) {
				dst.x = 0.0;
			} else {
				dst.x = MapMath.atan2(x * sinc * m_cosph0, c1 * rh);
			}
		}

		public ModeSphericalOblique(ArgBase argBase, XaStereographicOblique xa) {
			m_phi0 = xa.projectionLatitudeRads;
			m_akm1 = 2.0 * xa.scaleFactor;
			m_sinph0 = Math.sin(m_phi0);
			m_cosph0 = Math.cos(m_phi0);
		}
		private final double m_phi0;
		private final double m_akm1;
		private final double m_sinph0;
		private final double m_cosph0;
	}

	private static class ModeSphericalPolar extends ModeSpherical {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double sinlam = Math.sin(lam);
			final double coslam = m_north ? Math.cos(lam) : -Math.cos(lam);
			final double sphi = m_north ? phi : -phi;
			final double t = m_akm1 * Math.tan(MapMath.QUARTERPI + (0.5 * -sphi));
			dst.x = t * sinlam;
			dst.y = -t * coslam;
		}

		@Override
		public void projectInverse(double x, double y, Builder dst) {
			final double rh = MapMath.hypot(x, y);
			final double c = 2.0 * Math.atan(rh / m_akm1);
			final double cosc = Math.cos(c);
			if (Math.abs(rh) <= EPS10) {
				dst.y = m_phi0;
			} else {
				dst.y = MapMath.asin(m_north ? cosc : -cosc);
			}
			if (x == 0.0 && y == 0.0) {
				dst.x = 0.0;
			} else {
				dst.x = MapMath.atan2(x, m_north ? -y : y);
			}
		}

		public ModeSphericalPolar(ArgBase argBase, XaStereographicPolar xa) {
			final double scaleFactor = xa.scaleFactor;
			final double phits = Math.abs(xa.trueScaleLatitudeAbsRads);
			if (Math.abs(phits - MapMath.HALFPI) < EPS10) {
				m_akm1 = 2.0 * scaleFactor;
			} else {
				final double costs = Math.cos(phits);
				final double tn = Math.tan(MapMath.QUARTERPI - (0.5 * phits));
				m_akm1 = costs / tn;
			}
			m_north = xa.north;
			m_phi0 = xa.projectionLatitudeRads();
		}
		private final boolean m_north;
		private final double m_akm1;
		private final double m_phi0;
	}

}
