/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

/**
 * @author roach
 */
public class BerylliumPathQuery {

	public static BerylliumPathQuery createInstance(BerylliumPath opath) {
		if (opath == null) return null;
		return new BerylliumPathQuery(opath, BerylliumQuery.Empty);
	}

	public BerylliumPathQuery newPathQuery(Object... ozptRhsQueryNameValues) {
		final BerylliumQuery rhsQuery = BerylliumQuery.newInstance(ozptRhsQueryNameValues);
		final BerylliumQuery neoQuery = query.newQuery(rhsQuery);
		return new BerylliumPathQuery(path, neoQuery);
	}

	@Override
	public String toString() {
		return ztwPathQueryEncoded();
	}

	public String ztwPathQueryEncoded() {
		return path.ztwPathEncoded(query);
	}

	public BerylliumPathQuery(BerylliumPath path) {
		this(path, BerylliumQuery.Empty);
	}

	public BerylliumPathQuery(BerylliumPath path, BerylliumQuery query) {
		if (path == null) throw new IllegalArgumentException("object is null");
		if (query == null) throw new IllegalArgumentException("object is null");
		this.path = path;
		this.query = query;
	}

	public final BerylliumPath path;
	public final BerylliumQuery query;

}
