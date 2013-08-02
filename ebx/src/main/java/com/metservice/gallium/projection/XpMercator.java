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
class XpMercator extends AbstractProjection {

	private static final double MaxLatDeg = 85.0;
	private static final double MaxPhi = MapMath.degToRad(MaxLatDeg);
	private static final double MinLatDeg = -85.0;
	private static final double MinPhi = MapMath.degToRad(MinLatDeg);

	private static double validateLatitude(double phi)
			throws ProjectionException {
		if (phi >= MinPhi && phi <= MaxPhi) return phi;
		throw ProjectionException.latitudeOutsideBounds();
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
		return true;
	}

	@Override
	public void project(final double lam, final double phi, Builder dst)
			throws ProjectionException {
		validateLatitude(phi);
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
	public void projectInverse(final double x, final double y, Builder dst)
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

	public XpMercator(Authority oAuthority, Title title, ArgBase argBase, XaMercator arg) {
		super(oAuthority, title, argBase);
		m_arg = arg;
	}

	private final XaMercator m_arg;
}
