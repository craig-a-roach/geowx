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

	private void projectEllipsoid(double lam, double phi, Builder dst)
			throws ProjectionException {

	}

	private void projectSpherical(final double lam, final double phi, Builder dst)
			throws ProjectionException {
		final double coslam = Math.cos(lam);
		final double sinlam = Math.sin(lam);
		final double sinphi = Math.sin(phi);
		final double cosphi = Math.cos(phi);

		// final double y = 1.0 + m_sinphi0 * sinphi + m_cosphi0 * cosphi * coslam;
		// if (y <= EPS10) throw new ProjectionException(msgOutside(lam, phi));
		// xy.x = (xy.y = akm1 / xy.y) * cosphi * sinlam;
		// xy.y *= cosphi0 * sinphi - sinphi0 * cosphi * coslam;

	}

	private double ssfn(final double phit, final double sinphi, double e) {
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
	protected void project(double lam, double phi, Builder dst)
			throws ProjectionException {
		if (argBase.spherical) {
			projectSpherical(lam, phi, dst);
		} else {
			projectEllipsoid(lam, phi, dst);
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
		m_arg = arg;
		m_sinphi0 = Math.sin(arg.projectionLatitudeRads);
		m_cosphi0 = Math.cos(arg.projectionLatitudeRads);
	}
	private final XaStereographic m_arg;
	private final double m_sinphi0;
	private final double m_cosphi0;
}
