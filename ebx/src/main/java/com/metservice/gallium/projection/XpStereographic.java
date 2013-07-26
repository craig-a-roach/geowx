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

	private static String msgOutside(double lam, double phi) {
		final double lon = MapMath.radToDeg(lam);
		final double lat = MapMath.radToDeg(phi);
		return "Lat/Lon " + lat + "," + lon + " deg is outside projection bounds";
	}

	private static double ssfn(final double phit, final double sinphi, double e) {
		final double sinphie = sinphi * e;
		final double sinphier = (1.0 - sinphie) / (1.0 + sinphie);
		return Math.tan(0.5 * (MapMath.HALFPI + phit)) * Math.pow(sinphier, 0.5 * e);
	}

	@Override
	protected boolean inside(double lam, double phi)
			throws ProjectionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void project(final double lam, final double phi, Builder dst)
			throws ProjectionException {
		m_mode.project(lam, phi, dst);
	}

	@Override
	protected void projectInverse(double x, double y, Builder dst)
			throws ProjectionException {
		// TODO Auto-generated method stub

	}

	@Override
	public double clippingMaxLatitudeDegrees() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double clippingMinLatitudeDegrees() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasInverse() {
		return true;
	}

	@Override
	public boolean isConformal() {
		return true;
	}

	@Override
	public boolean isEqualArea() {
		return false;
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
	}

	private static abstract class ModeEllipsoid implements Mode {

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
			final double X = 2.0 * Math.atan(ssfn(phi, sinphi, m_e)) - MapMath.HALFPI;
			final double sinX = Math.sin(X);
			final double cosX = Math.cos(X);
			final double A = 2.0 * m_akm1 / (1.0 + (cosX * coslam));
			dst.y = A * sinX;
			dst.x = A * cosX * sinlam;
		}

		public ModeEllipsoidEquator(ArgBase argBase, XaStereographicEquator argEquator) {
			super(argBase);
			final double scaleFactor = argEquator.scaleFactor;
			m_akm1 = 2.0 * scaleFactor;
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
			final double aa = 1.0 + (m_sinX * sinX) + (m_cosX * cosX * coslam);
			final double A = m_akm1 / (m_cosX * aa);
			dst.x = A * cosX * sinlam;
			dst.y = A * ((m_cosX * sinX) - (m_sinX * cosX * coslam));
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
			final double sgn = m_north ? -1.0 : 1.0;
			final double sinlam = Math.sin(lam);
			final double coslam = sgn * Math.cos(lam);
			final double sinphi = sgn * Math.sin(phi);
			final double sphi = sgn * phi;
			final double x = m_akm1 * MapMath.tsfn(sphi, sinphi, m_e);
			dst.x = x * sinlam;
			dst.y = -x * coslam;
		}

		public ModeEllipsoidPolar(ArgBase argBase, XaStereographicPolar xa) {
			super(argBase);
			final double e = argBase.e;
			final double scaleFactor = xa.scaleFactor;
			final double trueScaleLatitude = xa.trueScaleLatitudeAbsRads;
			if (Math.abs(trueScaleLatitude - MapMath.HALFPI) < EPS10) {
				m_akm1 = 2.0 * scaleFactor / Math.sqrt(Math.pow(1 + e, 1 + e) * Math.pow(1 - e, 1 - e));
			} else {
				final double cost = Math.cos(trueScaleLatitude);
				final double sint = Math.sin(trueScaleLatitude);
				final double ts = MapMath.tsfn(trueScaleLatitude, sint, e);
				final double sinte = sint * e;
				m_akm1 = cost / ts / Math.sqrt(1.0 - (sinte * sinte));
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
			final double y = 1.0 + (cosphi * coslam);
			if (y <= EPS10) {
				final String m = msgOutside(lam, phi);
				throw new ProjectionException(m);
			}
			final double ay = m_akm1 / y;
			dst.x = ay * cosphi * sinlam;
			dst.y = ay * sinphi;
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
			final double aa = 1.0 + (m_sinX * sinphi) + (m_cosX * cosphi * coslam);
			if (aa <= EPS10) {
				final String m = msgOutside(lam, phi);
				throw new ProjectionException(m);
			}
			final double A = m_akm1 / aa;
			dst.x = A * cosphi * sinlam;
			dst.y = A * ((m_cosX * sinphi) - (m_sinX * cosphi * coslam));
		}

		public ModeSphericalOblique(ArgBase argBase, XaStereographicOblique xa) {
			final double phi0 = xa.projectionLatitudeRads;
			m_akm1 = 2.0 * xa.scaleFactor;
			m_sinX = Math.sin(phi0);
			m_cosX = Math.cos(phi0);
		}
		private final double m_akm1;
		private final double m_sinX;
		private final double m_cosX;
	}

	private static class ModeSphericalPolar extends ModeSpherical {

		@Override
		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			if (Math.abs(phi - MapMath.HALFPI) < EPS8) {
				final String m = msgOutside(lam, phi);
				throw new ProjectionException(m);
			}
			final double sgn = m_north ? -1.0 : 1.0;
			final double coslam = sgn * Math.cos(lam);
			final double sinlam = Math.sin(lam);
			final double sphi = sgn * phi;
			final double y = m_akm1 * Math.tan(MapMath.QUARTERPI + (0.5 * sphi));
			dst.x = sinlam * y;
			dst.y = coslam * y;
		}

		public ModeSphericalPolar(ArgBase argBase, XaStereographicPolar xa) {
			final double scaleFactor = xa.scaleFactor;
			final double trueScaleLatitude = xa.trueScaleLatitudeAbsRads;
			if (Math.abs(trueScaleLatitude - MapMath.HALFPI) < EPS10) {
				m_akm1 = 2.0 * scaleFactor;
			} else {
				final double cost = Math.cos(trueScaleLatitude);
				final double t = Math.tan(MapMath.QUARTERPI - (0.5 * trueScaleLatitude));
				m_akm1 = cost / t;
			}
			m_north = xa.north;
		}
		private final boolean m_north;
		private final double m_akm1;
	}

}
