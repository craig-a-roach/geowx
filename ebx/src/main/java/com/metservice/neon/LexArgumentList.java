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
class LexArgumentList extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs != null) {
			m_oLhs.compile(cc);
		}
		m_assignmentExpression.compile(cc);
		cc.add(new VmAddToList());
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		if (m_oLhs != null) {
			b.append(m_oLhs.toScript());
			b.append(',');
		}
		b.append(m_assignmentExpression.toScript());
		return b.toString();
	}

	public static LexArgumentList createInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexAssignmentExpression oRhs = LexAssignmentExpression.createInstance(tr, Alpha.Normal, Beta.AllowIn);
		if (oRhs == null) return null;

		LexArgumentList lhs = new LexArgumentList(null, oRhs);
		while (tr.current().isPunctuator(Punctuator.COMMA)) {
			tr.consume();
			lhs = new LexArgumentList(lhs, LexAssignmentExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn));
		}
		return lhs;
	}
	public static LexArgumentList newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexArgumentList o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting an ArgumentList", tr);
		return o;
	}

	private LexArgumentList(LexArgumentList oLhs, LexAssignmentExpression assignmentExpression) {
		assert assignmentExpression != null;
		m_oLhs = oLhs;
		m_assignmentExpression = assignmentExpression;
	}

	private final LexArgumentList m_oLhs;

	private final LexAssignmentExpression m_assignmentExpression;
}
