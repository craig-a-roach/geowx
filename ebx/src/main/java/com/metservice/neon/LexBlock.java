/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author roach
 */
class LexBlock extends LexStatement {

	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		for (int i = 0; i < m_zptStatements.length; i++) {
			final LexStatement statement = m_zptStatements[i];
			statement.compile(cc);
		}
	}

	@Override
	public String toScript() {
		final StringBuilder b = new StringBuilder();
		b.append("{\n");
		for (int i = 0; i < m_zptStatements.length; i++) {
			b.append(m_zptStatements[i].toScript());
		}
		b.append("}\n");
		return b.toString();
	}

	public static LexBlock createBlock(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isPunctuator(Punctuator.LBRACE)) {
			final int lineIndexBegin = tr.current().lineIndex;
			tr.consume();
			final List<LexStatement> zlStatements = new ArrayList<LexStatement>();
			while (!tr.current().isPunctuator(Punctuator.RBRACE)) {
				zlStatements.add(LexStatement.newStatement(tr));
			}
			tr.consumePunctuator(Punctuator.RBRACE);
			return new LexBlock(lineIndexBegin, zlStatements);
		}
		return null;
	}

	public static LexBlock newBlock(TokenReader tr)
			throws EsSyntaxException {
		final LexBlock o = createBlock(tr);
		if (o == null) throw new EsSyntaxException("Expecting a statement block", tr);
		return o;
	}
	private LexBlock(int lineIndexBegin, List<LexStatement> zlStatements) {
		super(lineIndexBegin);
		assert zlStatements != null;
		m_zptStatements = zlStatements.toArray(EMPTY);
	}

	private static final LexStatement[] EMPTY = new LexStatement[0];

	private final LexStatement[] m_zptStatements;
}
