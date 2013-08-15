/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsPrimitiveUndefined extends EsPrimitive {

	public static final EsPrimitiveUndefined Instance = new EsPrimitiveUndefined();

	@Override
	public IJsonNative createJsonNative() {
		return null;
	}

	public EsType esType() {
		return EsType.TUndefined;
	}

	@Override
	public JsonType getJsonType() {
		return null;
	}

	public String show(int depth) {
		return "undefined";
	}

	public boolean toCanonicalBoolean() {
		return false;
	}

	@Override
	public String toCanonicalString() {
		return "undefined";
	}

	@Override
	public int toHash() {
		return 1259;
	}

	public EsPrimitiveNumber toNumber(EsExecutionContext ecx) {
		return EsPrimitiveNumberNot.Instance;
	}

	public EsObject toObject(EsExecutionContext ecx) {
		throw new EsTypeCodeException("Cannot convert undefined to Object");
	}

	private EsPrimitiveUndefined() {
	}
}
