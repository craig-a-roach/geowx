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
class LexMultiplicativeExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs == null || m_oMultiplicativeOperatorToken == null) {
			m_unaryExpression.compile(cc);
		} else {
			m_oLhs.compile(cc);
			m_unaryExpression.compile(cc);
			final Punctuator punctuator = ((TokenPunctuator) m_oMultiplicativeOperatorToken).punctuator;
			switch (punctuator) {
				case STAR: {
					cc.add(VmMultiplicative.newMultiply());
				}
				break;
				case FWDSLASH: {
					cc.add(VmMultiplicative.newDivide());
				}
				break;
				case PERCENT: {
					cc.add(VmMultiplicative.newRemainder());
				}
				break;
				default: {
					throw new EsCompilerException("Unsupported Multiplicative Operator", cc
							.here(m_oMultiplicativeOperatorToken));
				}
			}
		}
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		if (m_oLhs != null && m_oMultiplicativeOperatorToken != null) {
			b.append(m_oLhs.toScript());
			b.append(m_oMultiplicativeOperatorToken.toScript());
		}
		b.append(m_unaryExpression.toScript());
		return b.toString();
	}

	public static LexMultiplicativeExpression createInstance(TokenReader tr, Alpha alpha,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexUnaryExpression oRhs = LexUnaryExpression.createInstance(tr, alpha, oLeftSideExpression);
		if (oRhs == null) return null;

		LexMultiplicativeExpression lhs = new LexMultiplicativeExpression(null, null, oRhs);
		while (tr.current().isMultiplicativeOperator()) {
			final Token operatorToken = tr.current();
			tr.consume();
			lhs = new LexMultiplicativeExpression(lhs, operatorToken, LexUnaryExpression.newInstance(tr, Alpha.Normal, null));
		}

		return lhs;
	}
	public static LexMultiplicativeExpression newInstance(TokenReader tr, Alpha alpha, LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexMultiplicativeExpression o = createInstance(tr, alpha, oLeftSideExpression);
		if (o == null) throw new EsSyntaxException("Expecting an MultiplicativeExpression", tr);
		return o;
	}
	public LexMultiplicativeExpression(LexMultiplicativeExpression oLhs, Token oMultiplicativeOperatorToken,
			LexUnaryExpression unaryExpression) {
		m_oLhs = oLhs;
		m_oMultiplicativeOperatorToken = oMultiplicativeOperatorToken;
		m_unaryExpression = unaryExpression;
	}

	private final LexMultiplicativeExpression m_oLhs;
	private final Token m_oMultiplicativeOperatorToken;
	private final LexUnaryExpression m_unaryExpression;
}
