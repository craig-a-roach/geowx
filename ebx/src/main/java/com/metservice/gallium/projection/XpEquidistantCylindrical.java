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
class XpEquidistantCylindrical extends AbstractProjection {

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
		dst.x = m_arg.cosphits * lam;
		dst.y = phi - m_arg.projectionLatitudeRads;
	}

	@Override
	public void projectInverse(final double x, final double y, Builder dst)
			throws ProjectionException {
		dst.y = y + m_arg.projectionLatitudeRads;
		dst.x = x / m_arg.cosphits;
	}

	public XpEquidistantCylindrical(Authority oAuthority, Title title, ArgBase argBase, XaEquidistantCylindrical arg) {
		super(oAuthority, title, argBase);
		m_arg = arg;
	}

	private final XaEquidistantCylindrical m_arg;
}
