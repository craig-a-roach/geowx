/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

/**
 * @author roach
 */
class ProjectedCoordinateSystem implements IGalliumProjectedCoordinateSystem {

	private ProjectedCoordinateSystem(Authority oAuthority, Title title, ProjectionSelector prjs, ParameterMap prjpm,
			IGalliumGeographicCoordinateSystem gcs, Unit linearUnit) {
		this.oAuthority = oAuthority;
		this.title = title;
		this.projectionSelector = prjs;
		this.projectionParameterMap = prjpm;
		this.gcs = gcs;
		this.linearUnit = linearUnit;
	}
	public final Authority oAuthority;
	public final Title title;
	public final ProjectionSelector projectionSelector;
	public final ParameterMap projectionParameterMap;
	public final IGalliumGeographicCoordinateSystem gcs;
	public final Unit linearUnit;
}
