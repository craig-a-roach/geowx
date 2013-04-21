/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

/**
 * @author roach
 */
public class JsonNull implements IJsonNull, IJsonNative {

	public static final JsonNull Instance = new JsonNull();

	public boolean equals(JsonNull rhs) {
		return (rhs == this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonNull)) return false;
		return equals((JsonNull) o);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNull;
	}

	@Override
	public int hashCode() {
		return 1259;
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return this;
	}

	@Override
	public String toString() {
		return "null";
	}

	private JsonNull() {
	}
}
