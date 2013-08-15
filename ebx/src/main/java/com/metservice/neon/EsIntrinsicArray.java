/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.json.IJsonArray;
import com.metservice.argon.json.IJsonDeArray;
import com.metservice.argon.json.IJsonDeValue;
import com.metservice.argon.json.IJsonNative;
import com.metservice.argon.json.IJsonValue;
import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonNull;
import com.metservice.argon.json.JsonType;

/**
 * 
 * @author roach
 */
public final class EsIntrinsicArray extends EsObject implements IJsonArray, IJsonDeArray {

	private static final int MAX_DETAIL_LENGTH = 256;

	private void ensureLength(int neoLength, boolean changeLengthProperty) {
		if (neoLength > m_length) {
			if (changeLengthProperty) {
				putLengthMutable(neoLength);
			}
			m_length = neoLength;
		}
	}

	// Private
	private void initLength() {
		m_length = 0;
		putLengthMutable(m_length);
	}

	private void setLength(int neoLength, boolean trimElements, boolean changeLengthProperty) {
		if (neoLength < 0) throw new IllegalArgumentException("Invalid length:" + neoLength);

		if (trimElements && neoLength < m_length) {
			for (int index = neoLength; index < m_length; index++) {
				delete(UNeon.toPropertyName(index));
			}
		}

		if (changeLengthProperty && neoLength != m_length) {
			putLengthMutable(neoLength);
		}

		m_length = neoLength;
	}

	@Override
	protected String canonizePropertyKey(String zccPropertyKey) {
		if (zccPropertyKey.length() == 0) return zccPropertyKey;
		final char c0 = zccPropertyKey.charAt(0);
		if (c0 != '-') return zccPropertyKey;

		try {
			final int iPropertyKey = Integer.parseInt(zccPropertyKey);
			if (iPropertyKey >= 0) return zccPropertyKey;
			final int iCanonPropertyName = m_length + iPropertyKey;
			return Integer.toString(iCanonPropertyName);
		} catch (final NumberFormatException exNF) {
			return zccPropertyKey;
		}
	}

	@Override
	protected void cascadeLengthUpdate(int neoLength) {
		setLength(neoLength, false, false);
	}

	@Override
	protected void cascadeUpdate(String zccPropertyKey, IEsOperand neoPropertyValue) {
		if (UNeon.isLengthProperty(zccPropertyKey)) {
			final int neoLength = UNeon.intNonNegativeVerified(zccPropertyKey, neoPropertyValue);
			setLength(neoLength, true, false);
		} else {
			final int iPropertyKey = UNeon.toPositiveInteger(zccPropertyKey);
			if (iPropertyKey >= 0) {
				final int neoLength = iPropertyKey + 1;
				ensureLength(neoLength, true);
			}
		}
	}

	@Override
	protected void loadProperties(EsExecutionContext ecx) {
	}

	@Override
	public IJsonNative createJsonNative() {
		return newJsonArray(true);
	}

	@Override
	public EsObject createObject() {
		return new EsIntrinsicArray(this);
	}

	@Override
	public String esClass() {
		return EsIntrinsicArrayConstructor.ClassName;
	}

	@Override
	public JsonType getJsonType() {
		return JsonType.TArray;
	}

	@Override
	public void jsonAdd(IJsonDeValue value) {
		jsonAdd(m_length, value);
	}

	@Override
	public void jsonAdd(int memberIndex, IJsonDeValue value) {
		if (value instanceof IEsOperand) {
			putByIndex(memberIndex, (IEsOperand) value);
		}
	}

	@Override
	public int jsonMemberCount() {
		return m_length;
	}

	@Override
	public IJsonValue jsonValue(int memberIndex) {
		return getByIndex(memberIndex);
	}

	public int length() {
		return m_length;
	}

	public JsonArray newJsonArray(boolean retainIndex) {
		final List<IJsonNative> neoValueList = new ArrayList<IJsonNative>(m_length);
		for (int i = 0; i < m_length; i++) {
			final IEsOperand esOperand = getByIndex(i);
			final IJsonNative oJsonNative = esOperand.createJsonNative();
			if (oJsonNative == null) {
				if (retainIndex) {
					neoValueList.add(JsonNull.Instance);
				}
			} else {
				neoValueList.add(oJsonNative);
			}
		}
		return JsonArray.newImmutable(neoValueList);
	}

	public void put(IEsOperand[] zptPropertyValues) {
		if (zptPropertyValues == null) throw new IllegalArgumentException("zptPropertyValues is null");
		for (int index = 0; index < zptPropertyValues.length; index++) {
			put(UNeon.toPropertyName(index), zptPropertyValues[index]);
		}
		ensureLength(zptPropertyValues.length, true);
	}

	public void put(IEsOperand[] zPropertyValues, int length) {
		if (zPropertyValues == null) throw new IllegalArgumentException("zPropertyValues is null");
		final int readLength = Math.min(length, zPropertyValues.length);

		for (int index = 0; index < readLength; index++) {
			final IEsOperand oPropertyValue = zPropertyValues[index];
			if (oPropertyValue != null) {
				put(UNeon.toPropertyName(index), oPropertyValue);
			}
		}
		ensureLength(length, true);
	}

	public void put(List<? extends IEsOperand> zloPropertyValues) {
		if (zloPropertyValues == null) throw new IllegalArgumentException("zloPropertyValues is null");
		final int length = zloPropertyValues.size();
		for (int index = 0; index < length; index++) {
			final IEsOperand oPropertyValue = zloPropertyValues.get(index);
			if (oPropertyValue != null) {
				put(UNeon.toPropertyName(index), oPropertyValue);
			}
		}
		ensureLength(length, true);
	}

	public void putByIndex(int index, IEsOperand propertyValue) {
		if (index < 0) throw new IllegalArgumentException("Invalid index:" + index);
		put(UNeon.toPropertyName(index), propertyValue);
		ensureLength(index + 1, true);
	}

	public void putOnly(IEsOperand propertyValue) {
		if (propertyValue == null) throw new IllegalArgumentException("propertyValue is null");
		put(UNeon.toPropertyName(0), propertyValue);
		ensureLength(1, true);
	}

	public void setLength(int neoLength) {
		setLength(neoLength, true, true);
	}

	@Override
	public String show(int depth) {
		final StringBuilder b = new StringBuilder();
		if (depth > 0) {
			b.append('[');
			final int cap = Math.min(m_length, MAX_DETAIL_LENGTH);
			for (int index = 0; index < cap; index++) {
				if (index > 0) {
					b.append(',');
				}
				b.append(esGet(UNeon.toPropertyName(index)).show(depth - 1));
			}
			if (cap < m_length) {
				b.append("...length=" + m_length);
			}
			b.append(']');
		} else {
			b.append("array[").append(m_length).append("]");
		}
		return b.toString();
	}

	public EsIntrinsicArray(EsObject prototype) {
		super(prototype);
		initLength();
	}

	private int m_length;
}
