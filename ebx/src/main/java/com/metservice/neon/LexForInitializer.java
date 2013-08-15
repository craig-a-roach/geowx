/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * 
 * @author roach
 */
class LexForInitializer extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_orExpression != null) {
			m_orExpression.compile(cc);
			return;
		}

		if (m_orVariableDeclarationList != null) {
			m_orVariableDeclarationList.compile(cc);
			return;
		}
	}

	@Override
	public String toScript() {
		if (m_orExpression != null) return m_orExpression.toScript();
		if (m_orVariableDeclarationList != null) return "var " + m_orVariableDeclarationList.toScript();
		return "";
	}

	public static LexForInitializer createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.VAR)) {
			tr.consume();
			return new LexForInitializer(LexVariableDeclarationList.newInstance(tr, Beta.NoIn));
		}

		final LexExpression oExpression = LexExpression.createInstance(tr, Alpha.Normal, Beta.NoIn);
		if (oExpression != null) return new LexForInitializer(oExpression);

		return null;
	}
	public static LexForInitializer newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexForInitializer o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a ForInitializer", tr);
		return o;
	}
	private LexForInitializer(LexExpression expression) {
		assert expression != null;
		m_orExpression = expression;
		m_orVariableDeclarationList = null;
	}

	private LexForInitializer(LexVariableDeclarationList variableDeclarationList) {
		assert variableDeclarationList != null;
		m_orExpression = null;
		m_orVariableDeclarationList = variableDeclarationList;
	}

	private final LexExpression m_orExpression;
	private final LexVariableDeclarationList m_orVariableDeclarationList;
}
