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
class LexLogicalAndExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs == null || m_oAndToken == null) {
			m_equalityExpression.compile(cc);
		} else {
			m_oLhs.compile(cc);
			final VmLogicalAnd vmAnd = new VmLogicalAnd();
			cc.add(vmAnd);
			m_equalityExpression.compile(cc);
			vmAnd.setJumpAddress(cc.nextAddress());
		}
	}

	@Override
	public String toScript() {
		final StringBuilder b = new StringBuilder();
		if (m_oLhs != null) {
			b.append(m_oLhs.toScript());
			b.append("&&");
		}
		b.append(m_equalityExpression.toScript());
		return b.toString();
	}

	public static LexLogicalAndExpression createInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexEqualityExpression oRhs = LexEqualityExpression.createInstance(tr, alpha, beta, oLeftSideExpression);
		if (oRhs == null) return null;

		LexLogicalAndExpression lhs = new LexLogicalAndExpression(null, null, oRhs);
		while (tr.current().isPunctuator(Punctuator.LOGICAL_AND)) {
			final Token token = tr.current();
			tr.consume();
			lhs = new LexLogicalAndExpression(lhs, token, LexEqualityExpression.newInstance(tr, Alpha.Normal, beta, null));
		}
		return lhs;
	}
	public static LexLogicalAndExpression newInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexLogicalAndExpression o = createInstance(tr, alpha, beta, oLeftSideExpression);
		if (o == null) throw new EsSyntaxException("Expecting a LogicalOrExpression", tr);
		return o;
	}
	private LexLogicalAndExpression(LexLogicalAndExpression oLhs, Token oToken, LexEqualityExpression equalityExpression) {
		m_oLhs = oLhs;
		m_oAndToken = oToken;
		m_equalityExpression = equalityExpression;
	}

	private final LexLogicalAndExpression m_oLhs;
	private final Token m_oAndToken;
	private final LexEqualityExpression m_equalityExpression;
}
