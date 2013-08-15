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
class LexReturnStatement extends LexStatement {

	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oExpression != null) {
			m_oExpression.compile(cc);
			cc.add(new VmGetValue(false));
		}
		cc.add(VmSetCompletion.newReturn());
	}

	@Override
	public String toScript() {
		return "return" + (m_oExpression == null ? "" : " " + m_oExpression.toScript()) + ";\n";
	}

	public static LexReturnStatement createReturn(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.RETURN)) {
			final int lineIndex = tr.current().lineIndex;
			tr.consume();
			LexExpression oExpression = null;
			if (!tr.current().isPunctuator(Punctuator.SEMICOLON)) {
				oExpression = LexExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn);
			}
			tr.consumePunctuator(Punctuator.SEMICOLON);
			return new LexReturnStatement(lineIndex, oExpression);
		}
		return null;
	}

	private LexReturnStatement(int lineIndex, LexExpression oExpression) {
		super(lineIndex);
		m_oExpression = oExpression;
	}

	private final LexExpression m_oExpression;
}
