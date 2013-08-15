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
public final class EsIntrinsicBoolean extends EsObject {

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	public final IJsonNative createJsonNative() {
		return m_value.createJsonNative();
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicBoolean(this);
	}

	@Override
	public String esClass() {
		return "Boolean";
	}

	@Override
	public JsonType getJsonType() {
		return m_value.getJsonType();
	}

	public void setValue(EsPrimitiveBoolean value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		m_value = value;
	}

	@Override
	public String show(int depth) {
		return m_value.show(0);
	}

	public EsPrimitiveBoolean value() {
		return m_value;
	}

	public EsIntrinsicBoolean(EsObject prototype) {
		super(prototype);
	}

	private EsPrimitiveBoolean m_value = EsPrimitiveBoolean.FALSE;
}
