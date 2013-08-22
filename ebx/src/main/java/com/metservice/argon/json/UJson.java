/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import java.util.List;

import com.metservice.argon.DateFormatter;
import com.metservice.argon.ElapsedFormatter;

/**
 * @author roach
 */
class UJson {

	private static void encodeArray(StringBuilder dest, String zPath, JsonArray src) {
		assert src != null;
		final int memberCount = src.jsonMemberCount();
		for (int i = 0; i < memberCount; i++) {
			final IJsonValue jsonValue = src.jsonValue(i);
			encodeProperty(dest, zPath, Integer.toString(i), jsonValue);
		}
	}

	private static void encodeLhs(StringBuilder dest, String zPath, String jsonName) {
		if (zPath.length() > 0) {
			dest.append(zPath);
			dest.append('_');
		}
		dest.append(jsonName);
		dest.append('=');
	}

	private static void encodeObject(StringBuilder dest, String zPath, JsonObject src) {
		assert src != null;
		final List<String> zlNames = src.jsonNames();
		final int nameCount = zlNames.size();
		for (int i = 0; i < nameCount; i++) {
			final String jsonName = zlNames.get(i);
			final IJsonValue jsonValue = src.jsonValue(jsonName);
			encodeProperty(dest, zPath, jsonName, jsonValue);
		}
	}

	private static void encodeProperty(StringBuilder dest, String zPath, String jsonName, IJsonValue jsonValue) {
		if (jsonValue instanceof JsonString) {
			final JsonString stringValue = (JsonString) jsonValue;
			encodeLhs(dest, zPath, jsonName);
			dest.append(stringValue.zValue);
			dest.append('\n');
			return;
		}
		if (jsonValue instanceof JsonNumberInteger) {
			final JsonNumberInteger integerValue = (JsonNumberInteger) jsonValue;
			encodeLhs(dest, zPath, jsonName);
			dest.append(integerValue.value);
			dest.append('\n');
			return;
		}
		if (jsonValue instanceof JsonNumberElapsed) {
			final JsonNumberElapsed elapsedValue = (JsonNumberElapsed) jsonValue;
			encodeLhs(dest, zPath, jsonName);
			dest.append(ElapsedFormatter.formatSingleUnit(elapsedValue.sms));
			dest.append('\n');
			return;
		}
		if (jsonValue instanceof JsonNumberDouble) {
			final JsonNumberDouble doubleValue = (JsonNumberDouble) jsonValue;
			encodeLhs(dest, zPath, jsonName);
			dest.append(doubleValue.value);
			dest.append('\n');
			return;
		}
		if (jsonValue instanceof JsonNumberTime) {
			final JsonNumberTime timeValue = (JsonNumberTime) jsonValue;
			encodeLhs(dest, zPath, jsonName);
			dest.append(DateFormatter.newT8FromTs(timeValue.ts));
			dest.append('\n');
			return;
		}
		if (jsonValue instanceof JsonBoolean) {
			final JsonBoolean booleanValue = (JsonBoolean) jsonValue;
			encodeLhs(dest, zPath, jsonName);
			dest.append(booleanValue.value);
			dest.append('\n');
			return;
		}
		if (jsonValue instanceof JsonNull) {
			encodeLhs(dest, zPath, jsonName);
			dest.append('\n');
			return;
		}
		if (jsonValue instanceof JsonBinary) {
			final JsonBinary binaryValue = (JsonBinary) jsonValue;
			encodeLhs(dest, zPath, jsonName);
			dest.append(binaryValue.value.newB64MIME());
			dest.append('\n');
			return;
		}
		if (jsonValue instanceof JsonObject) {
			final String neoPath = zPath.length() == 0 ? jsonName : (zPath + "_" + jsonName);
			encodeObject(dest, neoPath, (JsonObject) jsonValue);
			return;
		}
		if (jsonValue instanceof JsonArray) {
			final String neoPath = zPath.length() == 0 ? jsonName : (zPath + "_" + jsonName);
			encodeArray(dest, neoPath, (JsonArray) jsonValue);
		}
	}

	public static String encodePropertiesString(JsonObject src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		final StringBuilder sb = new StringBuilder(1024);
		encodeObject(sb, "", src);
		return sb.toString();
	}

	public static float floatVerified(JsonAccessor acc, double d)
			throws JsonSchemaException {
		final double ad = Math.abs(d);
		if (ad < Float.MIN_VALUE || ad > Float.MAX_VALUE) {
			final String fqn = acc.fullyQualifiedName();
			final String m = "Value '" + d + "' of property '" + fqn + "' exceeds range (float) limits";
			throw new JsonSchemaException(m);
		}
		return (float) d;
	}

	public static int intVerified(JsonAccessor acc, long l)
			throws JsonSchemaException {
		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
			final String fqn = acc.fullyQualifiedName();
			final String m = "Value '" + l + "' of property '" + fqn + "' exceeds range (int)  limits";
			throw new JsonSchemaException(m);
		}
		return (int) l;
	}

}
