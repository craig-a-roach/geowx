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
class LexThrowStatement extends LexStatement {

	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		m_expression.compile(cc);
		cc.add(new VmGetValue(false));
		cc.add(VmSetCompletion.newThrow());
	}

	@Override
	public String toScript() {
		return "throw " + m_expression.toScript() + ";\n";
	}

	public static LexThrowStatement createThrow(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.THROW)) {
			final int lineIndex = tr.current().lineIndex;
			tr.consume();
			final LexExpression expression = LexExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn);
			tr.consumePunctuator(Punctuator.SEMICOLON);
			return new LexThrowStatement(lineIndex, expression);
		}
		return null;
	}

	private LexThrowStatement(int lineIndex, LexExpression expression) {
		super(lineIndex);
		m_expression = expression;
	}

	private final LexExpression m_expression;
}
