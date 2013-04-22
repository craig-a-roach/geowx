/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class KryptonEditionResult<T extends IKryptonEditionTable<T>> {

	public T editionTable() {
		return m_editionTable;
	}

	public KryptonManifestCounter manifestCounter() {
		return m_counter;
	}

	public KryptonEditionResult<T> newSum(KryptonEditionResult<T> oRhs) {
		if (oRhs == null) return this;
		final T neoTable = m_editionTable.newSum(oRhs.editionTable());
		final KryptonManifestCounter neoCounter = m_counter.newSum(oRhs.manifestCounter());
		return new KryptonEditionResult<>(neoTable, neoCounter);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("editionTable", m_editionTable);
		ds.a("counter", m_counter);
		return ds.s();
	}

	public KryptonEditionResult(T editionTable, KryptonManifestCounter counter) {
		if (editionTable == null) throw new IllegalArgumentException("object is null");
		if (counter == null) throw new IllegalArgumentException("object is null");
		m_editionTable = editionTable;
		m_counter = counter;
	}
	private final T m_editionTable;
	private final KryptonManifestCounter m_counter;
}
