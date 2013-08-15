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
public final class EsIntrinsicString extends EsObject {

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return m_value.createJsonNative();
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicString(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicStringConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return m_value.getJsonType();
	}

	public void setValue(EsPrimitiveString value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		m_value = value;
		putLengthReadOnly(value.length());
	}

	@Override
	public String show(int depth) {
		return m_value.show(0);
	}

	public EsPrimitiveString value() {
		return m_value;
	}

	public EsIntrinsicString(EsObject prototype) {
		super(prototype);
	}

	private EsPrimitiveString m_value = EsPrimitiveString.EMPTY;
}
