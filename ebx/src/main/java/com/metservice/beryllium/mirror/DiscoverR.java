/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import java.util.List;

import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

class DiscoverR extends JsonR {

	private static final String p_demand = CProp.demandPaths;
	private static final String p_commit = CProp.commitPaths;

	@Override
	public void saveBody(JsonObject dst) {
		dst.put(p_demand, JsonArray.newImmutableFromStrings(m_zlDemandPathsAsc));
		dst.put(p_commit, JsonArray.newImmutableFromStrings(m_zlCommitPathsAsc));
	}

	@Override
	public int schema() {
		return 1;
	}

	public List<String> zlCommitPathsAsc() {
		return m_zlCommitPathsAsc;
	}

	public List<String> zlDemandPathsAsc() {
		return m_zlDemandPathsAsc;
	}

	public DiscoverR(JsonObject src) throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		final int schema = schema(src);
		if (schema != 1) throw new JsonSchemaException("Unsupported DiscoverR schema " + schema);
		m_zlDemandPathsAsc = src.accessor(p_demand).datumArray().new_zlqtwStrings();
		m_zlCommitPathsAsc = src.accessor(p_commit).datumArray().new_zlqtwStrings();
	}

	public DiscoverR(List<String> zlDemandPathsAsc, List<String> zlCommitPathsAsc) {
		if (zlDemandPathsAsc == null) throw new IllegalArgumentException("object is null");
		if (zlCommitPathsAsc == null) throw new IllegalArgumentException("object is null");
		m_zlDemandPathsAsc = zlDemandPathsAsc;
		m_zlCommitPathsAsc = zlCommitPathsAsc;
	}
	private final List<String> m_zlDemandPathsAsc;
	private final List<String> m_zlCommitPathsAsc;
}