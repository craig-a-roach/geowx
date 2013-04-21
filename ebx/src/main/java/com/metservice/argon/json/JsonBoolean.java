/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

/**
 * @author roach
 */
public class JsonBoolean implements IJsonBoolean, IJsonNative {

	private static final JsonBoolean InstanceTrue = new JsonBoolean(true);
	private static final JsonBoolean InstanceFalse = new JsonBoolean(false);

	public boolean equals(JsonBoolean rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return value == rhs.value;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonBoolean)) return false;
		return equals((JsonBoolean) o);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TBoolean;
	}

	@Override
	public int hashCode() {
		return value ? 1231 : 1237;
	}

	@Override
	public boolean jsonDatum() {
		return value;
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return this;
	}

	@Override
	public String toString() {
		return value ? "true" : "false";
	}

	public static JsonBoolean select(boolean value) {
		return value ? InstanceTrue : InstanceFalse;
	}

	private JsonBoolean(boolean value) {
		this.value = value;
	}

	public final boolean value;
}
