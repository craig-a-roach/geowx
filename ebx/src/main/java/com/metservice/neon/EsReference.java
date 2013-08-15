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
public class EsReference implements IEsOperand {

	@Override
	public IJsonNative createJsonNative() {
		return null;
	}

	public EsType esType() {
		return EsType.TReference;
	}

	public EsObject getBase() {
		return m_oBase;
	}

	@Override
	public JsonType getJsonType() {
		return null;
	}

	public String show(int depth) {
		final String qBase = m_oBase == null ? "?" : m_oBase.esClass();
		return qBase + "." + m_zccPropertyKey;
	}

	public boolean toCanonicalBoolean() {
		throw new EsInterpreterException("Cannot convert Reference (" + show(1) + ") to a canonical boolean");
	}

	public String toCanonicalString(EsExecutionContext ecx) {
		throw new EsInterpreterException("Cannot convert Reference (" + show(1) + ") to a canonical string");
	}

	public EsPrimitiveNumber toNumber(EsExecutionContext ecx) {
		throw new EsInterpreterException("Cannot convert Reference (" + show(1) + ") to a number");
	}

	public EsObject toObject(EsExecutionContext ecx) {
		throw new EsInterpreterException("Cannot convert Reference (" + show(1) + ") to an object");
	}

	public EsPrimitive toPrimitive(EsExecutionContext ecx, EsType oPreference) {
		throw new EsInterpreterException("Cannot convert Reference (" + show(1) + ") to a primitive");
	}

	@Override
	public String toString() {
		return show(1);
	}

	public String zccPropertyKey() {
		return m_zccPropertyKey;
	}

	public EsReference(EsObject oBase, String zccPropertyKey) {
		if (zccPropertyKey == null) throw new IllegalArgumentException("key is null");
		m_oBase = oBase;
		m_zccPropertyKey = zccPropertyKey;
	}

	private final EsObject m_oBase;
	private final String m_zccPropertyKey;
}
