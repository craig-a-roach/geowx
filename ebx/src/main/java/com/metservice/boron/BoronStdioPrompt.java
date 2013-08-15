/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.nio.charset.Charset;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class BoronStdioPrompt {

	public static BoronStdioPrompt newAnywhere(byte[] xptBytes) {
		if (xptBytes == null || xptBytes.length == 0) throw new IllegalArgumentException("array is null or empty");
		return new BoronStdioPrompt(xptBytes, null, false);
	}

	public static BoronStdioPrompt newAnywhere(String qValue) {
		if (qValue == null || qValue.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return new BoronStdioPrompt(null, qValue, false);
	}

	public static BoronStdioPrompt newStartOfLine(byte[] xptBytes) {
		if (xptBytes == null || xptBytes.length == 0) throw new IllegalArgumentException("array is null or empty");
		return new BoronStdioPrompt(xptBytes, null, true);
	}

	public static BoronStdioPrompt newStartOfLine(String qValue) {
		if (qValue == null || qValue.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return new BoronStdioPrompt(null, qValue, true);
	}

	public boolean atStartOfLine() {
		return m_atStartOfLine;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("'");
		if (m_oqPrompt != null) {
			sb.append(m_oqPrompt);
		}
		if (m_oxptBytes != null) {
			sb.append(Ds.dump(m_oxptBytes));
		}
		sb.append("' ");
		sb.append(m_atStartOfLine ? "at start of line" : "anywhere");
		return sb.toString();
	}

	public byte[] xptBytes(Charset stdioEncoding) {
		if (stdioEncoding == null) throw new IllegalArgumentException("object is null");
		if (m_oqPrompt != null) return m_oqPrompt.getBytes(stdioEncoding);
		if (m_oxptBytes != null) return m_oxptBytes;
		throw new IllegalStateException("Malformed prompt");
	}

	private BoronStdioPrompt(byte[] oxptBytes, String oqPrompt, boolean atStartOfLine) {
		m_oxptBytes = oxptBytes;
		m_oqPrompt = oqPrompt;
		m_atStartOfLine = atStartOfLine;
	}

	private final byte[] m_oxptBytes;
	private final String m_oqPrompt;
	private final boolean m_atStartOfLine;
}
