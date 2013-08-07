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
class XpLambertConformalConic extends AbstractProjection {

	@Override
	public boolean hasInverse() {
		return true;
	}

	@Override
	public boolean isRectilinear() {
		return false;
	}

	@Override
	public void project(final double lam, final double phi, Builder dst) {
		final double r;
		if (Math.abs(Math.abs(phi) - MapMath.HALFPI) < EPS10) {
			r = 0.0;
		} else {
			double t;
			double n;
			if (argBase.spherical) {
				t = Math.tan(MapMath.QUARTERPI + 0.5 * phi);
				n = -m_n;
			} else {
				t = MapMath.tsfn(phi, Math.sin(phi), argBase.e);
				n = m_n;
			}
			r = m_c * Math.pow(t, n);
		}
		final double theta = lam * m_n;
		final double k = m_arg.scaleFactor;
		dst.x = k * (r * Math.sin(theta));
		dst.y = k * (m_r - (r * Math.cos(theta)));
	}

	@Override
	public void projectInverse(final double x, final double y, Builder dst)
			throws ProjectionException {
		final double k = m_arg.scaleFactor;
		final double xk = x / k;
		final double yk = m_r - (y / k);
		final double r = MapMath.distance(xk, yk);
		if (r < EPS10) {
			dst.x = 0.0;
			dst.y = m_n > 0.0 ? MapMath.HALFPI : -MapMath.HALFPI;
		} else {
			double xn;
			double yn;
			double rn;
			if (m_n < 0.0) {
				rn = -r;
				xn = -xk;
				yn = -yk;
			} else {
				rn = r;
				xn = xk;
				yn = yk;
			}
			if (argBase.spherical) {
				dst.y = 2.0 * Math.atan(Math.pow(m_c / rn, 1.0 / m_n)) - MapMath.HALFPI;
			} else {
				dst.y = MapMath.phi2(Math.pow(rn / m_c, 1.0 / m_n), argBase.e);
			}
			dst.x = Math.atan2(xn, yn) / m_n;
		}
	}

	public XpLambertConformalConic(Authority oAuthority, Title title, ArgBase argBase, XaLambertConformalConic arg) {
		super(oAuthority, title, argBase);
		m_arg = arg;
		final double phi0 = arg.projectionLatitudeRads0;
		final double phi1 = arg.projectionLatitudeRads1;
		final double phi2 = arg.projectionLatitudeRads2;
		final double sinphi1 = Math.sin(phi1);
		final double cosphi1 = Math.cos(phi1);
		final boolean secant = Math.abs(phi1 - phi2) >= EPS10;
		final double n;
		final double c;
		final double r;
		if (argBase.spherical) {
			final double t1 = Math.tan(MapMath.QUARTERPI + 0.5 * phi1);
			if (secant) {
				final double cosphi2 = Math.cos(phi2);
				final double t2 = Math.tan(MapMath.QUARTERPI + 0.5 * phi2);
				n = Math.log(cosphi1 / cosphi2) / Math.log(t2 / t1);
			} else {
				n = sinphi1;
			}
			c = cosphi1 * Math.pow(t1, n) / n;
			if (Math.abs(Math.abs(phi0) - MapMath.HALFPI) < EPS10) {
				r = 0.0;
			} else {
				final double t0 = Math.tan(MapMath.QUARTERPI + 0.5 * phi0);
				r = c * Math.pow(t0, -n);
			}
		} else {
			final double t1 = MapMath.tsfn(phi1, sinphi1, argBase.e);
			final double m1 = MapMath.msfn(sinphi1, cosphi1, argBase.es);
			if (secant) {
				final double sinphi2 = Math.sin(phi2);
				final double cosphi2 = Math.cos(phi2);
				final double m2 = MapMath.msfn(sinphi2, cosphi2, argBase.es);
				final double t2 = MapMath.tsfn(phi2, sinphi2, argBase.e);
				n = Math.log(m1 / m2) / Math.log(t1 / t2);
			} else {
				n = sinphi1;
			}
			c = m1 * Math.pow(t1, -n) / n;
			if (Math.abs(Math.abs(phi0) - MapMath.HALFPI) < EPS10) {
				r = 0.0;
			} else {
				final double sinphi0 = Math.sin(phi0);
				final double t0 = MapMath.tsfn(phi0, sinphi0, argBase.e);
				r = c * Math.pow(t0, n);
			}
		}
		m_n = n;
		m_c = c;
		m_r = r;
	}

	private final XaLambertConformalConic m_arg;
	private final double m_n;
	private final double m_r;
	private final double m_c;
}
