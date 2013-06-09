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
class ProjectionSelector {

	public static final int Standard = 0;

	public static ProjectionSelector newEpsg(int code, String title, Class<? extends IProjectionFactory> factoryClass) {
		return new ProjectionSelector(Authority.newEPSG(code), Title.newInstance(title), factoryClass, Standard);
	}

	public static ProjectionSelector newEpsg(int code, String title, Class<? extends IProjectionFactory> factoryClass,
			int variantId) {
		return new ProjectionSelector(Authority.newEPSG(code), Title.newInstance(title), factoryClass, variantId);
	}

	public static ProjectionSelector newEsri(int code, String title, Class<? extends IProjectionFactory> factoryClass) {
		return new ProjectionSelector(Authority.newESRI(code), Title.newInstance(title), factoryClass, Standard);
	}

	public static ProjectionSelector newEsri(int code, String title, Class<? extends IProjectionFactory> factoryClass,
			int variantId) {
		return new ProjectionSelector(Authority.newESRI(code), Title.newInstance(title), factoryClass, variantId);
	}

	public IProjectionFactory newFactory()
			throws GalliumProjectionException {
		try {
			final IProjectionFactory neo = m_factoryClass.newInstance();
			if (oAuthority != null) {
				neo.setAuthority(oAuthority);
			}
			neo.setTitle(title);
			neo.setVariant(m_variantId);
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
		sb.append(", variant ").append(m_variantId);
		return sb.toString();
	}

	private ProjectionSelector(Authority oAuthority, Title title, Class<? extends IProjectionFactory> factoryClass,
			int variantId) {
		assert title != null;
		assert factoryClass != null;
		this.oAuthority = oAuthority;
		this.title = title;
		m_factoryClass = factoryClass;
		m_variantId = variantId;
	}
	public final Authority oAuthority;
	public final Title title;
	private final Class<? extends IProjectionFactory> m_factoryClass;
	private final int m_variantId;
}
