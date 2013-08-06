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
class XpTransverseMercator extends AbstractProjection {

	private final static double FC1 = 1.0;
	private final static double FC2 = 0.5;
	private final static double FC3 = 0.16666666666666666666;
	private final static double FC4 = 0.08333333333333333333;
	private final static double FC5 = 0.05;
	private final static double FC6 = 0.03333333333333333333;
	private final static double FC7 = 0.02380952380952380952;
	private final static double FC8 = 0.01785714285714285714;

	private static final double MaxLonDeg = 89.9;
	private static final double MaxLam = MapMath.degToRad(MaxLonDeg);
	private static final double MinLonDeg = -89.9;
	private static final double MinLam = MapMath.degToRad(MinLonDeg);

	private static double validateLongitude(double lam)
			throws ProjectionException {
		if (MinLam <= lam && lam <= MaxLam) return lam;
		throw ProjectionException.longitudeOutsideBounds();
	}

	@Override
	protected void project(final double lam, final double phi, Builder dst)
			throws ProjectionException {
		m_imp.project(lam, phi, dst);
	}

	@Override
	protected void projectInverse(final double x, final double y, Builder dst)
			throws ProjectionException {
		m_imp.projectInverse(x, y, dst);
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

	public XpTransverseMercator(Authority oAuthority, Title title, ArgBase argBase, XaTransverseMercator arg) {
		super(oAuthority, title, argBase);
		if (argBase.spherical) {
			m_imp = new ImpSpheroid(argBase, arg);
		} else {
			m_imp = new ImpEllipsoid(argBase, arg);
		}
	}

	private final Imp m_imp;

	private static interface Imp {

		public void project(final double lam, final double phi, Builder dst)
				throws ProjectionException;

		public void projectInverse(final double x, final double y, Builder dst)
				throws ProjectionException;
	}

	private static class ImpEllipsoid implements Imp {

		@Override
		public void project(double lam, double phi, Builder dst)
				throws ProjectionException {
			validateLongitude(lam);
			final double sinphi = Math.sin(phi);
			final double cosphi = Math.cos(phi);
			final double t = Math.abs(cosphi) > EPS10 ? sinphi / cosphi : 0.0;
			final double ts = t * t;
			final double al = cosphi * lam;
			final double als = al * al;
			final double aly = al / Math.sqrt(1.0 - m_es * sinphi * sinphi);
			final double n = m_esp * cosphi * cosphi;
			final double xD = 61.0 + ts * (ts * (179.0 - ts) - 479.0);
			final double xC = 5.0 + ts * (ts - 18.0) + n * (14.0 - 58.0 * ts) + FC7 * als * xD;
			final double xB = 1.0 - ts + n + FC5 * als * xC;
			final double xA = FC1 + FC3 * als * xB;
			dst.x = m_k * aly * xA;

			final double yE = 1385.0 + ts * (ts * (543.0 - ts) - 3111.0);
			final double yD = 61.0 + ts * (ts - 58.0) + n * (270.0 - 330.0 * ts) + FC8 * als * yE;
			final double yC = 5.0 - ts + n * (9.0 + 4.0 * n) + FC6 * als * yD;
			final double yB = 1.0 + FC4 * als * yC;
			final double yA = MapMath.mlfn(phi, sinphi, cosphi, m_en) - m_ml0 + sinphi * aly * lam * FC2 * yB;
			dst.y = m_k * yA;
		}

		@Override
		public void projectInverse(double x, double y, Builder dst) {
			final double dy = MapMath.inv_mlfn(m_ml0 + (y / m_k), m_es, m_en);
			if (Math.abs(y) >= MapMath.HALFPI) {
				dst.y = y < 0. ? -MapMath.HALFPI : MapMath.HALFPI;
				dst.x = 0.0;
			} else {
				final double sinphi = Math.sin(dy);
				final double cosphi = Math.cos(dy);
				final double t = Math.abs(cosphi) > EPS10 ? sinphi / cosphi : 0.0;
				final double n = m_esp * cosphi * cosphi;
				final double con = 1.0 - m_es * sinphi * sinphi;
				final double d = x * Math.sqrt(con) / m_k;
				final double cont = con * t;
				final double ts = t * t;
				final double ds = d * d;
				final double yD = 1385.0 + ts * (3633.0 + ts * (4095.0 + 1574.0 * ts));
				final double yC = 61.0 + ts * (90.0 - 252.0 * n + 45.0 * ts) + 46.0 * n - ds * FC8 * yD;
				final double yB = 5.0 + ts * (3.0 - 9.0 * n) + n * (1.0 - 4.0 * n) - ds * FC6 * yC;
				final double yA = 1.0 - ds * FC4 * yB;
				dst.y = dy - (cont * ds / (1.0 - m_es)) * FC2 * yA;

				final double xD = 61.0 + ts * (662.0 + ts * (1320.0 + 720.0 * ts));
				final double xC = 5.0 + ts * (28.0 + 24.0 * ts + 8.0 * n) + 6.0 * n - ds * FC7 * xD;
				final double xB = 1.0 + 2.0 * ts + n - ds * FC5 * xC;
				final double xA = FC1 - ds * FC3 * xB;
				dst.x = d * xA / cosphi;
			}
		}

		public ImpEllipsoid(ArgBase argBase, XaTransverseMercator arg) {
			m_k = arg.scaleFactor;
			m_es = argBase.es;
			m_en = MapMath.enfn(m_es);
			m_esp = m_es / (1.0 - m_es);
			final double phi0 = arg.projectionLatitudeRads;
			m_ml0 = MapMath.mlfn(phi0, Math.sin(phi0), Math.cos(phi0), m_en);
		}
		private final double m_k;
		private final double m_es;
		private final double m_esp;
		private final double m_ml0;
		private final double[] m_en;
	}

	private static class ImpSpheroid implements Imp {

		@Override
		public void project(double lam, double phi, Builder dst)
				throws ProjectionException {
			validateLongitude(lam);
			final double cosphi = Math.cos(phi);
			final double b = cosphi * Math.sin(lam);
			if (!MapMath.validDivisorEPS10(Math.abs(b) - 1.0)) throw ProjectionException.coordinateOutsideBounds();
			final double c = cosphi * Math.cos(lam) / Math.sqrt(1.0 - b * b);
			final double ac = Math.abs(c);
			double d;
			if (ac >= 1.0) {
				if (!MapMath.validDivisorEPS10(ac - 1.0)) throw ProjectionException.coordinateOutsideBounds();
				d = 0.0;
			} else {
				d = MapMath.acos(c);
			}
			final double sy = phi < 0.0 ? -d : d;
			dst.x = m_aks5 * Math.log((1.0 + b) / (1.0 - b));
			dst.y = m_aks0 * (sy - m_phi0);
		}

		@Override
		public void projectInverse(double x, double y, Builder dst) {
			final double h1 = Math.exp(x / m_aks0);
			final double g = 0.5 * (h1 - (1.0 / h1));
			final double h2 = Math.cos(m_phi0 + (y / m_aks0));
			final double hg = (1.0 - h2 * h2) / (1.0 + g * g);
			final double d = Math.asin(Math.sqrt(hg));
			dst.x = (g == 0.0 && h2 == 0.0) ? 0.0 : Math.atan2(g, h2);
			dst.y = y < 0.0 ? -d : d;
		}

		public ImpSpheroid(ArgBase argBase, XaTransverseMercator arg) {
			m_aks0 = arg.scaleFactor;
			m_aks5 = 0.5 * m_aks0;
			m_phi0 = arg.projectionLatitudeRads;
		}
		private final double m_aks0;
		private final double m_aks5;
		private final double m_phi0;
	}
}
