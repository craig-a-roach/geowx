/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Binary;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.JsonBinary;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsIntrinsicBinary extends EsObject {

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return JsonBinary.newInstance(m_value);
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicBinary(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicBinaryConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TBinary;
	}

	public void setValue(Binary value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		m_value = value;
	}

	@Override
	public String show(int depth) {
		return m_value.dump(1 + depth);
	}

	public Binary value() {
		return m_value;
	}

	public EsIntrinsicBinary(EsObject prototype) {
		super(prototype);
	}

	private Binary m_value = Binary.Empty;
}
