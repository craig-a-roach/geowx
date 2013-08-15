/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.nio.charset.Charset;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
class StdioPromptMatcher {

	private static final int CR = ArgonText.CH_ASCII_CR;
	private static final int LF = ArgonText.CH_ASCII_LF;

	public static StdioPromptMatcher createInstance(BoronStdioPrompt oPrompt, Charset stdioEncoding) {
		if (oPrompt == null) return null;
		if (stdioEncoding == null) throw new IllegalArgumentException("object is null");
		final byte[] xptBytes = oPrompt.xptBytes(stdioEncoding);
		final boolean atStartOfLine = oPrompt.atStartOfLine();
		return new StdioPromptMatcher(xptBytes, atStartOfLine);
	}

	public boolean matches(int r) {
		if (r == LF || r == CR || r == -1) {
			m_bcMatch = 0;
			m_satisfied = false;
			return false;
		}

		if (m_satisfied) return false;

		final byte br = (byte) r;
		final byte bm = m_xptBytes[m_bcMatch];
		if (br != bm) {
			m_bcMatch = 0;
			m_satisfied = m_atStartOfLine;
			return false;
		}

		m_bcMatch++;
		final boolean matches;
		if (m_bcMatch == m_bcDepth) {
			matches = true;
			m_bcMatch = 0;
			m_satisfied = m_atStartOfLine;
		} else {
			matches = false;
		}
		return matches;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("bcMatch", m_bcMatch);
		ds.a("satisfied", m_satisfied);
		ds.a("bcDepth", m_bcDepth);
		ds.a("xptBytes", m_xptBytes);
		return ds.s();
	}

	private StdioPromptMatcher(byte[] xptBytes, boolean atStartOfLine) {
		assert xptBytes != null;
		m_xptBytes = xptBytes;
		m_bcDepth = xptBytes.length;
		m_atStartOfLine = atStartOfLine;
	}

	private final byte[] m_xptBytes;
	private final int m_bcDepth;
	private final boolean m_atStartOfLine;
	private int m_bcMatch;
	private boolean m_satisfied;
}
