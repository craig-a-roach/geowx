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

	@Override
	protected boolean inside(double lam, double phi)
			throws ProjectionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void project(double lam, double phi, Builder dst)
			throws ProjectionException {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConformal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEqualArea() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRectilinear() {
		// TODO Auto-generated method stub
		return false;
	}

	public ProjectionTransverseMercator(Authority oAuthority, Title title, ArgBase argBase, ArgTransverseMercator arg) {
		super(oAuthority, title, argBase);
		m_arg = arg;
	}

	private final ArgTransverseMercator m_arg;
}
