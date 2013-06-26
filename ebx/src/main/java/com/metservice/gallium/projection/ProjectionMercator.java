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

	@Override
	public boolean hasInverse() {
		return true;
	}

	@Override
	public boolean isConformal() {
		return true;
	}

	@Override
	public Builder project(double lam, double phi, Builder dst) {
		if (args.base.spherical) {
			dst.x = scaleFactor * lam;
			dst.y = scaleFactor * Math.log(Math.tan(MapMath.QUARTERPI + 0.5 * phi));
		} else {
			dst.x = scaleFactor * lam;
			dst.y = -scaleFactor * Math.log(MapMath.tsfn(phi, Math.sin(phi), args.base.e));
		}
		return dst;
	}

	@Override
	public Builder projectInverse(double x, double y, Builder dst)
			throws ProjectionException {
		if (args.base.spherical) {
			dst.y = MapMath.HALFPI - 2. * Math.atan(Math.exp(-y / scaleFactor));
			dst.x = x / scaleFactor;
		} else {
			dst.y = MapMath.phi2(Math.exp(-y / scaleFactor), args.base.e);
			dst.x = x / scaleFactor;
		}
		return dst;
	}

	public ProjectionMercator(Authority oAuthority, Title title, ArgBase argBase, ArgMercator arg) {
		super(oAuthority, title, argBase);
		m_arg = arg;
	}
	private final ArgMercator m_arg;
}
