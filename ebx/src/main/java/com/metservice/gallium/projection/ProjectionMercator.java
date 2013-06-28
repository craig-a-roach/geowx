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
class ProjectionMercator extends AbstractProjectionCylindrical {

	private static final double clippingMaxLatDeg = 85.0;
	private static final double clippingMaxPhi = MapMath.degToRad(clippingMaxLatDeg);
	private static final double clippingMinLatDeg = -85.0;
	private static final double clippingMinPhi = MapMath.degToRad(clippingMinLatDeg);

	@Override
	protected boolean inside(double lam, double phi)
			throws ProjectionException {
		return clippingMinPhi <= lam && lam <= clippingMaxPhi;
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
	public void project(double lam, double phi, Builder dst) {
		final double k0 = m_arg.scaleFactor;
		if (argBase.spherical) {
			dst.x = k0 * lam;
			dst.y = k0 * Math.log(Math.tan(MapMath.QUARTERPI + 0.5 * phi));
		} else {
			dst.x = k0 * lam;
			dst.y = -k0 * Math.log(MapMath.tsfn(phi, Math.sin(phi), argBase.e));
		}
	}

	@Override
	public void projectInverse(double x, double y, Builder dst)
			throws ProjectionException {
		final double k0 = m_arg.scaleFactor;
		if (argBase.spherical) {
			dst.y = MapMath.HALFPI - 2. * Math.atan(Math.exp(-y / k0));
			dst.x = x / k0;
		} else {
			dst.y = MapMath.phi2(Math.exp(-y / k0), argBase.e);
			dst.x = x / k0;
		}
	}

	public ProjectionMercator(Authority oAuthority, Title title, ArgBase argBase, ArgMercator arg) {
		super(oAuthority, title, argBase);
		m_arg = arg;
	}

	private final ArgMercator m_arg;
}
