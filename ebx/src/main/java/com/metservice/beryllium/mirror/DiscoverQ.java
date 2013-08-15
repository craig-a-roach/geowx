/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import java.util.List;

import com.metservice.argon.Ds;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

class DiscoverQ extends JsonQ {

	private static final String p_hiex = CProp.hiexPath;
	private static final String p_wip = CProp.wipPaths;

	public String qcctwHiex() {
		return m_qcctwHiex;
	}

	@Override
	public void saveTo(JsonObject dst) {
		saveSchema(dst, 1);
		dst.putString(p_hiex, m_qcctwHiex);
		dst.put(p_wip, JsonArray.newImmutableFromStrings(m_zlWipPathsAsc));
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o("DiscoverQ");
		ds.a("hiex", m_qcctwHiex);
		ds.a("wip", m_zlWipPathsAsc);
		return ds.ss();
	}

	public List<String> zlWipPathsAsc() {
		return m_zlWipPathsAsc;
	}

	public DiscoverQ(JsonObject src) throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final int schema = schema(src);
		if (schema != 1) throw new JsonSchemaException("Unsupported DiscoverQ schema " + schema);
		m_qcctwHiex = src.accessor(p_hiex).datumQtwString();
		m_zlWipPathsAsc = src.accessor(p_wip).datumArray().new_zlqtwStrings();
	}

	public DiscoverQ(String qcctwHiex, List<String> zlWipPathsAsc) {
		if (qcctwHiex == null || qcctwHiex.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (zlWipPathsAsc == null) throw new IllegalArgumentException("object is null");
		m_qcctwHiex = qcctwHiex;
		m_zlWipPathsAsc = zlWipPathsAsc;
	}
	private final String m_qcctwHiex;
	private final List<String> m_zlWipPathsAsc;
}