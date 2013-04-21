/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ICodedEnum;
import com.metservice.argon.TimeZoneFormatter;

/**
 * @author roach
 */
public final class JsonObject implements IJsonObject, IJsonDeObject, IJsonNative, Map<String, IJsonNative> {

	private static final Map<String, IJsonNative> ZMVALUES = Collections.emptyMap();
	public static final JsonObject Empty = new JsonObject(ZMVALUES, true);

	static JsonObject constructDecodeTarget(int initialCapacity) {
		return new JsonObject(initialCapacity, true);
	}

	public static JsonObject newImmutable(Map<String, IJsonNative> src) {
		return new JsonObject(src, true);
	}

	public static JsonObject newMutable() {
		return new JsonObject(16, false);
	}

	public static JsonObject newMutable(int initialCapacity) {
		return new JsonObject(initialCapacity, false);
	}

	public static JsonObject newMutableInitStrings(Object... nameValuePairs) {
		if (nameValuePairs == null) throw new IllegalArgumentException("object is null");
		final int pairCount = nameValuePairs.length / 2;
		if (pairCount == 0) return Empty;
		final JsonObject neo = new JsonObject(pairCount, false);
		for (int p = 0, iname = 0, ivalue = 1; p < pairCount; p++, iname += 2, ivalue += 2) {
			final Object oName = nameValuePairs[iname];
			final Object oValue = nameValuePairs[ivalue];
			if (oName == null || oValue == null) {
				continue;
			}
			final String ztwName = oName.toString().trim();
			if (ztwName.length() == 0) {
				continue;
			}
			final String zValue = oValue.toString();
			neo.putString(ztwName, zValue);
		}
		return neo;
	}

	@Override
	protected Object clone()
			throws CloneNotSupportedException {
		return newReplica(true);
	}

	public JsonAccessor accessor(String name) {
		return accessor(name, null);
	}

	public JsonAccessor accessor(String name, JsonAccessor oBase) {
		if (name == null || name.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return new JsonAccessor(name, m_propertyValueMap.get(name), oBase);
	}

	@Override
	public void clear() {
		if (m_immutable) throw new UnsupportedOperationException("clear not supported; immutable");
		m_propertyValueMap.clear();
	}

	@Override
	public boolean containsKey(Object name) {
		return m_propertyValueMap.containsKey(name);
	}

	@Override
	public boolean containsValue(Object value) {
		return m_propertyValueMap.containsValue(value);
	}

	@Override
	public Set<Map.Entry<String, IJsonNative>> entrySet() {
		return m_propertyValueMap.entrySet();
	}

	public boolean equals(JsonObject rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_propertyValueMap.equals(rhs.m_propertyValueMap);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonObject)) return false;
		return equals((JsonObject) o);
	}

	@Override
	public IJsonNative get(Object key) {
		return m_propertyValueMap.get(key);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TObject;
	}

	@Override
	public int hashCode() {
		return m_propertyValueMap.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return m_propertyValueMap.isEmpty();
	}

	@Override
	public List<String> jsonNames() {
		final List<String> namesAsc = new ArrayList<String>(m_propertyValueMap.keySet());
		Collections.sort(namesAsc);
		return namesAsc;
	}

	@Override
	public void jsonPut(String name, IJsonDeValue value) {
		if (!(value instanceof IJsonNative)) {
			final String m = "Property " + name + " is class " + value.getClass() + "; expecting native";
			throw new IllegalArgumentException(m);
		}
		m_propertyValueMap.put(name, (IJsonNative) value);
	}

	@Override
	public IJsonValue jsonValue(String name) {
		return m_propertyValueMap.get(name);
	}

	@Override
	public Set<String> keySet() {
		return m_propertyValueMap.keySet();
	}

	public JsonObject newReplica(boolean immutable) {
		final int count = m_propertyValueMap.size();
		final Map<String, IJsonNative> neoPropertyValueMap = new HashMap<String, IJsonNative>(count);
		for (final Map.Entry<String, IJsonNative> e : m_propertyValueMap.entrySet()) {
			final IJsonNative value = e.getValue();
			final IJsonNative neoValue = value.replicate(immutable);
			neoPropertyValueMap.put(e.getKey(), neoValue);
		}
		return new JsonObject(neoPropertyValueMap, immutable);
	}

	@Override
	public IJsonNative put(String name, IJsonNative value) {
		if (m_immutable) throw new UnsupportedOperationException("put not supported; immutable");
		return m_propertyValueMap.put(name, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends IJsonNative> m) {
		if (m_immutable) throw new UnsupportedOperationException("putAll not supported; immutable");
		m_propertyValueMap.putAll(m);
	}

	public IJsonNative putBinary(String name, Binary oValue) {
		return put(name, JsonBinary.newInstance(oValue));
	}

	public IJsonNative putBoolean(String name, boolean value) {
		return put(name, JsonBoolean.select(value));
	}

	public IJsonNative putCoded(String name, ICodedEnum coded) {
		if (coded == null) throw new IllegalArgumentException("object is null");
		return put(name, JsonString.newInstance(coded.qCode()));
	}

	public IJsonNative putDouble(String name, double value) {
		return put(name, new JsonNumberDouble(value));
	}

	public IJsonNative putElapsed(String name, Elapsed value) {
		return put(name, JsonNumberElapsed.newInstance(value));
	}

	public IJsonNative putElapsedMs(String name, long sms) {
		return put(name, JsonNumberElapsed.newInstance(sms));
	}

	public IJsonNative putElapsedSecs(String name, int ssecs) {
		return put(name, JsonNumberElapsed.newInstance(ssecs * CArgon.LMS_PER_SEC));
	}

	public IJsonNative putFloat(String name, float value) {
		return put(name, new JsonNumberDouble(value));
	}

	public IJsonNative putInteger(String name, int value) {
		return put(name, JsonNumberInteger.newInstance(value));
	}

	public IJsonNative putLong(String name, long value) {
		return put(name, JsonNumberInteger.newInstance(value));
	}

	public IJsonNative putNull(String name) {
		return put(name, JsonNull.Instance);
	}

	public IJsonNative putString(String name, String ozValue) {
		return put(name, JsonString.newInstance(ozValue));
	}

	public IJsonNative putTime(String name, Date time) {
		return put(name, new JsonNumberTime(time));
	}

	public IJsonNative putTime(String name, long ts) {
		return put(name, new JsonNumberTime(ts));
	}

	public IJsonNative putTimeZone(String name, TimeZone tz) {
		return put(name, new JsonString(TimeZoneFormatter.id(tz)));
	}

	@Override
	public IJsonNative remove(Object name) {
		if (m_immutable) throw new UnsupportedOperationException("remove not supported; immutable");
		return m_propertyValueMap.remove(name);
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return newReplica(immutable);
	}

	@Override
	public int size() {
		return m_propertyValueMap.size();
	}

	public String toPropertiesString() {
		return UJson.encodePropertiesString(this);
	}

	@Override
	public String toString() {
		return JsonEncoder.Default.encode(this);
	}

	@Override
	public Collection<IJsonNative> values() {
		return m_propertyValueMap.values();
	}

	private JsonObject(int initialCapacity, boolean immutable) {
		m_propertyValueMap = new HashMap<String, IJsonNative>(initialCapacity);
		m_immutable = immutable;
	}

	private JsonObject(Map<String, IJsonNative> src, boolean immutable) {
		if (src == null) throw new IllegalArgumentException("object is null");
		m_propertyValueMap = src;
		m_immutable = immutable;
	}

	private final Map<String, IJsonNative> m_propertyValueMap;
	private final boolean m_immutable;
}
