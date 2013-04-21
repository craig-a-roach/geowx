/*
 * Copyright 2005 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author roach
 */
public class CodedEnumTable<T extends ICodedEnum> implements Iterable<T> {

	private void put(T coded) {
		assert coded != null;
		final String qccCode = qccCode(coded);
		if (m_zm.put(qccCode, coded) != null)
			throw new IllegalArgumentException("The code " + coded.toString() + " is not unique in " + m_codeClass);
	}

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

	public int compare(T oLhs, T oRhs) {
		return oLhs == null ? -1 : (oRhs == null ? 1 : oLhs.qCode().compareTo(oRhs.qCode()));
	}

	public boolean contains(String qTargetCode) {
		if (qTargetCode == null || qTargetCode.length() == 0) throw new IllegalArgumentException("qTargetCode is empty");
		return m_zm.containsKey(qccCode(qTargetCode));
	}

	public boolean contains(T candidate) {
		if (candidate == null) throw new IllegalArgumentException("candidate is null");
		return contains(candidate.qCode());
	}

	public T find(String qTargetCode) {
		if (qTargetCode == null || qTargetCode.length() == 0) throw new IllegalArgumentException("qTargetCode is empty");
		final String qccTargetCode = qccCode(qTargetCode);
		return m_zm.get(qccTargetCode);
	}

	public Iterator<T> iterator() {
		return m_zm.values().iterator();
	}

	public List<T> new_zlValuesAsc() {
		return new_zlValuesAsc(null);
	}

	public List<T> new_zlValuesAsc(Comparator<T> oComparator) {
		final List<T> zlAsc = new ArrayList<T>(m_zm.values());
		if (oComparator == null) {
			final Comparator<T> cmp = new Comparator<T>() {

				public int compare(T lhs, T rhs) {
					return lhs.qCode().compareTo(rhs.qCode());
				}
			};
			Collections.sort(zlAsc, cmp);
		} else {
			Collections.sort(zlAsc, oComparator);
		}
		return zlAsc;
	}

	public String qCommaValues() {
		final StringBuilder b = new StringBuilder();
		for (final String qCode : m_zm.keySet()) {
			if (b.length() > 0) {
				b.append(", ");
			}
			b.append(qCode);
		}
		return b.toString();
	}

	public T read(ObjectInput in)
			throws IOException {
		if (in == null) throw new IllegalArgumentException("object is null");
		return read(in.readUTF());
	}

	public T read(String qTargetCode)
			throws IOException {
		if (qTargetCode == null || qTargetCode.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final T o = find(qTargetCode);
		if (o == null)
			throw new InvalidObjectException("Unknown member of enumerated code " + m_codeClass.getName() + ": "
					+ qTargetCode);
		return o;
	}

	public T select(String qTargetCode)
			throws ArgonFormatException {
		final T o = find(qTargetCode);
		if (o == null)
			throw new ArgonFormatException("Unknown member of enumerated code " + m_codeClass.getName() + ": " + qTargetCode);
		return o;
	}

	@Override
	public String toString() {
		return codeClassName();
	}

	public Collection<T> values() {
		return m_zm.values();
	}

	public void write(T coded, ObjectOutput out)
			throws IOException {
		if (coded == null) throw new IllegalArgumentException("coded is null");
		out.writeUTF(coded.qCode());
	}

	CodedEnumTable(Class<T> codeClass, boolean caseSensitive, Map<String, T> zm) {
		assert codeClass != null;
		assert zm != null;
		m_codeClass = codeClass;
		m_caseSensitive = caseSensitive;
		m_zm = zm;
	}

	public CodedEnumTable(Class<T> codeClass, boolean caseSensitive, T[] zptCoded) {
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
