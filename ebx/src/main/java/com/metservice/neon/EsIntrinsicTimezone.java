/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.TimeZone;

import com.metservice.argon.TimeZoneFactory;
import com.metservice.argon.TimeZoneFormatter;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonString;
import com.metservice.argon.json.JsonString;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsIntrinsicTimezone extends EsObject implements IJsonString {

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return JsonString.newInstance(jsonDatum());
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicTimezone(this);
	}

	public boolean equivalentTo(TimeZone rhs) {
		if (rhs == null) throw new IllegalArgumentException("object is null");
		return m_value.equals(rhs) || m_value.hasSameRules(rhs);
	}

	@Override
	public String esClass() {
		return EsIntrinsicTimezoneConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TString;
	}

	@Override
	public String jsonDatum() {
		return TimeZoneFormatter.id(m_value);
	}

	public void setValue(TimeZone value) {
		if (value == null) throw new IllegalArgumentException("value is null");
		m_value = value;
	}

	@Override
	public String show(int depth) {
		return m_value.getID();
	}

	public TimeZone timeZoneValue() {
		return m_value;
	}

	public EsPrimitiveString toPrimitiveString() {
		return new EsPrimitiveString(jsonDatum());
	}

	public EsIntrinsicTimezone(EsObject prototype) {
		super(prototype);
	}

	private TimeZone m_value = TimeZoneFactory.GMT;
}
