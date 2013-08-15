/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonNull;
import com.metservice.argon.json.JsonNull;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsPrimitiveNull extends EsPrimitive implements IJsonNull {

	public static final EsPrimitiveNull Instance = new EsPrimitiveNull();

	@Override
	public IJsonNative createJsonNative() {
		return JsonNull.Instance;
	}

	public EsType esType() {
		return EsType.TNull;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TNull;
	}

	public String show(int depth) {
		return "null";
	}

	public boolean toCanonicalBoolean() {
		return false;
	}

	@Override
	public String toCanonicalString() {
		return "null";
	}

	@Override
	public int toHash() {
		return 1259;
	}

	public EsPrimitiveNumber toNumber(EsExecutionContext ecx) {
		return EsPrimitiveNumberInteger.ZERO;
	}

	public EsObject toObject(EsExecutionContext ecx) {
		throw new EsTypeCodeException("Cannot convert null to Object");
	}

	private EsPrimitiveNull() {
	}
}
