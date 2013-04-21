/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

/**
 * @author roach
 */
public final class JsonString implements IJsonString, IJsonNative {

	public static final JsonString Empty = new JsonString("");

	public boolean equals(JsonString rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return zValue.equals(rhs.zValue);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonString)) return false;
		return equals((JsonString) o);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	@Override
	public int hashCode() {
		return zValue.hashCode();
	}

	@Override
	public String jsonDatum() {
		return zValue;
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return this;
	}

	@Override
	public String toString() {
		return zValue;
	}

	public static JsonString newInstance(String ozValue) {
		return (ozValue == null || ozValue.length() == 0) ? Empty : new JsonString(ozValue);
	}

	JsonString(String zValue) {
		assert zValue != null;
		this.zValue = zValue;
	}

	public final String zValue;
}
