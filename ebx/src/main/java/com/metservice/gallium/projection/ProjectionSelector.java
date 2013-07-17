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
		return newEpsg(code, title, factoryClass, null);
	}

	public static ProjectionSelector newEpsg(int code, String title, Class<? extends IProjectionFactory> factoryClass,
			Integer oZone) {
		return new ProjectionSelector(Authority.newEPSG(code), Title.newInstance(title), factoryClass, oZone);
	}

	public static ProjectionSelector newEsri(int code, String title, Class<? extends IProjectionFactory> factoryClass) {
		return newEsri(code, title, factoryClass, null);
	}

	public static ProjectionSelector newEsri(int code, String title, Class<? extends IProjectionFactory> factoryClass,
			Integer oZone) {
		return new ProjectionSelector(Authority.newESRI(code), Title.newInstance(title), factoryClass, oZone);
	}

	public Authority getAuthority() {
		return m_oAuthority;
	}

	public IProjectionFactory newFactory()
			throws GalliumProjectionException {
		try {
			final IProjectionFactory neo = m_factoryClass.newInstance();
			if (m_oAuthority != null) {
				neo.setAuthority(m_oAuthority);
			}
			neo.setTitle(m_title);
			if (m_oZone != null) {
				neo.setZone(m_oZone.intValue());
			}
			return neo;
		} catch (final ProjectionException ex) {
			final String m = "Invalid factory configuration for " + m_title + "..." + ex.getMessage();
			throw new GalliumProjectionException(m);
		} catch (InstantiationException | IllegalAccessException ex) {
			final String m = "Invalid factory class for " + m_title + "..." + Ds.message(ex);
			throw new GalliumProjectionException(m);
		}
	}

	public Title title() {
		return m_title;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_title);
		if (m_oAuthority != null) {
			sb.append(", authority ").append(m_oAuthority);
		}
		sb.append(", class ").append(m_factoryClass.getName());
		if (m_oZone != null) {
			sb.append(", zone ").append(m_oZone);
		}
		return sb.toString();
	}

	@Override
	public WktStructure toWkt() {
		return new WktStructure("PROJECTION", m_title, m_oAuthority);
	}

	private ProjectionSelector(Authority oAuthority, Title title, Class<? extends IProjectionFactory> factoryClass, Integer oZone) {
		assert title != null;
		assert factoryClass != null;
		m_oAuthority = oAuthority;
		m_title = title;
		m_factoryClass = factoryClass;
		m_oZone = oZone;
	}
	private final Authority m_oAuthority;
	private final Title m_title;
	private final Class<? extends IProjectionFactory> m_factoryClass;
	private final Integer m_oZone;
}
