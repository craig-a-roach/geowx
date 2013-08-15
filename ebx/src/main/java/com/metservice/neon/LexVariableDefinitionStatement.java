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
class LexVariableDefinitionStatement extends LexStatement {

	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		m_variableDeclarationList.compile(cc);
		cc.add(VmSetCompletion.newNormal());
	}

	@Override
	public String toScript() {
		return "var " + m_variableDeclarationList.toScript() + ";\n";
	}
	public static LexVariableDefinitionStatement createVariableDefinition(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.VAR)) {
			final int lineIndex = tr.current().lineIndex;
			tr.consume();
			final LexVariableDeclarationList declarationList = LexVariableDeclarationList.newInstance(tr, Beta.AllowIn);
			tr.consumePunctuator(Punctuator.SEMICOLON);
			return new LexVariableDefinitionStatement(lineIndex, declarationList);
		}
		return null;
	}

	private LexVariableDefinitionStatement(int lineIndex, LexVariableDeclarationList variableDeclarationList) {
		super(lineIndex);
		m_variableDeclarationList = variableDeclarationList;
	}

	private final LexVariableDeclarationList m_variableDeclarationList;
}
