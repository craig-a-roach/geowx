/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import java.util.HashMap;
import java.util.Map;

/**
 * @author roach
 */
class ProjectionFactoryDictionary {

	private static final ProjectionFactoryDictionary Instance = newInstance();

	private static ProjectionFactoryDictionary newInstance() {
		final Builder b = new Builder(128);
		b.add(ProjectionSelector.newEpsg(9801, "Lambert_Conformal_Conic_1SP", XfLambertConformalConic.class));
		b.add(ProjectionSelector.newEpsg(9802, "Lambert_Conformal_Conic_2SP", XfLambertConformalConic.class));
		b.add(ProjectionSelector.newEpsg(9804, "Mercator_1SP", XfMercator.class));
		b.add(ProjectionSelector.newEpsg(9805, "Mercator_2SP", XfMercator.class));
		b.add(ProjectionSelector.newEpsg(9807, "Transverse_Mercator", XfTransverseMercator.class));
		b.add(ProjectionSelector.newEpsg(9809, "Oblique_Stereographic", XfStereographic.class));
		b.add(ProjectionSelector.newEpsg(9823, "Equirectangular", XfEquidistantCylindrical.class, XfEquidistantCylindrical.EQ));
		b.add(ProjectionSelector.newEpsg(9825, "Pseudo_Plate_Carree", XfEquidistantCylindrical.class,
				XfEquidistantCylindrical.EQ));
		b.add(ProjectionSelector.newEsri(43001, "Plate_Carree", XfEquidistantCylindrical.class, XfEquidistantCylindrical.EQ));
		b.add(ProjectionSelector.newEsri(43002, "Equidistant_Cylindrical", XfEquidistantCylindrical.class));
		b.add(ProjectionSelector.newEsri(43004, "Mercator", XfMercator.class));
		b.add(ProjectionSelector.newEsri(43005, "Gauss_Kruger", XfTransverseMercator.class));
		b.add(ProjectionSelector.newEsri(43020, "Lambert_Conformal_Conic", XfLambertConformalConic.class));
		b.add(ProjectionSelector.newEsri(43026, "Stereographic", XfStereographic.class));
		b.add(ProjectionSelector.newEsri(43041, "Orthographic", XfOrthographic.class));
		b.add(ProjectionSelector.newEsri(43047, "Gnomonic", XfGnomonic.class));
		b.add(ProjectionSelector.newEsri(43050, "Stereographic_North_Pole", XfStereographic.class, XfStereographic.NorthPole));
		b.add(ProjectionSelector.newEsri(43051, "Stereographic_South_Pole", XfStereographic.class, XfStereographic.SouthPole));

		return new ProjectionFactoryDictionary(b);
	}

	public static ProjectionSelector findByAuthority(Authority a) {
		return Instance.findByAuthorityImp(a);
	}

	public static ProjectionSelector findByTitle(String nc) {
		return Instance.findByTitleImp(Title.newInstance(nc));
	}

	private ProjectionSelector findByAuthorityImp(Authority a) {
		assert a != null;
		return m_authorityMap.get(a);
	}

	private ProjectionSelector findByTitleImp(Title t) {
		assert t != null;
		final ProjectionSelector oMatch = m_titleMap.get(t);
		if (oMatch != null) return oMatch;
		final Authority oAuth = Authority.createInstance(t);
		return oAuth == null ? null : findByAuthorityImp(oAuth);
	}

	private ProjectionFactoryDictionary(Builder b) {
		assert b != null;
		m_authorityMap = b.authorityMap;
		m_titleMap = b.titleMap;
	}
	final Map<Authority, ProjectionSelector> m_authorityMap;
	final Map<Title, ProjectionSelector> m_titleMap;

	private static class Builder {

		void add(ProjectionSelector s) {
			assert s != null;
			final Title title = s.title();
			final Authority oAuthority = s.getAuthority();
			if (titleMap.put(title, s) != null) throw new IllegalStateException("ambiguous title..." + s);
			if (oAuthority != null) {
				if (authorityMap.put(oAuthority, s) != null) throw new IllegalStateException("ambiguous authority..." + s);
			}
		}

		Builder(int initCap) {
			authorityMap = new HashMap<>(initCap);
			titleMap = new HashMap<>(initCap);
		}
		final Map<Authority, ProjectionSelector> authorityMap;
		final Map<Title, ProjectionSelector> titleMap;
	}
}
