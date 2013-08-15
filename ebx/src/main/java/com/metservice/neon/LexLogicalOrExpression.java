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
class LexLogicalOrExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs == null || m_oOrToken == null) {
			m_logicalAndExpression.compile(cc);
		} else {
			m_oLhs.compile(cc);
			final VmLogicalOr vmOr = new VmLogicalOr();
			cc.add(vmOr);
			m_logicalAndExpression.compile(cc);
			vmOr.setJumpAddress(cc.nextAddress());
		}
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		if (m_oLhs != null) {
			b.append(m_oLhs.toScript());
			b.append("||");
		}
		b.append(m_logicalAndExpression.toScript());
		return b.toString();
	}

	public static LexLogicalOrExpression createInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexLogicalAndExpression oRhs = LexLogicalAndExpression.createInstance(tr, alpha, beta, oLeftSideExpression);
		if (oRhs == null) return null;

		LexLogicalOrExpression lhs = new LexLogicalOrExpression(null, null, oRhs);
		while (tr.current().isPunctuator(Punctuator.LOGICAL_OR)) {
			final Token token = tr.current();
			tr.consume();
			lhs = new LexLogicalOrExpression(lhs, token, LexLogicalAndExpression.newInstance(tr, Alpha.Normal, beta, null));
		}
		return lhs;
	}
	public static LexLogicalOrExpression newInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexLogicalOrExpression o = createInstance(tr, alpha, beta, oLeftSideExpression);
		if (o == null) throw new EsSyntaxException("Expecting a LogicalOrExpression", tr);
		return o;
	}
	private LexLogicalOrExpression(LexLogicalOrExpression oLhs, Token oToken, LexLogicalAndExpression logicalAndExpression) {
		m_oLhs = oLhs;
		m_oOrToken = oToken;
		m_logicalAndExpression = logicalAndExpression;
	}

	private final LexLogicalOrExpression m_oLhs;
	private final Token m_oOrToken;
	private final LexLogicalAndExpression m_logicalAndExpression;
}
