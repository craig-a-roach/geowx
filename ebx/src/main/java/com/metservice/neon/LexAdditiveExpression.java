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
class LexAdditiveExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs == null || m_oAdditiveOperatorToken == null) {
			m_multiplicativeExpression.compile(cc);
		} else {
			m_oLhs.compile(cc);
			m_multiplicativeExpression.compile(cc);
			final Punctuator punctuator = ((TokenPunctuator) m_oAdditiveOperatorToken).punctuator;
			switch (punctuator) {
				case PLUS: {
					cc.add(VmAdditive.newPlus());
				}
				break;
				case MINUS: {
					cc.add(VmAdditive.newMinus());
				}
				break;
				default: {
					throw new EsCompilerException("Unsupported Additive Operator", cc.here(m_oAdditiveOperatorToken));
				}
			}
		}
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		if (m_oLhs != null && m_oAdditiveOperatorToken != null) {
			b.append(m_oLhs.toScript());
			b.append(m_oAdditiveOperatorToken.toScript());
		}
		b.append(m_multiplicativeExpression.toScript());
		return b.toString();
	}

	public static LexAdditiveExpression createInstance(TokenReader tr, Alpha alpha, LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexMultiplicativeExpression oRhs = LexMultiplicativeExpression.createInstance(tr, alpha, oLeftSideExpression);
		if (oRhs == null) return null;

		LexAdditiveExpression lhs = new LexAdditiveExpression(null, null, oRhs);
		while (tr.current().isAdditiveOperator()) {
			final Token operatorToken = tr.current();
			tr.consume();
			lhs = new LexAdditiveExpression(lhs, operatorToken, LexMultiplicativeExpression.newInstance(tr, Alpha.Normal,
					null));
		}
		return lhs;
	}
	public static LexAdditiveExpression newInstance(TokenReader tr, Alpha alpha, LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexAdditiveExpression o = createInstance(tr, alpha, oLeftSideExpression);
		if (o == null) throw new EsSyntaxException("Expecting an AdditiveExpression", tr);
		return o;
	}
	public LexAdditiveExpression(LexAdditiveExpression oLhs, Token oAdditiveOperatorToken,
			LexMultiplicativeExpression multiplicativeExpression) {
		m_oLhs = oLhs;
		m_oAdditiveOperatorToken = oAdditiveOperatorToken;
		m_multiplicativeExpression = multiplicativeExpression;
	}

	private final LexAdditiveExpression m_oLhs;

	private final Token m_oAdditiveOperatorToken;

	private final LexMultiplicativeExpression m_multiplicativeExpression;
}
