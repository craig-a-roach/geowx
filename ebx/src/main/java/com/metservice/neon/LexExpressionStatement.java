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
class LexExpressionStatement extends LexStatement {
	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		m_expression.compile(cc);
		cc.add(VmSetCompletion.newNormal());
	}

	@Override
	public String toScript() {
		return m_expression.toScript() + ";\n";
	}

	public static LexExpressionStatement createExpression(TokenReader tr)
			throws EsSyntaxException {
		final int lineIndex = tr.current().lineIndex;
		final LexExpression oExpression = LexExpression.createInstance(tr, Alpha.Initial, Beta.AllowIn);
		if (oExpression != null) {
			tr.consumePunctuator(Punctuator.SEMICOLON);
			return new LexExpressionStatement(lineIndex, oExpression);
		}
		return null;
	}

	private LexExpressionStatement(int lineIndex, LexExpression expression) {
		super(lineIndex);
		m_expression = expression;
	}

	private final LexExpression m_expression;
}
