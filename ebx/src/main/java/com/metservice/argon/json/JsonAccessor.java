/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ICodedEnum;
import com.metservice.argon.TimeOfDayFactory;
import com.metservice.argon.TimeOfDayRule;
import com.metservice.argon.TimeZoneFactory;

/**
 * @author roach
 */
public class JsonAccessor {

	private static final int MaxFqDepth = 100;

	private String fqn() {
		return fqn(0);
	}

	private String fqn(int depth) {
		if (m_oBase == null || depth > MaxFqDepth) return name;
		final boolean isIndex = ArgonText.isDigit(name.charAt(0));
		final StringBuilder sb = new StringBuilder();
		sb.append(m_oBase.fqn(depth + 1));
		if (isIndex) {
			sb.append('[');
			sb.append(name);
			sb.append(']');
		} else {
			sb.append('.');
			sb.append(name);
		}
		return sb.toString();
	}

	public JsonArray datumArray()
			throws JsonSchemaException {
		return definedNonNull(JsonArray.class, JsonType.TArray);
	}

	public Binary datumBinary()
			throws JsonSchemaException {
		return definedNonNull(JsonBinary.class, JsonType.TBinary).value;
	}

	public boolean datumBoolean()
			throws JsonSchemaException {
		return definedNonNull(JsonBoolean.class, JsonType.TBoolean).value;
	}

	public <E extends ICodedEnum> E datumCoded(CodedEnumTable<E> table)
			throws JsonSchemaException {
		if (table == null) throw new IllegalArgumentException("object is null");
		final String qtw = datumQtwString();
		final E oEnum = table.find(qtw);
		if (oEnum == null) {
			final String m = "Code '" + qtw + "' identified by property '" + fqn() + "' is not a member of class "
					+ table.codeClassName();
			throw new JsonSchemaException(m);
		}
		return oEnum;
	}

	public Date datumDate()
			throws JsonSchemaException {
		return definedNonNull(JsonNumberTime.class, JsonType.TNumberTime).newDate();
	}

	public double datumDouble()
			throws JsonSchemaException {
		return definedNumberDoubleOrInteger().doubleValue();
	}

	public Elapsed datumElapsed()
			throws JsonSchemaException {
		return definedNonNull(JsonNumberElapsed.class, JsonType.TNumberElapsed).newElapsed();
	}

	public long datumElapsedMs()
			throws JsonSchemaException {
		return definedNonNull(JsonNumberElapsed.class, JsonType.TNumberElapsed).sms;
	}

	public int datumElapsedSecs()
			throws JsonSchemaException {
		final long secs = datumElapsedMs() / CArgon.LMS_PER_SEC;
		return UJson.intVerified(this, secs);
	}

	public float datumFloat()
			throws JsonSchemaException {
		final double d = datumDouble();
		return UJson.floatVerified(this, d);
	}

	public int datumInteger()
			throws JsonSchemaException {
		final long l = datumLong();
		return UJson.intVerified(this, l);
	}

	public long datumLong()
			throws JsonSchemaException {
		return definedNonNull(JsonNumberInteger.class, JsonType.TNumberInteger).value;
	}

	public JsonObject datumObject()
			throws JsonSchemaException {
		return definedNonNull(JsonObject.class, JsonType.TObject);
	}

	public Pattern datumPattern()
			throws JsonSchemaException {
		final String qtwSpec = datumQtwString();
		try {
			return Pattern.compile(qtwSpec);
		} catch (final PatternSyntaxException ex) {
			final String m = "Pattern specified by property '" + fqn() + "' is malformed..." + ex.getMessage();
			throw new JsonSchemaException(m);
		}
	}

	public String datumQtwEcmaName()
			throws JsonSchemaException {
		final String qtw = datumQtwString();
		try {
			return ArgonText.qtwEcmaName(qtw);
		} catch (final ArgonApiException ex) {
			throw new JsonSchemaException(exName(qtw, ex));
		}
	}

	public String datumQtwPosixName()
			throws JsonSchemaException {
		final String qtw = datumQtwString();
		try {
			return ArgonText.qtwPosixName(qtw);
		} catch (final ArgonApiException ex) {
			throw new JsonSchemaException(exName(qtw, ex));
		}
	}

	public String datumQtwString()
			throws JsonSchemaException {
		final String zValue = datumZString();
		final String ztwValue = zValue.trim();
		if (ztwValue.length() > 0) return ztwValue;
		final String m = "String value of property '" + fqn()
				+ "' is empty  or all whitespace, but expected one or more non-whitespace characters";
		throw new JsonSchemaException(m);
	}

