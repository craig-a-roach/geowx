/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class ProjectedCoordinateSystem implements IWktEmit {

	public static ProjectedCoordinateSystem newInstance(String title, ProjectionSelector ps, ParameterMap pmap,
			GeographicCoordinateSystem gcs, Unit linearUnit, Authority oAuthority) {
		if (ps == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");
		if (gcs == null) throw new IllegalArgumentException("object is null");
		if (linearUnit == null) throw new IllegalArgumentException("object is null");
		return new ProjectedCoordinateSystem(oAuthority, Title.newInstance(title), ps, pmap, gcs, linearUnit);
	}

	public IGalliumProjection newProjection()
			throws GalliumProjectionException {
		final IProjectionFactory factory = selector.newFactory();
		final IGalliumProjection prj = factory.newProjection(parameterMap, gcs, linearUnit);
		return prj;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("authority", oAuthority);
		ds.a("title", title);
		ds.a("selector", selector);
		ds.a("parameterMap", parameterMap);
		ds.a("gcs", gcs);
		ds.a("linearUnit", linearUnit);
		return ds.s();
	}

	@Override
	public WktStructure toWkt() {
		return new WktStructure("PROJCS", title, gcs, selector, parameterMap, linearUnit);
	}

	private ProjectedCoordinateSystem(Authority oAuthority, Title title, ProjectionSelector ps, ParameterMap pmap,
			GeographicCoordinateSystem gcs, Unit linearUnit) {
		this.oAuthority = oAuthority;
		this.title = title;
		this.selector = ps;
		this.parameterMap = pmap;
		this.gcs = gcs;
		this.linearUnit = linearUnit;
	}
	public final Authority oAuthority;
	public final Title title;
	public final ProjectionSelector selector;
	public final ParameterMap parameterMap;
	public final GeographicCoordinateSystem gcs;
	public final Unit linearUnit;
}
