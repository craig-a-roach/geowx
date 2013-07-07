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
class ProjectionTransverseMercator extends AbstractProjectionCylindrical {

	private final static double FC1 = 1.0;
	private final static double FC2 = 0.5;
	private final static double FC3 = 0.16666666666666666666;
	private final static double FC4 = 0.08333333333333333333;
	private final static double FC5 = 0.05;
	private final static double FC6 = 0.03333333333333333333;
	private final static double FC7 = 0.02380952380952380952;
	private final static double FC8 = 0.01785714285714285714;

	private static final double clippingMaxLatDeg = 90.0;
	private static final double clippingMinLatDeg = -90.0;

	private static String pair(double lam, double phi) {
		return "(lambda=" + lam + ", phi=" + phi + ")";
	}

	private void projectEllipsoid(double lam, double phi, Builder dst)
			throws ProjectionException {
		final double sinphi = Math.sin(phi);
		final double cosphi = Math.cos(phi);
		final double t = Math.abs(cosphi) > EPS10 ? sinphi / cosphi : 0.0;
		final double ts = t * t;
		final double al = cosphi * lam;
		final double als = al * al;
		final double aly = al / Math.sqrt(1.0 - argBase.es * sinphi * sinphi);
		final double n = m_esp * cosphi * cosphi;
		final double xD = 61.0 + ts * (ts * (179.0 - ts) - 479.0);
		final double xC = 5.0 + ts * (ts - 18.0) + n * (14.0 - 58.0 * ts) + FC7 * als * xD;
		final double xB = 1.0 - ts + n + FC5 * als * xC;
		final double xA = FC1 + FC3 * als * xB;
		dst.x = m_arg.scaleFactor * aly * xA;

		final double yE = 1385.0 + ts * (ts * (543.0 - ts) - 3111.0);
		final double yD = 61.0 + ts * (ts - 58.0) + n * (270.0 - 330.0 * ts) + FC8 * als * yE;
		final double yC = 5.0 - ts + n * (9.0 + 4.0 * n) + FC6 * als * yD;
		final double yB = 1.0 + FC4 * als * yC;
		final double yA = MapMath.mlfn(phi, sinphi, cosphi, m_oen) - m_ml0 + sinphi * aly * lam * FC2 * yB;
		dst.y = m_arg.scaleFactor * yA;
	}

	private void projectInverseEllipsoid(double x, double y, Builder dst) {
		final double dy = MapMath.inv_mlfn(m_ml0 + y / m_arg.scaleFactor, argBase.es, m_oen);
		if (Math.abs(y) >= MapMath.HALFPI) {
			dst.y = y < 0. ? -MapMath.HALFPI : MapMath.HALFPI;
			dst.x = 0.0;
		} else {
			final double sinphi = Math.sin(dy);
			final double cosphi = Math.cos(dy);
			final double t = Math.abs(cosphi) > EPS10 ? sinphi / cosphi : 0.0;
			final double n = m_esp * cosphi * cosphi;
			final double con = 1.0 - argBase.es * sinphi * sinphi;
			final double d = x * Math.sqrt(con) / m_arg.scaleFactor;
			final double cont = con * t;
			final double ts = t * t;
			final double ds = d * d;
			final double yD = 1385.0 + ts * (3633.0 + ts * (4095.0 + 1574.0 * ts));
			final double yC = 61.0 + ts * (90.0 - 252.0 * n + 45.0 * ts) + 46.0 * n - ds * FC8 * yD;
			final double yB = 5.0 + ts * (3.0 - 9.0 * n) + n * (1.0 - 4.0 * n) - ds * FC6 * yC;
			final double yA = 1.0 - ds * FC4 * yB;
			dst.y = dy - (cont * ds / (1.0 - argBase.es)) * FC2 * yA;

			final double xD = 61.0 + ts * (662.0 + ts * (1320.0 + 720.0 * ts));
			final double xC = 5.0 + ts * (28.0 + 24.0 * ts + 8.0 * n) + 6.0 * n - ds * FC7 * xD;
			final double xB = 1.0 + 2.0 * ts + n - ds * FC5 * xC;
			final double xA = FC1 - ds * FC3 * xB;
			dst.x = d * xA / cosphi;
		}
	}

	private void projectInverseSpherical(double x, double y, Builder dst) {
		final double D = y / m_arg.scaleFactor + m_arg.projectionLatitudeRads;
		final double xp = x / m_arg.scaleFactor;
		dst.y = Math.asin(Math.sin(D) / Math.cosh(xp));
		dst.x = Math.atan2(Math.sinh(xp), Math.cos(D));
	}

	private void projectSpherical(double lam, double phi, Builder dst)
			throws ProjectionException {
		final double cosphi = Math.cos(phi);
		double b = cosphi * Math.sin(lam);
		if (Math.abs(Math.abs(b) - 1.0) <= EPS10) {
			final String m = "Invalid coordinates " + pair(lam, phi);
			throw new ProjectionException(m);
		}
		final double dx = m_ml0 * m_arg.scaleFactor * Math.log((1.0 + b) / (1.0 - b));
		double dy = cosphi * Math.cos(lam) / Math.sqrt(1.0 - b * b);
		b = Math.abs(dy);
		if (b >= 1.0) {
			if ((b - 1.0) > EPS10) {
				final String m = "Invalid coordinates " + pair(lam, phi);
				throw new ProjectionException(m);
			}
			dy = 0.0;
		} else {
			dy = MapMath.acos(dy);
		}
		if (phi < 0.0) {
			dy = -dy;
		}
		dy = m_esp * (dy - m_arg.projectionLatitudeRads);
		dst.x = dx;
		dst.y = dy;
	}

	@Override
	protected boolean inside(double lam, double phi)
			throws ProjectionException {
		return true;
	}

	@Override
	protected void project(double lam, double phi, Builder dst)
			throws ProjectionException {
		if (m_oen == null) {
			projectSpherical(lam, phi, dst);
		} else {
			projectEllipsoid(lam, phi, dst);
		}
	}

	@Override
	protected void projectInverse(double x, double y, Builder dst)
			throws ProjectionException {
		if (m_oen == null) {
			projectInverseSpherical(x, y, dst);
		} else {
			projectInverseEllipsoid(x, y, dst);
		}
	}

	@Override
	public double clippingMaxLatitudeDegrees() {
		return clippingMaxLatDeg;
	}

	@Override
	public double clippingMinLatitudeDegrees() {
		return clippingMinLatDeg;
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

	public ProjectionTransverseMercator(Authority oAuthority, Title title, ArgBase argBase, ArgTransverseMercator arg) {
		super(oAuthority, title, argBase);
		m_arg = arg;
		if (argBase.spherical) {
			m_esp = arg.scaleFactor;
			m_ml0 = 0.5 * m_esp;
			m_oen = null;
		} else {
			m_oen = MapMath.enfn(argBase.es);
			m_esp = argBase.es / (1.0 - argBase.es);
			final double lar = arg.projectionLatitudeRads;
			m_ml0 = MapMath.mlfn(lar, Math.sin(lar), Math.cos(lar), m_oen);
		}
	}

	private final ArgTransverseMercator m_arg;
	private final double m_esp;
	private final double m_ml0;
	private final double[] m_oen;
}
