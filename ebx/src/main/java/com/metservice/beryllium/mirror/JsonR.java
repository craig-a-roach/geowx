/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
abstract class JsonR {

	protected static final int schema(JsonObject src)
			throws JsonSchemaException {
		return src.accessor(CProp.schema).datumInteger();
	}

	protected abstract void saveBody(JsonObject dst);

	public final void saveTo(JsonObject dst) {
		if (dst == null) throw new IllegalArgumentException("object is null");
		dst.putInteger(CProp.schema, schema());
		saveBody(dst);
	}

	public abstract int schema();
}
