/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.projection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author roach
 */
class ProjectionFactoryDictionary {

	private static final ProjectionFactoryDictionary Instance = newInstance();

	private static Entry epsg(int code, String title) {
		return new Entry(Authority.newEPSG(code), Title.newInstance(title));
	}

	private static Entry esri(int code, String title) {
		return new Entry(Authority.newESRI(code), Title.newInstance(title));
	}

	private static ProjectionFactoryDictionary newInstance() {
		final Builder b = new Builder(128);
		b.add(epsg(1, "demo"));
		return new ProjectionFactoryDictionary(b);
	}

	private ProjectionFactoryDictionary(Builder b) {
		assert b != null;
		m_authorityMap = b.authorityMap;
		m_titleMap = b.titleMap;
	}
	final Map<Authority, Entry> m_authorityMap;
	final Map<Title, Entry> m_titleMap;

	private static class Builder {

		void add(Entry e) {
			assert e != null;
			if (titleMap.put(e.title, e) != null) throw new IllegalStateException("ambiguous title..." + e);
			if (e.oAuthority != null) {
				if (authorityMap.put(e.oAuthority, e) != null)
					throw new IllegalStateException("ambiguous authority..." + e);
			}
		}

		Builder(int initCap) {
			authorityMap = new HashMap<>(initCap);
			titleMap = new HashMap<>(initCap);
		}
		final Map<Authority, Entry> authorityMap;
		final Map<Title, Entry> titleMap;
	}

	private static class Entry {

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(title);
			if (oAuthority != null) {
				sb.append(" authority ").append(oAuthority);
			}
			return sb.toString();
		}

		Entry(Authority oAuthority, Title title) {
			assert title != null;
			this.oAuthority = oAuthority;
			this.title = title;
		}
		public final Authority oAuthority;
		public final Title title;
	}

}
