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
public class EsCompletionThrow implements IEsOperand {

	@Override
	public IJsonNative createJsonNative() {
		return m_value.createJsonNative();
	}

	public EsType esType() {
		return m_value.esType();
	}

	@Override
	public JsonType getJsonType() {
		return m_value.getJsonType();
	}

	public String show(int depth) {
		return "thrown " + m_value.show(depth);
	}

	public boolean toCanonicalBoolean() {
		return m_value.toCanonicalBoolean();
	}

	public String toCanonicalString(EsExecutionContext ecx)
			throws InterruptedException {
		return m_value.toCanonicalString(ecx);
	}

	public EsPrimitiveNumber toNumber(EsExecutionContext ecx)
			throws InterruptedException {
		return m_value.toNumber(ecx);
	}

	public EsObject toObject(EsExecutionContext ecx)
			throws InterruptedException {
		return m_value.toObject(ecx);
	}

	public EsPrimitive toPrimitive(EsExecutionContext ecx, EsType oPreference)
			throws InterruptedException {
		return m_value.toPrimitive(ecx, oPreference);
	}

	@Override
	public String toString() {
		return show(1);
	}

	public IEsOperand value() {
		return m_value;
	}

	public EsCompletionThrow(IEsOperand value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		m_value = value;
	}

	private final IEsOperand m_value;
}
