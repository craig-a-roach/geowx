/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import com.metservice.argon.Ds;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

class DiscoverK extends JsonK<DiscoverR> {

	@Override
	protected DiscoverR newResponse(JsonObject src)
			throws JsonSchemaException {
		return new DiscoverR(src);
	}

	@Override
	protected String trackerType() {
		return "Discover";
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("query", query);
		ds.a("response", getResponse());
		return ds.s();
	}

	public DiscoverK(IBerylliumMirrorProbe probe, DiscoverQ query) {
		super(probe);
		if (query == null) throw new IllegalArgumentException("object is null");
		this.query = query;
	}
	public final DiscoverQ query;
}