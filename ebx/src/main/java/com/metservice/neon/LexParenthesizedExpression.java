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
class LexParenthesizedExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		m_expression.compile(cc);
	}

	@Override
	public String toScript() {
		return "(" + m_expression.toScript() + ")";
	}

	public static LexParenthesizedExpression createInstance(TokenReader tr)
			throws EsSyntaxException {
		final Token tokenL = tr.current();
		if (tokenL.isPunctuator(Punctuator.LPAREN)) {
			tr.consume();
			final LexExpression oExpression = LexExpression.createInstance(tr, Alpha.Normal, Beta.AllowIn);
			if (oExpression == null) throw new EsSyntaxException("Expecting an Expression", tr);
			tr.consumePunctuator(Punctuator.RPAREN);
			return new LexParenthesizedExpression(oExpression);
		}
		return null;
	}

	public static LexParenthesizedExpression newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexParenthesizedExpression o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a ParenthesizedExpression", tr);
		return o;
	}

	private LexParenthesizedExpression(LexExpression expression) {
		assert expression != null;
		m_expression = expression;
	}

	private final LexExpression m_expression;
}
