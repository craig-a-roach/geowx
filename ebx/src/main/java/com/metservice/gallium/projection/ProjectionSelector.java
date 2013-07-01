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
class ProjectionSelector implements IWktEmit {

	public static ProjectionSelector newEpsg(int code, String title, Class<? extends IProjectionFactory> factoryClass) {
		return new ProjectionSelector(Authority.newEPSG(code), Title.newInstance(title), factoryClass);
	}

	public static ProjectionSelector newEsri(int code, String title, Class<? extends IProjectionFactory> factoryClass) {
		return new ProjectionSelector(Authority.newESRI(code), Title.newInstance(title), factoryClass);
	}

	public IProjectionFactory newFactory()
			throws GalliumProjectionException {
		try {
			final IProjectionFactory neo = m_factoryClass.newInstance();
			if (oAuthority != null) {
				neo.setAuthority(oAuthority);
			}
			neo.setTitle(title);
			return neo;
		} catch (InstantiationException | IllegalAccessException ex) {
			final String m = "Invalid factory class for " + title + "..." + Ds.message(ex);
			throw new GalliumProjectionException(m);
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(title);
		if (oAuthority != null) {
			sb.append(", authority ").append(oAuthority);
		}
		sb.append(", class ").append(m_factoryClass.getName());
		return sb.toString();
	}

	@Override
	public WktStructure toWkt() {
		return new WktStructure("PROJECTION", title, oAuthority);
	}

	private ProjectionSelector(Authority oAuthority, Title title, Class<? extends IProjectionFactory> factoryClass) {
		assert title != null;
		assert factoryClass != null;
		this.oAuthority = oAuthority;
		this.title = title;
		m_factoryClass = factoryClass;
	}
	public final Authority oAuthority;
	public final Title title;
	private final Class<? extends IProjectionFactory> m_factoryClass;
}
