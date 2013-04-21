/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
abstract class AbstractProjectionConic extends AbstractProjection {

	/**
	 * Default standard latitude for conic projections
	 */
	public static final double CONIC_LAT_DEG = 40;

	public double getProjectionLatitude1() {
		return projectionLatitude1;
	}

	public final double getProjectionLatitude1Degrees() {
		return getProjectionLatitude1() * RTD;
	}

	public double getProjectionLatitude2() {
		return projectionLatitude2;
	}

	public final double getProjectionLatitude2Degrees() {
		return getProjectionLatitude2() * RTD;
	}

	/**
	 * Set the projection latitude in radians.
	 */
	@Override
	public void setProjectionLatitude(double projectionLatitude) {
		this.projectionLatitude = projectionLatitude;
		this.projectionLatitude1 = projectionLatitude;
		this.projectionLatitude2 = projectionLatitude;
	}

	public void setProjectionLatitude1(double projectionLatitude1) {
		this.projectionLatitude1 = projectionLatitude1;
	}

	public final void setProjectionLatitude1Degrees(double projectionLatitude1) {
		setProjectionLatitude1(DTR * projectionLatitude1);
	}

	public void setProjectionLatitude2(double projectionLatitude2) {
		this.projectionLatitude2 = projectionLatitude2;
	}

	public final void setProjectionLatitude2Degrees(double projectionLatitude2) {
		setProjectionLatitude2(DTR * projectionLatitude2);
	}

	@Override
	public String toString() {
		return "Conic";
	}

	protected AbstractProjectionConic() {
		projectionLatitude = Math.toRadians(CONIC_LAT_DEG);
		projectionLatitude1 = projectionLatitude;
		projectionLatitude2 = projectionLatitude;
	}

	/**
	 * Standard parallel 1 (for projections which use it).
	 */
	protected double projectionLatitude1;

	/**
	 * Standard parallel 2 (for projections which use it)
	 */
	protected double projectionLatitude2;
}
