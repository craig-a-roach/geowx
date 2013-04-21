/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.journal;

import com.metservice.argon.Binary;
import com.metservice.argon.DateFormatter;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class ArgonJournalIn implements Comparable<ArgonJournalIn> {

	@Override
	public int compareTo(ArgonJournalIn rhs) {
		if (m_serial < rhs.m_serial) return -1;
		if (m_serial > rhs.m_serial) return +1;
		return 0;
	}

	public boolean isWorkInProgress() {
		return m_wip;
	}

	public String qccType() {
		return m_qccType;
	}

	public long serial() {
		return m_serial;
	}

	public Binary source() {
		return m_source;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("serial", m_serial);
		ds.a("type", m_qccType);
		ds.a("wip", m_wip);
		ds.a("lastModified", DateFormatter.newT8FromTs(m_tsLastModified));
		ds.a("source", m_source);
		return ds.s();
	}

	public long tsLastModified() {
		return m_tsLastModified;
	}

	public ArgonJournalIn(long serial, String qccType, boolean wip, Binary source, long tsLastModified) {
		if (qccType == null || qccType.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (source == null) throw new IllegalArgumentException("object is null");
		m_serial = serial;
		m_qccType = qccType;
		m_wip = wip;
		m_source = source;
		m_tsLastModified = tsLastModified;
	}

	private final long m_serial;
	private final String m_qccType;
	private final boolean m_wip;
	private final Binary m_source;
	private final long m_tsLastModified;
}
