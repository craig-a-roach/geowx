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
class LexVariableDeclarationList extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs != null) {
			m_oLhs.compile(cc);
		}
		m_variableDeclaration.compile(cc);
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		if (m_oLhs != null) {
			b.append(m_oLhs.toScript());
			b.append(",");
		}
		b.append(m_variableDeclaration.toScript());
		return b.toString();
	}

	public static LexVariableDeclarationList createInstance(TokenReader tr, Beta beta)
			throws EsSyntaxException {
		final LexVariableDeclaration oRhs = LexVariableDeclaration.createInstance(tr, beta, false);
		if (oRhs == null) return null;

		LexVariableDeclarationList lhs = new LexVariableDeclarationList(null, oRhs);
		while (tr.current().isPunctuator(Punctuator.COMMA)) {
			tr.consume();
			lhs = new LexVariableDeclarationList(lhs, LexVariableDeclaration.newInstance(tr, beta, false));
		}
		return lhs;
	}
	public static LexVariableDeclarationList newInstance(TokenReader tr, Beta beta)
			throws EsSyntaxException {
		final LexVariableDeclarationList o = createInstance(tr, beta);
		if (o == null) throw new EsSyntaxException("Expecting a VariableDeclarationList", tr);
		return o;
	}

	private LexVariableDeclarationList(LexVariableDeclarationList oLhs, LexVariableDeclaration variableDeclaration) {
		assert variableDeclaration != null;
		m_oLhs = oLhs;
		m_variableDeclaration = variableDeclaration;
	}

	private final LexVariableDeclarationList m_oLhs;

	private final LexVariableDeclaration m_variableDeclaration;
}
