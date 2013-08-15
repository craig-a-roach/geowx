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
class LexCaseBlock extends CompileableLex {
	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
	}

	public LexCaseClause getDefaultCaseClause() {
		return m_oDefaultCaseClause;
	}

	@Override
	public String toScript() {
		final StringBuilder b = new StringBuilder();
		b.append("{\n");
		for (int i = 0; i < m_zptSpecificCaseClauses.length; i++) {
			b.append(m_zptSpecificCaseClauses[i].toScript());
		}
		if (m_oDefaultCaseClause != null) {
			b.append(m_oDefaultCaseClause.toScript());
		}
		b.append("}\n");
		return b.toString();
	}

	public LexCaseClause[] zptSpecificCaseClauses() {
		return m_zptSpecificCaseClauses;
	}

	public static LexCaseBlock createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isPunctuator(Punctuator.LBRACE)) {
			tr.consume();
			final List<LexCaseClause> zlSpecificCaseClauses = new ArrayList<LexCaseClause>();
			LexCaseClause oDefaultCaseClause = null;
			while (!tr.current().isPunctuator(Punctuator.RBRACE)) {
				final LexCaseClause caseClause = LexCaseClause.newInstance(tr);
				final LexExpression oCaseExpression = caseClause.getExpression();
				if (oCaseExpression == null) {
					if (oDefaultCaseClause == null) {
						oDefaultCaseClause = caseClause;
					} else
						throw new EsSyntaxException("Ambiguous default case clause", tr);
				} else {
					zlSpecificCaseClauses.add(caseClause);
				}
			}
			tr.consumePunctuator(Punctuator.RBRACE);
			return new LexCaseBlock(zlSpecificCaseClauses, oDefaultCaseClause);
		}

		return null;
	}

	public static LexCaseBlock newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexCaseBlock o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a CaseBlock", tr);
		return o;
	}

	private LexCaseBlock(List<LexCaseClause> zlSpecificCaseClauses, LexCaseClause oDefaultCaseClause) {
		assert zlSpecificCaseClauses != null;
		m_zptSpecificCaseClauses = zlSpecificCaseClauses.toArray(EMPTY);
		m_oDefaultCaseClause = oDefaultCaseClause;
	}

	private static final LexCaseClause[] EMPTY = new LexCaseClause[0];
	private final LexCaseClause[] m_zptSpecificCaseClauses;
	private final LexCaseClause m_oDefaultCaseClause;
}
