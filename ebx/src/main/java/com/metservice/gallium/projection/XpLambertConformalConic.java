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

	private static final double clippingMaxLatDeg = 90.0;
	private static final double clippingMinLatDeg = -90.0;

	@Override
	protected boolean inside(double lam, double phi)
			throws ProjectionException {
		return true;
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

	@Override
	public void project(double lam, double phi, Builder dst) {
	}

	@Override
	public void projectInverse(double x, double y, Builder dst)
			throws ProjectionException {
	}

	public XpLambertConformalConic(Authority oAuthority, Title title, ArgBase argBase, XaLambertConformalConic arg) {
		super(oAuthority, title, argBase);
		m_arg = arg;
		final double phi0 = arg.projectionLatitude0;
		final double phi1 = arg.projectionLatitude1;
		final double phi2 = arg.projectionLatitude2;
		final double sinphi1 = Math.sin(phi1);
		final double cosphi1 = Math.cos(phi1);
		final boolean secant = Math.abs(phi1 - phi2) >= EPS10;
		final double n;
		final double c;
		final double rho;
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
				rho = 0.0;
			} else {
				final double t0 = Math.tan(MapMath.QUARTERPI + 0.5 * phi0);
				rho = c * Math.pow(t0, -n);
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
				rho = 0.0;
			} else {
				final double sinphi0 = Math.sin(phi0);
				final double t0 = MapMath.tsfn(phi0, sinphi0, argBase.e);
				rho = c * Math.pow(t0, n);
			}
		}
		m_n = n;
		m_c = c;
		m_rho = rho;
	}

	private final XaLambertConformalConic m_arg;
	private final double m_n;
	private final double m_rho;
	private final double m_c;
}
