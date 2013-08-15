/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class ProjectedCoordinateSystem implements IWktEmit {

	public static ProjectedCoordinateSystem newInstance(String title, ProjectionSelector ps, ParameterMap pmap,
			GeographicCoordinateSystem gcs, Unit projectionLinearUnit, Authority oAuthority) {
		if (ps == null) throw new IllegalArgumentException("object is null");
		if (pmap == null) throw new IllegalArgumentException("object is null");
		if (gcs == null) throw new IllegalArgumentException("object is null");
		if (projectionLinearUnit == null) throw new IllegalArgumentException("object is null");
		return new ProjectedCoordinateSystem(oAuthority, Title.newInstance(title), ps, pmap, gcs, projectionLinearUnit);
	}

	public IGalliumProjection newProjection()
			throws GalliumProjectionException {
		final IProjectionFactory factory = selector.newFactory();
		final IGalliumProjection prj = factory.newProjection(parameterMap, gcs, projectionLinearUnit);
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
		ds.a("projectionLinearUnit", projectionLinearUnit);
		return ds.s();
	}

	@Override
	public WktStructure toWkt() {
		return new WktStructure("PROJCS", title, gcs, selector, parameterMap, projectionLinearUnit, oAuthority);
	}

	private ProjectedCoordinateSystem(Authority oAuthority, Title title, ProjectionSelector ps, ParameterMap pmap,
			GeographicCoordinateSystem gcs, Unit plu) {
		this.oAuthority = oAuthority;
		this.title = title;
		this.selector = ps;
		this.parameterMap = pmap;
		this.gcs = gcs;
		this.projectionLinearUnit = plu;
	}
	public final Authority oAuthority;
	public final Title title;
	public final ProjectionSelector selector;
	public final ParameterMap parameterMap;
	public final GeographicCoordinateSystem gcs;
	public final Unit projectionLinearUnit;
}
