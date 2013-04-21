/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.journal;

import com.metservice.argon.Binary;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class ArgonJournalOut {

	public String qccType() {
		return m_qccType;
	}

	public Binary source() {
		return m_source;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("type", m_qccType);
		ds.a("source", m_source);
		return ds.s();
	}

	public ArgonJournalOut(String qccType, Binary source) {
		if (qccType == null || qccType.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (qccType.equals(ArgonJournalController.ArchiveType))
			throw new IllegalArgumentException("invalid qccType>" + qccType + "<");
		if (source == null) throw new IllegalArgumentException("object is null");
		m_qccType = qccType;
		m_source = source;
	}

	private final String m_qccType;
	private final Binary m_source;
}
