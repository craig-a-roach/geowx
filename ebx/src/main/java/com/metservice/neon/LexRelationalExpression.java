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
class LexRelationalExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs == null || m_oRelationalOperatorToken == null) {
			m_additiveExpression.compile(cc);
		} else {
			m_oLhs.compile(cc);
			m_additiveExpression.compile(cc);
			final Punctuator punctuator = ((TokenPunctuator) m_oRelationalOperatorToken).punctuator;
			switch (punctuator) {
				case LT_RELATION: {
					cc.add(VmRelational.newLT());
				}
				break;
				case LEQ_RELATION: {
					cc.add(VmRelational.newLEQ());
				}
				break;
				case GT_RELATION: {
					cc.add(VmRelational.newGT());
				}
				break;
				case GEQ_RELATION: {
					cc.add(VmRelational.newGEQ());
				}
				break;
				default: {
					throw new EsCompilerException("Unsupported Relational Operator", cc.here(m_oRelationalOperatorToken));
				}
			}
		}
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		if (m_oLhs != null && m_oRelationalOperatorToken != null) {
			b.append(m_oLhs.toScript());
			b.append(m_oRelationalOperatorToken.toScript());
		}
		b.append(m_additiveExpression.toScript());
		return b.toString();
	}

	public static LexRelationalExpression createInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexAdditiveExpression oRhs = LexAdditiveExpression.createInstance(tr, alpha, oLeftSideExpression);
		if (oRhs == null) return null;

		LexRelationalExpression lhs = new LexRelationalExpression(null, null, oRhs);
		while (tr.current().isRelationalOperator(beta)) {
			final Token operatorToken = tr.current();
			tr.consume();
			lhs = new LexRelationalExpression(lhs, operatorToken, LexAdditiveExpression.newInstance(tr, Alpha.Normal, null));
		}
		return lhs;
	}
	public static LexRelationalExpression newInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexRelationalExpression o = createInstance(tr, alpha, beta, oLeftSideExpression);
		if (o == null) throw new EsSyntaxException("Expecting an EqualityExpression", tr);
		return o;
	}
	private LexRelationalExpression(LexRelationalExpression oLhs, Token oRelationalOperatorToken,
			LexAdditiveExpression additiveExpression) {
		m_oLhs = oLhs;
		m_oRelationalOperatorToken = oRelationalOperatorToken;
		m_additiveExpression = additiveExpression;
	}

	private final LexRelationalExpression m_oLhs;

	private final Token m_oRelationalOperatorToken;

	private final LexAdditiveExpression m_additiveExpression;
}
