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
class XProjectionLambertConformalConical extends XAbstractProjectionConic {

	@Override
	public boolean hasInverse() {
		return true;
	}

	@Override
	public void initialize()
			throws GalliumProjectionException {
		super.initialize();
		double cosphi, sinphi;
		boolean secant;

		if (projectionLatitude1 == 0) {
			projectionLatitude1 = projectionLatitude2 = projectionLatitude;
		}

		if (Math.abs(projectionLatitude1 + projectionLatitude2) < 1e-10) {
			final String m = "Invalid projection latitude pair";
			throw new GalliumProjectionException(m);
		}
		n = sinphi = Math.sin(projectionLatitude1);
		cosphi = Math.cos(projectionLatitude1);
		secant = Math.abs(projectionLatitude1 - projectionLatitude2) >= 1e-10;
		spherical = (es == 0.0);
		if (!spherical) {
			double ml1, m1;

			m1 = MapMath.msfn(sinphi, cosphi, es);
			ml1 = MapMath.tsfn(projectionLatitude1, sinphi, e);
			if (secant) {
				n = Math.log(m1 / MapMath.msfn(sinphi = Math.sin(projectionLatitude2), Math.cos(projectionLatitude2), es));
				n /= Math.log(ml1 / MapMath.tsfn(projectionLatitude2, sinphi, e));
			}
			c = (rho0 = m1 * Math.pow(ml1, -n) / n);
			rho0 *= (Math.abs(Math.abs(projectionLatitude) - MapMath.HALFPI) < 1.0e-10) ? 0.0 : Math.pow(
					MapMath.tsfn(projectionLatitude, Math.sin(projectionLatitude), e), n);
		} else {
			if (secant) {
				n = Math.log(cosphi / Math.cos(projectionLatitude2))
						/ Math.log(Math.tan(MapMath.QUARTERPI + 0.5 * projectionLatitude2)
								/ Math.tan(MapMath.QUARTERPI + 0.5 * projectionLatitude1));
			}
			c = cosphi * Math.pow(Math.tan(MapMath.QUARTERPI + 0.5 * projectionLatitude1), n) / n;
			rho0 = (Math.abs(Math.abs(projectionLatitude) - MapMath.HALFPI) < 1.0e-10) ? 0.0 : c
					* Math.pow(Math.tan(MapMath.QUARTERPI + 0.5 * projectionLatitude), -n);
		}
	}

	@Override
	public boolean isConformal() {
		return true;
	}

	@Override
	public Builder project(double x, double y, Builder dst)
			throws GalliumProjectionException {
		final double rho;
		if (Math.abs(Math.abs(y) - MapMath.HALFPI) < 1e-10) {
			rho = 0.0;
		} else {
			final double rr;
			if (spherical) {
				rr = Math.pow(Math.tan(MapMath.QUARTERPI + 0.5 * y), -n);
			} else {
				rr = Math.pow(MapMath.tsfn(y, Math.sin(y), e), n);
			}
			rho = c * rr;
		}
		dst.x = scaleFactor * (rho * Math.sin(x *= n));
		dst.y = scaleFactor * (rho0 - rho * Math.cos(x));
		return dst;
	}

	@Override
	public Builder projectInverse(double x, double y, Builder out)
			throws GalliumProjectionException {
		x /= scaleFactor;
		y /= scaleFactor;
		double rho = MapMath.distance(x, y = rho0 - y);
		if (rho != 0) {
			if (n < 0.0) {
				rho = -rho;
				x = -x;
				y = -y;
			}
			if (spherical) {
				out.y = 2.0 * Math.atan(Math.pow(c / rho, 1.0 / n)) - MapMath.HALFPI;
			} else {
				out.y = MapMath.phi2(Math.pow(rho / c, 1.0 / n), e);
			}
			out.x = Math.atan2(x, y) / n;
		} else {
			out.x = 0.0;
			out.y = n > 0.0 ? MapMath.HALFPI : -MapMath.HALFPI;
		}
		return out;
	}

	@Override
	public String toString() {
		return "Lambert Conformal Conic";
	}

	public XProjectionLambertConformalConical() throws GalliumProjectionException {
		clippingMinLatitude = Math.toRadians(0);
		clippingMaxLatitude = Math.toRadians(80.0);
		projectionLatitude = MapMath.QUARTERPI;
		projectionLatitude1 = 0;
		projectionLatitude2 = 0;
		initialize();
	}

	public XProjectionLambertConformalConical(Ellipsoid ellipsoid, double lon_0, double lat_1, double lat_2, double lat_0,
			double x_0, double y_0) throws GalliumProjectionException {
		setEllipsoid(ellipsoid);
		projectionLongitude = lon_0;
		projectionLatitude = lat_0;
		scaleFactor = 1.0;
		falseEasting = x_0;
		falseNorthing = y_0;
		projectionLatitude1 = lat_1;
		projectionLatitude2 = lat_2;
		initialize();
	}

	private double n;
	private double rho0;
	private double c;
}
