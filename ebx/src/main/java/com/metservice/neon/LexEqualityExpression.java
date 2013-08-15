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
class LexEqualityExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs == null || m_oEqualityOperatorToken == null) {
			m_relationalExpression.compile(cc);
		} else {
			m_oLhs.compile(cc);
			m_relationalExpression.compile(cc);
			final Punctuator punctuator = ((TokenPunctuator) m_oEqualityOperatorToken).punctuator;
			switch (punctuator) {
				case EQ_RELATION: {
					cc.add(VmEquality.newPositive());
				}
				break;
				case NEQ_RELATION: {
					cc.add(VmEquality.newNegative());
				}
				break;
				case EQS_RELATION: {
					cc.add(VmEquality.newPositiveStrict());
				}
				break;
				case NEQS_RELATION: {
					cc.add(VmEquality.newNegativeStrict());
				}
				break;
				default: {
					throw new EsCompilerException("Unsupported Equality Operator", cc.here(m_oEqualityOperatorToken));
				}
			}
		}
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		if (m_oLhs != null && m_oEqualityOperatorToken != null) {
			b.append(m_oLhs.toScript());
			b.append(m_oEqualityOperatorToken.toScript());
		}
		b.append(m_relationalExpression.toScript());
		return b.toString();
	}

	public static LexEqualityExpression createInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexRelationalExpression oRhs = LexRelationalExpression.createInstance(tr, alpha, beta, oLeftSideExpression);
		if (oRhs == null) return null;

		LexEqualityExpression lhs = new LexEqualityExpression(null, null, oRhs);
		while (tr.current().isEqualityOperator()) {
			final Token operatorToken = tr.current();
			tr.consume();
			lhs = new LexEqualityExpression(lhs, operatorToken, LexRelationalExpression.newInstance(tr, Alpha.Normal, beta,
					null));
		}
		return lhs;
	}
	public static LexEqualityExpression newInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexEqualityExpression o = createInstance(tr, alpha, beta, oLeftSideExpression);
		if (o == null) throw new EsSyntaxException("Expecting an EqualityExpression", tr);
		return o;
	}
	private LexEqualityExpression(LexEqualityExpression oLhs, Token oEqualityOperatorToken,
			LexRelationalExpression relationalExpression) {
		m_oLhs = oLhs;
		m_oEqualityOperatorToken = oEqualityOperatorToken;
		m_relationalExpression = relationalExpression;
	}

	private final LexEqualityExpression m_oLhs;

	private final Token m_oEqualityOperatorToken;

	private final LexRelationalExpression m_relationalExpression;
}
