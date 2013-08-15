/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.Date;

import com.metservice.argon.Binary;
import com.metservice.argon.DateFactory;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BerylliumBinaryHttpPayload {

	public static BerylliumBinaryHttpPayload newEmpty(long tsLastModified) {
		return new BerylliumBinaryHttpPayload(CBeryllium.BinaryContentType, Binary.Empty, tsLastModified);
	}

	public Binary content() {
		return m_content;
	}

	public Date lastModified() {
		return DateFactory.newDate(m_tsLastModified);
	}

	public String qlcContentType() {
		return m_qlcContentType;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("contentType", m_qlcContentType);
		ds.at8("lastModified", m_tsLastModified);
		ds.a("content", m_content);
		return ds.s();
	}

	public long tsLastModified() {
		return m_tsLastModified;
	}

	public BerylliumBinaryHttpPayload(String qlcContentType, Binary content, long tsLastModified) {
		if (qlcContentType == null || qlcContentType.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (content == null) throw new IllegalArgumentException("object is null");
		m_qlcContentType = qlcContentType;
		m_content = content;
		m_tsLastModified = tsLastModified;
	}

	private final String m_qlcContentType;
	private final Binary m_content;
	private final long m_tsLastModified;
}
