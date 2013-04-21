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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public class JsonArray implements IJsonArray, IJsonDeArray, IJsonNative, List<IJsonNative> {

	private static final List<IJsonNative> ZLVALUES = Collections.emptyList();
	public static final JsonArray Empty = new JsonArray(ZLVALUES, true);

	public static JsonArray newImmutable() {
		return new JsonArray(16, true);
	}

	public static JsonArray newImmutable(int initialCapacity) {
		return new JsonArray(initialCapacity, true);
	}

	public static JsonArray newImmutable(List<IJsonNative> src) {
		return new JsonArray(src, true);
	}

	public static JsonArray newImmutableFromStrings(List<String> zl) {
		if (zl == null) throw new IllegalArgumentException("object is null");
		final int srcCount = zl.size();
		final List<IJsonNative> src = new ArrayList<IJsonNative>(srcCount);
		for (int i = 0; i < srcCount; i++) {
			final String ozValue = zl.get(i);
			final IJsonNative value = ozValue == null ? JsonNull.Instance : JsonString.newInstance(ozValue);
			src.add(value);
		}
		return new JsonArray(src, true);
	}

	public static JsonArray newMutable() {
		return new JsonArray(16, false);
	}

	public static JsonArray newMutable(int initialCapacity) {
		return new JsonArray(initialCapacity, false);
	}

	@Override
	protected Object clone()
			throws CloneNotSupportedException {
		return newReplica(true);
	}

	public JsonAccessor accessor(int index) {
		return accessor(index, null);
	}

	public JsonAccessor accessor(int index, JsonAccessor oBase) {
		final int count = m_valueList.size();
		final int cindex = index < 0 ? count + index : index;
		final IJsonNative oValue = cindex >= 0 && cindex < count ? m_valueList.get(cindex) : null;
		return new JsonAccessor(Integer.toString(index), oValue, oBase);
	}

	@Override
	public boolean add(IJsonNative e) {
		if (m_immutable) throw new UnsupportedOperationException("add not supported; immutable");
		return m_valueList.add(e);
	}

	@Override
	public void add(int index, IJsonNative element) {
		if (m_immutable) throw new UnsupportedOperationException("add not supported; immutable");
		m_valueList.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends IJsonNative> c) {
		if (m_immutable) throw new UnsupportedOperationException("addAll not supported; immutable");
		return m_valueList.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends IJsonNative> c) {
		if (m_immutable) throw new UnsupportedOperationException("addAll not supported; immutable");
		return m_valueList.addAll(index, c);
	}

	public boolean addBoolean(boolean value) {
		return add(JsonBoolean.select(value));
	}

	public boolean addDouble(double value) {
		return add(new JsonNumberDouble(value));
	}

	public boolean addElapsed(Elapsed value) {
		return add(JsonNumberElapsed.newInstance(value));
	}

	public boolean addElapsed(long sms) {
		return add(JsonNumberElapsed.newInstance(sms));
	}

	public boolean addInteger(int value) {
		return add(JsonNumberInteger.newInstance(value));
	}

	public boolean addNull() {
		return add(JsonNull.Instance);
	}

	public boolean addString(String ozValue) {
		return add(JsonString.newInstance(ozValue));
	}

	public boolean addTime(Date time) {
		return add(new JsonNumberTime(time));
	}

	public boolean addTime(long ts) {
		return add(new JsonNumberTime(ts));
	}

	@Override
	public void clear() {
		if (m_immutable) throw new UnsupportedOperationException("clear not supported; immutable");
		m_valueList.clear();
	}

	@Override
	public boolean contains(Object o) {
		return m_valueList.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return m_valueList.containsAll(c);
	}

	public boolean equals(JsonArray rhs) {
		if (rhs == this) return true;
		if (rhs == null) return false;
		return m_valueList.equals(rhs.m_valueList);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof JsonArray)) return false;
		return equals((JsonArray) o);
	}

	@Override
	public IJsonNative get(int index) {
		return m_valueList.get(index);
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TArray;
	}

	@Override
	public int hashCode() {
		return m_valueList.hashCode();
	}

	@Override
	public int indexOf(Object o) {
		return m_valueList.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return m_valueList.isEmpty();
	}

	@Override
	public Iterator<IJsonNative> iterator() {
		return m_valueList.iterator();
	}

	@Override
	public void jsonAdd(IJsonDeValue value) {
		jsonAdd(m_valueList.size(), value);
	}

	@Override
	public void jsonAdd(int memberIndex, IJsonDeValue value) {
		if (value == null) throw new IllegalArgumentException("object is null");
		if (!(value instanceof IJsonNative)) {
			final String m = "Member " + memberIndex + " is class " + value.getClass() + "; expecting native";
			throw new IllegalArgumentException(m);
		}
		m_valueList.add((IJsonNative) value);
	}

	@Override
	public int jsonMemberCount() {
		return m_valueList.size();
	}

	@Override
	public IJsonValue jsonValue(int memberIndex) {
		return m_valueList.get(memberIndex);
	}

	@Override
	public int lastIndexOf(Object o) {
		return m_valueList.lastIndexOf(o);
	}

	@Override
	public ListIterator<IJsonNative> listIterator() {
		return m_valueList.listIterator();
	}

	@Override
	public ListIterator<IJsonNative> listIterator(int index) {
		return m_valueList.listIterator(index);
	}

	public List<String> new_zlqtwStrings()
			throws JsonSchemaException {
		final int count = m_valueList.size();
		final List<String> zl = new ArrayList<String>(count);
		for (int i = 0; i < count; i++) {
			zl.add(accessor(i).datumQtwString());
		}
		return zl;
	}

	public IJsonDeValue[] new_zptValues() {
		return toArray(new IJsonDeValue[m_valueList.size()]);
	}

	public JsonArray newReplica(boolean immutable) {
		final int count = m_valueList.size();
		final List<IJsonNative> neoValueList = new ArrayList<IJsonNative>(count);
		for (int i = 0; i < count; i++) {
			final IJsonNative value = m_valueList.get(i);
			final IJsonNative neoValue = value.replicate(immutable);
			neoValueList.add(neoValue);
		}
		return new JsonArray(neoValueList, immutable);
	}

	@Override
	public IJsonNative remove(int index) {
		if (m_immutable) throw new UnsupportedOperationException("remove not supported; immutable");
		return m_valueList.remove(index);
	}

	@Override
	public boolean remove(Object o) {
		if (m_immutable) throw new UnsupportedOperationException("remove not supported; immutable");
		return m_valueList.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (m_immutable) throw new UnsupportedOperationException("removeAll not supported; immutable");
		return m_valueList.removeAll(c);
	}

	@Override
	public IJsonNative replicate(boolean immutable) {
		return newReplica(immutable);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (m_immutable) throw new UnsupportedOperationException("retainAll not supported; immutable");
		return m_valueList.retainAll(c);
	}

	@Override
	public IJsonNative set(int index, IJsonNative element) {
		if (m_immutable) throw new UnsupportedOperationException("set not supported; immutable");
		return m_valueList.set(index, element);
	}

	@Override
	public int size() {
		return m_valueList.size();
	}

	@Override
	public List<IJsonNative> subList(int fromIndex, int toIndex) {
		return m_valueList.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return m_valueList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return m_valueList.toArray(a);
	}

	@Override
	public String toString() {
		return JsonEncoder.Default.encode(this);
	}

	private JsonArray(int initialCapacity, boolean immutable) {
		m_valueList = new ArrayList<IJsonNative>(initialCapacity);
		m_immutable = immutable;
	}

	private JsonArray(List<IJsonNative> src, boolean immutable) {
		if (src == null) throw new IllegalArgumentException("object is null");
		m_valueList = src;
		m_immutable = immutable;
	}

	private final List<IJsonNative> m_valueList;
	private final boolean m_immutable;
}
