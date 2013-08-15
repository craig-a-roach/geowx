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
class LexPostfixExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		m_leftSideExpression.compile(cc);
		if (m_oPostfixOperatorToken != null) {
			cc.add(new VmSwapForValueRefValue());
			cc.add(new VmPushLiteral(EsPrimitiveNumberInteger.ONE));
			final Punctuator postfixOperator = ((TokenPunctuator) m_oPostfixOperatorToken).punctuator;
			switch (postfixOperator) {
				case PLUSPLUS: {
					cc.add(VmAdditive.newPlus());
				}
				break;
				case MINUSMINUS: {
					cc.add(VmAdditive.newMinus());
				}
				break;
				default: {
					throw new EsCompilerException("Unsupported postfix operator", cc.here(m_oPostfixOperatorToken));
				}
			}
			cc.add(VmPutValue.newDiscardOperands());
		}
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		b.append(m_leftSideExpression.toScript());
		if (m_oPostfixOperatorToken != null) {
			b.append(m_oPostfixOperatorToken.toScript());
		}
		return b.toString();
	}

	public static LexPostfixExpression createInstance(TokenReader tr, Alpha alpha, LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		LexLeftSideExpression oLeft = oLeftSideExpression;
		if (oLeft == null) {
			oLeft = LexLeftSideExpression.createInstance(tr, alpha);
		}

		if (oLeft == null) return null;

		Token oPostfixOperatorToken = null;
		if (tr.current().isPostfixOperator()) {
			oPostfixOperatorToken = tr.current();
			tr.consume();
		}

		return new LexPostfixExpression(oLeft, oPostfixOperatorToken);
	}
	public static LexPostfixExpression newInstance(TokenReader tr, Alpha alpha, LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexPostfixExpression o = createInstance(tr, alpha, oLeftSideExpression);
		if (o == null) throw new EsSyntaxException("Expecting an PostfixExpression", tr);
		return o;
	}
	private LexPostfixExpression(LexLeftSideExpression leftSideExpression, Token oPostfixOperatorToken) {
		m_leftSideExpression = leftSideExpression;
		m_oPostfixOperatorToken = oPostfixOperatorToken;
	}

	private final LexLeftSideExpression m_leftSideExpression;
	private final Token m_oPostfixOperatorToken;
}
