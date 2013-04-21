/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import com.metservice.argon.Binary;

/**
 * @author roach
 */
public final class JsonBinary implements IJsonBinary, IJsonNative {

	public static final JsonBinary Empty = new JsonBinary(Binary.Empty);

	public boolean equals(JsonBinary rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return value.equals(rhs.value);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonBinary)) return false;
		return equals((JsonBinary) o);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TBinary;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public Binary jsonDatum() {
		return value;
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return this;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	public static JsonBinary newInstance(Binary value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		return value.isEmpty() ? Empty : new JsonBinary(value);
	}

	JsonBinary(Binary value) {
		assert value != null;
		this.value = value;
	}

	public final Binary value;
}
