/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.DecimalMask;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonString;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsIntrinsicDecimalmask extends EsObject implements IJsonString {

	public static final DecimalMask DEFAULT_DECIMALMASK = DecimalMask.FixedNegSignNatural;

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return JsonString.newInstance(jsonDatum());
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicDecimalmask(this);
	}

	public DecimalMask decimalMaskValue() {
		return m_value;
	}

	@Override
	public String esClass() {
		return EsIntrinsicDecimalmaskConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	@Override
	public String jsonDatum() {
		return m_value.mask();
	}

	public void setValue(DecimalMask value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		m_value = value;
	}

	@Override
	public String show(int depth) {
		return m_value.mask();
	}

	public EsPrimitiveString toPrimitiveString() {
		return new EsPrimitiveString(jsonDatum());
	}

	public EsIntrinsicDecimalmask(EsObject prototype) {
		super(prototype);
	}

	private DecimalMask m_value = DEFAULT_DECIMALMASK;
}
