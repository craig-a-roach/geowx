/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.json.IJsonBoolean;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonBoolean;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsPrimitiveBoolean extends EsPrimitive implements IJsonBoolean {

	public static final EsPrimitiveBoolean TRUE = new EsPrimitiveBoolean(true);
	public static final EsPrimitiveBoolean FALSE = new EsPrimitiveBoolean(false);

	private static final EsPrimitiveString STRUE = new EsPrimitiveString("true");
	private static final EsPrimitiveString SFALSE = new EsPrimitiveString("false");

	@Override
	public IJsonNative createJsonNative() {
		return JsonBoolean.select(m_value);
	}

	public EsType esType() {
		return EsType.TBoolean;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TBoolean;
	}

	@Override
	public boolean jsonDatum() {
		return m_value;
	}

	public boolean sameBooleanValue(EsPrimitiveBoolean rhsBoolean) {
		return m_value == rhsBoolean.m_value;
	}

	public String show(int depth) {
		return m_value ? "true" : "false";
	}

	public boolean toCanonicalBoolean() {
		return m_value;
	}

	@Override
	public String toCanonicalString() {
		return m_value ? "true" : "false";
	}

	@Override
	public int toHash() {
		return m_value ? 1231 : 1237;
	}

	public EsPrimitiveNumber toNumber(EsExecutionContext ecx) {
		return m_value ? EsPrimitiveNumberInteger.ONE : EsPrimitiveNumberInteger.ZERO;
	}

	public EsObject toObject(EsExecutionContext ecx) {
		return ecx.global().newIntrinsicBoolean(this);
	}

	public EsPrimitiveString toPrimitiveString() {
		return m_value ? STRUE : SFALSE;
	}

	public static EsPrimitiveBoolean instance(boolean value) {
		return value ? TRUE : FALSE;
	}

	private EsPrimitiveBoolean(boolean value) {
		m_value = value;
	}

	private final boolean m_value;
}
