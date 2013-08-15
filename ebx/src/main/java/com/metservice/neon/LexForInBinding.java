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
class LexForInBinding extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_orLeftSideExpression != null) {
			m_orLeftSideExpression.compile(cc);
			return;
		}

		if (m_orVariableDeclaration != null) {
			m_orVariableDeclaration.compile(cc);
			return;
		}
	}

	@Override
	public String toScript() {
		if (m_orLeftSideExpression != null) return m_orLeftSideExpression.toScript();
		if (m_orVariableDeclaration != null) return "var " + m_orVariableDeclaration.toScript();
		return "";
	}

	public static LexForInBinding createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.VAR)) {
			tr.consume();
			return new LexForInBinding(LexVariableDeclaration.newInstance(tr, Beta.NoIn, true));
		}

		final LexLeftSideExpression oLeftSideExpression = LexLeftSideExpression.createInstance(tr, Alpha.Normal);
		if (oLeftSideExpression != null) return new LexForInBinding(oLeftSideExpression);

		return null;
	}
	public static LexForInBinding newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexForInBinding o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a ForInBinding", tr);
		return o;
	}
	private LexForInBinding(LexLeftSideExpression leftSideExpression) {
		assert leftSideExpression != null;
		m_orLeftSideExpression = leftSideExpression;
		m_orVariableDeclaration = null;
	}

	private LexForInBinding(LexVariableDeclaration variableDeclaration) {
		assert variableDeclaration != null;
		m_orLeftSideExpression = null;
		m_orVariableDeclaration = variableDeclaration;
	}

	private final LexLeftSideExpression m_orLeftSideExpression;

	private final LexVariableDeclaration m_orVariableDeclaration;
}
