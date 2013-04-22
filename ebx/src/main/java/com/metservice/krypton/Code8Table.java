/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class Code8Table<E> implements IResourceTable {

	private static final short ParameterLo = 0;
	private static final short ParameterHi = 254;

	public E find(int code) {
		if (code < ParameterLo || code > ParameterHi) return null;
		return m_type.cast(m_entryTable[code]);
	}

	public void put(int code, E oEntry) {
		if (code >= ParameterLo && code <= ParameterHi) {
			m_entryTable[code] = oEntry;
		}
	}

	@Override
	public String qccKey() {
		return m_qccKey;
	}

	public E select(IKryptonProbe probe, String source, int code)
			throws KryptonCodeException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		final E oEntry = find(code);
		if (oEntry == null) {
			probe.codeNotFound(source, m_type.getName(), m_qccKey, code);
			final String m = "Code '" + code + "' not found in resource '" + m_qccKey + "'";
			throw new KryptonCodeException(source, m);
		}
		return oEntry;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(m_type.getName() + "Table");
		ds.a("key", m_qccKey);
		ds.a("entryTable", m_entryTable);
		return ds.s();
	}

	public Code8Table(Class<E> type, String qccKey) {
		if (type == null) throw new IllegalArgumentException("object is null");
		if (qccKey == null || qccKey.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_type = type;
		m_qccKey = qccKey;
		m_entryTable = new Object[ParameterHi + 1];
	}
	private final Class<E> m_type;
	private final String m_qccKey;
	private final Object[] m_entryTable;
}
