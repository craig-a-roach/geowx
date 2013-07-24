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

	private static Mode newModeEquator(ArgBase argBase, XaStereographicEquator argEquator) {
		final double scaleFactor = argEquator.scaleFactor;
		final double akm1 = 2.0 * scaleFactor;
		return new ModeEquator(argBase, akm1);
	}

	private static Mode newModeOblique(ArgBase argBase, XaStereographicOblique argOblique) {
		final double e = argBase.e;
		final double scaleFactor = argOblique.scaleFactor;
		final boolean spherical = argBase.spherical;
		final double akm1;
		final double sinphi0;
		final double cosphi0;
		final double projectionLatitude = argOblique.projectionLatitudeRads;
		final double t = Math.abs(projectionLatitude);
		if (spherical) {
			akm1 = 2.0 * scaleFactor;
			sinphi0 = Math.sin(projectionLatitude);
			cosphi0 = Math.cos(projectionLatitude);
		} else {
			final double sint = Math.sin(projectionLatitude);
			final double cost = Math.cos(projectionLatitude);
			final double X = 2.0 * Math.atan(ssfn(projectionLatitude, sint, e)) - MapMath.HALFPI;
			final double sinte = sint * e;
			akm1 = 2.0 * scaleFactor * cost / Math.sqrt(1.0 - sinte * sinte);
			sinphi0 = Math.sin(X);
			cosphi0 = Math.cos(X);
		}
		return new ModeOblique(argBase, akm1, sinphi0, cosphi0);
	}

	private static Mode newModePolar(ArgBase argBase, XaStereographicPolar argPolar) {
		final double e = argBase.e;
		final double scaleFactor = argPolar.scaleFactor;
		final boolean spherical = argBase.spherical;
		final double trueScaleLatitude = argPolar.trueScaleLatitudeAbsRads;
		final double akm1;
		if (Math.abs(trueScaleLatitude - MapMath.HALFPI) < EPS10) {
			if (spherical) {
				akm1 = 2.0 * scaleFactor;
			} else {
				akm1 = 2.0 * scaleFactor / Math.sqrt(Math.pow(1 + e, 1 + e) * Math.pow(1 - e, 1 - e));
			}
		} else {
			final double cost = Math.cos(trueScaleLatitude);
			if (spherical) {
				final double t = Math.tan(MapMath.QUARTERPI - (0.5 * trueScaleLatitude));
				akm1 = cost / t;
			} else {
				final double sint = Math.sin(trueScaleLatitude);
				final double ts = MapMath.tsfn(trueScaleLatitude, sint, e);
				final double sinte = sint * e;
				akm1 = cost / ts / Math.sqrt(1.0 - (sinte * sinte));
			}
		}
		return new ModePolar(argBase, argPolar.north, akm1);
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
		if (argBase.spherical) {
			m_mode.projectSpherical(lam, phi, dst);
		} else {
			m_mode.projectEllipsoid(lam, phi, dst);
		}
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
		final Mode mode;
		if (arg instanceof XaStereographicPolar) {
			mode = newModePolar(argBase, (XaStereographicPolar) arg);
		} else if (arg instanceof XaStereographicEquator) {
			mode = newModeEquator(argBase, (XaStereographicEquator) arg);
		} else if (arg instanceof XaStereographicOblique) {
			mode = newModeOblique(argBase, (XaStereographicOblique) arg);
		} else {
			final String m = "Unsupported projection mode " + arg.getClass().getName();
			throw new IllegalStateException(m);
		}
		m_mode = mode;
	}
	private final Mode m_mode;

	private static abstract class Mode {

		abstract void projectEllipsoid(final double lam, final double phi, Builder dst)
				throws ProjectionException;

		abstract void projectSpherical(final double lam, final double phi, Builder dst)
				throws ProjectionException;

		protected Mode(ArgBase argBase) {
			assert argBase != null;
			m_e = argBase.e;
		}
		protected final double m_e;
	}

	private static class ModeEquator extends Mode {

		@Override
		void projectEllipsoid(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double X = 2.0 * Math.atan(ssfn(phi, sinphi, m_e)) - MapMath.HALFPI;
			final double sinX = Math.sin(X);
			final double cosX = Math.cos(X);
			final double A = 2.0 * m_akm1 / (1.0 + cosX * coslam);
			dst.y = A * sinX;
			dst.x = A * cosX;
		}

		@Override
		void projectSpherical(final double lam, final double phi, Builder dst)
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

		public ModeEquator(ArgBase argBase, double akm1) {
			super(argBase);
			m_akm1 = akm1;
		}
		private final double m_akm1;
	}

	private static class ModeOblique extends Mode {

		@Override
		void projectEllipsoid(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double X = 2.0 * Math.atan(ssfn(phi, sinphi, m_e)) - MapMath.HALFPI;
			final double sinX = Math.sin(X);
			final double cosX = Math.cos(X);
			final double A = m_akm1 / (m_cosphi0 * (1.0 + (m_sinphi0 * sinX) + (m_cosphi0 * cosX * coslam)));
			dst.y = A * ((m_cosphi0 * sinX) - (m_sinphi0 * cosX * coslam));
			dst.x = A * cosX;
		}

		@Override
		void projectSpherical(final double lam, final double phi, Builder dst)
				throws ProjectionException {
			final double sinlam = Math.sin(lam);
			final double coslam = Math.cos(lam);
			final double sinphi = Math.sin(phi);
			final double cosphi = Math.cos(phi);
			final double y = 1.0 + (m_sinphi0 * sinphi) + (m_cosphi0 * cosphi * coslam);
			if (y <= EPS10) {
				final String m = msgOutside(lam, phi);
				throw new ProjectionException(m);
			}
			final double ay = m_akm1 / y;
			dst.x = ay * cosphi * sinlam;
			dst.y = ay * (m_cosphi0 * sinphi) - (m_sinphi0 * cosphi * coslam);
		}

		public ModeOblique(ArgBase argBase, double akm1, double sinphi0, double cosphi0) {
			super(argBase);
			m_akm1 = akm1;
			m_sinphi0 = sinphi0;
			m_cosphi0 = cosphi0;
		}
		private final double m_akm1;
		private final double m_sinphi0;
		private final double m_cosphi0;
	}

	private static class ModePolar extends Mode {

		@Override
		void projectEllipsoid(double lam, double phi, Builder dst)
				throws ProjectionException {
			final double sgn = m_north ? -1.0 : 1.0;
			final double coslam = sgn * Math.cos(lam);
			final double sinphi = sgn * Math.sin(phi);
			final double sphi = sgn * phi;
			final double x = m_akm1 * MapMath.tsfn(sphi, sinphi, m_e);
			dst.x = x;
			dst.y = -x * coslam;
		}

		@Override
		void projectSpherical(double lam, double phi, Builder dst)
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

		public ModePolar(ArgBase argBase, boolean north, double akm1) {
			super(argBase);
			m_north = north;
			m_akm1 = akm1;
		}
		private final boolean m_north;
		private final double m_akm1;
	}

}
