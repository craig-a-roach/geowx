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
class LexExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs != null) {
			m_oLhs.compile(cc);
		}

		m_assignmentExpression.compile(cc);
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		if (m_oLhs != null) {
			b.append(m_oLhs.toScript());
			b.append(",");
		}
		b.append(m_assignmentExpression.toScript());
		return b.toString();
	}

	public static LexExpression createInstance(TokenReader tr, Alpha alpha, Beta beta)
			throws EsSyntaxException {
		final LexAssignmentExpression oRhs = LexAssignmentExpression.createInstance(tr, alpha, beta);
		if (oRhs == null) return null;

		LexExpression lhs = new LexExpression(null, oRhs);
		while (tr.current().isPunctuator(Punctuator.COMMA)) {
			tr.consume();
			lhs = new LexExpression(lhs, LexAssignmentExpression.newInstance(tr, Alpha.Normal, beta));
		}
		return lhs;
	}
	public static LexExpression newInstance(TokenReader tr, Alpha alpha, Beta beta)
			throws EsSyntaxException {
		final LexExpression o = createInstance(tr, alpha, beta);
		if (o == null) throw new EsSyntaxException("Expecting an Expression", tr);
		return o;
	}

	private LexExpression(LexExpression oLhs, LexAssignmentExpression assignmentExpression) {
		m_oLhs = oLhs;
		m_assignmentExpression = assignmentExpression;
	}

	private final LexExpression m_oLhs;

	private final LexAssignmentExpression m_assignmentExpression;
}
