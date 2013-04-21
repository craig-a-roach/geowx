/*
 * Copyright 2005 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author roach
 */
public class MutableCodedEnumTable<T extends ICodedEnum> implements Iterable<T> {
	private String qccCode(ICodedEnum coded) {
		final String qCode = coded.qCode();
		if (qCode == null)
			throw new UnsupportedOperationException("The coded entry " + coded.toString() + " returned a null code");
		return qccCode(qCode);
	}

	private String qccCode(String qCode) {
		return m_caseSensitive ? qCode : qCode.toUpperCase();
	}

	public Class<T> codeClass() {
		return m_codeClass;
	}

	public String codeClassName() {
		return m_codeClass.getName();
	}

	public boolean contains(String qTargetCode) {
		if (qTargetCode == null || qTargetCode.length() == 0) throw new IllegalArgumentException("qTargetCode is empty");
		return m_zm.containsKey(qccCode(qTargetCode));
	}

	public T find(String qTargetCode) {
		if (qTargetCode == null || qTargetCode.length() == 0) throw new IllegalArgumentException("qTargetCode is empty");
		final String qccTargetCode = qccCode(qTargetCode);
		return m_zm.get(qccTargetCode);
	}

	public boolean isEmpty() {
		return m_zm.isEmpty();
	}

	public Iterator<T> iterator() {
		return m_zm.values().iterator();
	}

	public CodedEnumTable<T> newReadOnly() {
		return new CodedEnumTable<T>(m_codeClass, m_caseSensitive, m_zm);
	}

	public void put(T coded) {
		assert coded != null;
		final String qccCode = qccCode(coded);
		if (m_zm.put(qccCode, coded) != null)
			throw new IllegalArgumentException("The code " + coded.toString() + " is not unique in " + m_codeClass);
	}

	public T remove(String qTargetCode) {
		if (qTargetCode == null || qTargetCode.length() == 0) throw new IllegalArgumentException("qTargetCode is empty");
		final String qccTargetCode = qccCode(qTargetCode);
		return m_zm.remove(qccTargetCode);
	}

	public T select(String qTargetCode)
			throws ArgonFormatException {
		final T o = find(qTargetCode);
		if (o == null) throw new ArgonFormatException("Unknown " + m_codeClass + ": " + qTargetCode);
		return o;
	}

	@Override
	public String toString() {
		return codeClassName();
	}

	public Collection<T> values() {
		return m_zm.values();
	}

	public MutableCodedEnumTable(Class<T> codeClass, boolean caseSensitive, int initialCapacity) {
		if (codeClass == null) throw new IllegalArgumentException("codeClass is null");
		m_codeClass = codeClass;
		m_caseSensitive = caseSensitive;
		m_zm = new HashMap<String, T>(initialCapacity);
	}

	public MutableCodedEnumTable(Class<T> codeClass, boolean caseSensitive, T[] zptCoded) {
		if (codeClass == null) throw new IllegalArgumentException("codeClass is null");
		if (zptCoded == null) throw new IllegalArgumentException("zptCoded is null");
		m_codeClass = codeClass;
		m_caseSensitive = caseSensitive;
		final int reqd = zptCoded.length;
		m_zm = new HashMap<String, T>(reqd);
		for (int i = 0; i < zptCoded.length; i++) {
			put(zptCoded[i]);
		}
	}

	// Stable
	private final Class<T> m_codeClass;

	private final boolean m_caseSensitive;

	private final Map<String, T> m_zm;
}
