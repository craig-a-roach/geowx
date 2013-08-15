/*
 * Copyright 2008 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
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
class LexCaseClause extends CompileableLex {
	public int clauseLineIndex() {
		return m_clauseLineIndex;
	}

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
	}

	public LexExpression getExpression() {
		return m_oExpression;
	}

	@Override
	public String toScript() {
		final StringBuilder b = new StringBuilder();
		if (m_oExpression == null) {
			b.append("default:\n");
		} else {
			b.append("case ");
			b.append(m_oExpression.toScript());
			b.append(":\n");
		}
		for (int i = 0; i < m_zptStatements.length; i++) {
			b.append(m_zptStatements[i].toScript());
		}
		return b.toString();
	}

	public LexStatement[] zptStatements() {
		return m_zptStatements;
	}

	public static LexCaseClause createInstance(TokenReader tr)
			throws EsSyntaxException {
		final Token current = tr.current();
		final boolean isCase = current.isKeyword(Keyword.CASE);
		if (isCase || current.isKeyword(Keyword.DEFAULT)) {
			final int clauseLineIndex = current.lineIndex;
			tr.consume();
			final LexExpression oExpression;
			if (isCase) {
				oExpression = LexExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn);
			} else {
				oExpression = null;
			}
			tr.consumePunctuator(Punctuator.COLON);
			List<LexStatement> oxlStatements = null;
			boolean moreStatements = true;
			while (moreStatements) {
				final LexStatement oStatement = LexStatement.createStatement(tr);
				if (oStatement == null) {
					moreStatements = false;
				} else {
					if (oxlStatements == null) {
						oxlStatements = new ArrayList<LexStatement>();
					}
					oxlStatements.add(oStatement);
				}
			}
			return new LexCaseClause(clauseLineIndex, oExpression, oxlStatements);
		}

		return null;
	}

	public static LexCaseClause newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexCaseClause o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a CaseClause", tr);
		return o;
	}

	private LexCaseClause(int clauseLineIndex, LexExpression oExpression, List<LexStatement> oxlStatements) {
		m_clauseLineIndex = clauseLineIndex;
		m_oExpression = oExpression;
		if (oxlStatements == null || oxlStatements.size() == 0) {
			m_zptStatements = EMPTY;
		} else {
			m_zptStatements = oxlStatements.toArray(EMPTY);
		}
	}

	private static final LexStatement[] EMPTY = new LexStatement[0];
	private final int m_clauseLineIndex;

	private final LexExpression m_oExpression;

	private final LexStatement[] m_zptStatements;
}
