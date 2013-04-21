/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

/**
 * @author roach
 */
public class JsonNumberDouble implements IJsonNumberDouble, IJsonNativeNumber {

	public static final JsonNumberDouble NaN = new JsonNumberDouble(Double.NaN);

	@Override
	public double doubleValue() {
		return value;
	}

	public boolean equals(JsonNumberDouble rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return value == rhs.value;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonNumberDouble)) return false;
		return equals((JsonNumberDouble) o);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNumberDouble;
	}

	@Override
	public int hashCode() {
		final long bits = Double.doubleToLongBits(value);
		return (int) (bits ^ (bits >>> 32));
	}

	@Override
	public double jsonDatum() {
		return value;
	}

	@Override
	public long longValue() {
		return Math.round(value);
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return this;
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

	public JsonNumberDouble(double value) {
		this.value = value;
	}

	public final double value;
}