	public TimeOfDayRule datumTimeOfDayRule()
			throws JsonSchemaException {
		final String qtwSpec = datumQtwString();
		try {
			return TimeOfDayFactory.newRule(qtwSpec);
		} catch (final ArgonApiException ex) {
			final String m = "Time of day rule '" + qtwSpec + "' specified by property '" + fqn() + "' is malformed..."
					+ ex.getMessage();
			throw new JsonSchemaException(m);
		}
	}

	public TimeZone datumTimeZone()
			throws JsonSchemaException {
		final String qtwId = datumQtwString();
		try {
			return TimeZoneFactory.selectById(qtwId);
		} catch (final ArgonApiException ex) {
			final String m = "Time zone '" + qtwId + "' identified by property '" + fqn() + "' is unsupported..."
					+ ex.getMessage();
			throw new JsonSchemaException(m);
		}
	}

	public long datumTs()
			throws JsonSchemaException {
		return definedNonNull(JsonNumberTime.class, JsonType.TNumberTime).ts;
	}

	public String datumZString()
			throws JsonSchemaException {
		return definedNonNull(JsonString.class, JsonType.TString).zValue;
	}

	public String datumZtwString()
			throws JsonSchemaException {
		return datumZString().trim();
	}

	public IJsonNative definedNonNull()
			throws JsonSchemaException {
		final JsonType jsonType = jsonType();
		if (jsonType == JsonType.TNull) {
			final String m = "Property '" + fqn() + "' is null";
			throw new JsonSchemaException(m);
		}
		return value();
	}

	public <T extends IJsonNative> T definedNonNull(Class<T> tclass, JsonType oExpected)
			throws JsonSchemaException {
		final IJsonNative value = definedNonNull();
		if (tclass.isInstance(value)) return tclass.cast(value);
		final String actualType = oJsonType.title;
		final String expectedType = oExpected == null ? tclass.getName() : oExpected.title;
		final String m = "Value of property '" + fqn() + "' is type " + actualType + ", but expected " + expectedType;
		throw new JsonSchemaException(m);
	}

	public IJsonNativeNumber definedNumber()
			throws JsonSchemaException {
		final IJsonNative value = definedNonNull();
		if (value instanceof JsonNumberInteger) return (JsonNumberInteger) value;
		if (value instanceof JsonNumberDouble) return (JsonNumberDouble) value;
		if (value instanceof JsonNumberElapsed) return (JsonNumberElapsed) value;
		if (value instanceof JsonNumberTime) return (JsonNumberTime) value;

		final String actualType = oJsonType.title;
		final String m = "Value of property '" + fqn() + "' is type " + actualType + ", but expected a Number";
		throw new JsonSchemaException(m);
	}

	public IJsonNativeNumber definedNumberDoubleOrInteger()
			throws JsonSchemaException {
		final IJsonNative value = definedNonNull();
		if (value instanceof JsonNumberDouble) return (JsonNumberDouble) value;
		if (value instanceof JsonNumberInteger) return (JsonNumberInteger) value;
		final String actualType = oJsonType.title;
		final String m = "Value of property '" + fqn() + "' is type " + actualType + ", but expected a Double or Integer";
		throw new JsonSchemaException(m);
	}

	public String exName(String zcc, Throwable ex) {
		if (zcc == null) throw new IllegalArgumentException("object is null");
		if (ex == null) throw new IllegalArgumentException("object is null");
		return "Name '" + zcc + "' identified by property '" + fqn() + "' is malformed..." + ex.getMessage();
	}

	public String fullyQualifiedName() {
		return fqn(0);
	}

	public boolean isDefined() {
		return oValue != null && oJsonType != null;
	}

	public boolean isDefinedNonNull() {
		return oValue != null && oJsonType != null && oJsonType != JsonType.TNull;
	}

	public boolean isDefinedNull() {
		return oValue != null && oJsonType != null && oJsonType == JsonType.TNull;
	}

	public JsonType jsonType()
			throws JsonSchemaException {
		if (oJsonType == null) {
			final String m = "Property '" + fqn() + "' has a value which is JSON-incompatible";
			throw new JsonSchemaException(m);
		}
		return oJsonType;
	}

	@Override
	public String toString() {
		return name + ":" + (oValue == null ? "undefined" : oValue.toString());
	}

	public IJsonNative value()
			throws JsonSchemaException {
		if (oValue == null) {
			final String m = "Property '" + fqn() + "' is undefined";
			throw new JsonSchemaException(m);
		}
		return oValue;
	}

	public JsonAccessor(String name, IJsonNative oValue) {
		this(name, oValue, null);
	}

	public JsonAccessor(String name, IJsonNative oValue, JsonAccessor oBase) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("string is null or empty");
		this.name = name;
		this.oValue = oValue;
		this.oJsonType = oValue == null ? null : oValue.getJsonType();
		m_oBase = oBase;
	}

	public final String name;
	public final IJsonNative oValue;
	public JsonType oJsonType;
	private final JsonAccessor m_oBase;
}
