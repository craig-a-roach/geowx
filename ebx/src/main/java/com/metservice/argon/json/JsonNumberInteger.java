/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import com.metservice.argon.HashCoder;

/**
 * @author roach
 */
public class JsonNumberInteger implements IJsonNumberInteger, IJsonNativeNumber {

	public static final JsonNumberInteger Zero = new JsonNumberInteger(0L);
	public static final JsonNumberInteger One = new JsonNumberInteger(1L);
	public static final JsonNumberInteger MinusOne = new JsonNumberInteger(-1L);

	@Override
	public double doubleValue() {
		return value;
	}

	public boolean equals(JsonNumberInteger rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return value == rhs.value;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonNumberInteger)) return false;
		return equals((JsonNumberInteger) o);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberInteger;
	}

	@Override
	public int hashCode() {
		return HashCoder.field(value);
	}

	@Override
	public long jsonDatum() {
		return value;
	}

	@Override
	public long longValue() {
		return value;
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return this;
	}

	@Override
	public String toString() {
		return Long.toString(value);
	}

	public static JsonNumberInteger newInstance(long value) {
		if (value == 0L) return Zero;
		if (value == 1L) return One;
		if (value == -1L) return MinusOne;
		return new JsonNumberInteger(value);
	}

	JsonNumberInteger(long value) {
		this.value = value;
	}

	public final long value;
}
