/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.collection.DynamicArrayInt;

/**
 * 
 * @author roach
 */
class TokenReader {
	void consume() {
		m_pos++;
	}

	void consumePunctuator(Punctuator punctuator)
			throws EsSyntaxException {
		if (punctuator == null) throw new IllegalArgumentException("punctuator is null");
		final Token token = current();
		if (token.isPunctuator(punctuator)) {
			consume();
			return;
		}
		throw new EsSyntaxException("Missing " + punctuator.qDescription, here(), lineIndex());
	}

	void mark() {
		m_marks.push(m_pos);
	}

	void reset() {
		if (!m_marks.isEmpty()) {
			m_pos = m_marks.pop();
		}
	}

	public Token current() {
		return m_pos < m_posEof ? m_xptTokens[m_pos] : m_xptTokens[m_posEof];
	}

	public String here() {
		final Token token = current();
		return m_scriptSource.lineHere(token.lineIndex, token.startIndex);
	}

	public int lineIndex() {
		return current().lineIndex;
	}

	public boolean more() {
		return m_pos < m_posEof;
	}

	public EsParsedProgram newParsedProgram()
			throws EsSyntaxException {
		return EsParsedProgram.newInstance(this);
	}

	public EsSourceHtml newSourceHtml() {
		final String qAuthors = m_scriptSource.meta().qAuthors;
		final String qPurpose = m_scriptSource.meta().qPurpose;
		return EsSourceHtml.newInstance(m_xptTokens, qAuthors, qPurpose);
	}

	public Token peek() {
		final int peekPos = m_pos + 1;
		return peekPos < m_posEof ? m_xptTokens[peekPos] : m_xptTokens[m_posEof];
	}

	public EsSource scriptSource() {
		return m_scriptSource;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < m_xptTokens.length; i++) {
			if (i > 0) {
				b.append('\n');
			}
			final boolean pa = (i == m_pos);
			final boolean ma = (!m_marks.isEmpty() && i == m_marks.last());
			if (ma) {
				b.append(">>");
			}
			if (pa) {
				b.append('>');
			}
			b.append(m_xptTokens[i].toScript());
			if (pa) {
				b.append('<');
			}
		}
		return b.toString();
	}

	public TokenReader(EsSource scriptSource, Token[] xptTokens) {
		if (scriptSource == null) throw new IllegalArgumentException("object is null");
		if (xptTokens == null || xptTokens.length == 0) throw new IllegalArgumentException("array is null or empty");
		m_scriptSource = scriptSource;
		m_xptTokens = xptTokens;
		m_posEof = xptTokens.length - 1;
	}

	private final EsSource m_scriptSource;
	private final Token[] m_xptTokens;
	private final int m_posEof;
	private int m_pos;
	private final DynamicArrayInt m_marks = new DynamicArrayInt(4);
}
