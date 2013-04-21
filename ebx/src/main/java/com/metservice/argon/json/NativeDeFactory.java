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
class NativeDeFactory implements IJsonDeFactory {

	public static final NativeDeFactory Instance = new NativeDeFactory();

	@Override
	public IJsonDeValue instanceNotNumber() {
		return JsonNumberDouble.NaN;
	}

	@Override
	public IJsonDeValue instanceNull() {
		return JsonNull.Instance;
	}

	@Override
	public IJsonDeArray newArray() {
		return JsonArray.newImmutable(16);
	}

	@Override
	public IJsonDeValue newBinary(Binary value) {
		return new JsonBinary(value);
	}

	@Override
	public IJsonDeValue newBoolean(boolean value) {
		return JsonBoolean.select(value);
	}

	@Override
	public IJsonDeValue newNumberDouble(double value) {
		return new JsonNumberDouble(value);
	}

	@Override
	public IJsonDeValue newNumberElapsed(long ms) {
		return new JsonNumberElapsed(ms);
	}

	@Override
	public IJsonDeValue newNumberInt(int value) {
		return new JsonNumberInteger(value);
	}

	@Override
	public IJsonDeValue newNumberTime(long ts) {
		return new JsonNumberTime(ts);
	}

	@Override
	public IJsonDeObject newObject() {
		return JsonObject.constructDecodeTarget(16);
	}

	@Override
	public IJsonDeValue newString(String zValue) {
		return new JsonString(zValue);
	}

	private NativeDeFactory() {
	}
}
