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
class LexLiteralElement extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		m_assignmentExpression.compile(cc);
	}

	@Override
	public String toScript() {
		return m_assignmentExpression.toScript();
	}

	public static LexLiteralElement createInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexAssignmentExpression oAssignmentExpression = LexAssignmentExpression.createInstance(tr, Alpha.Normal,
				Beta.AllowIn);
		if (oAssignmentExpression != null) return new LexLiteralElement(oAssignmentExpression);
		return null;
	}

	public static LexLiteralElement newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexLiteralElement o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a LiteralElement", tr);
		return o;
	}

	private LexLiteralElement(LexAssignmentExpression assignmentExpression) {
		assert assignmentExpression != null;
		m_assignmentExpression = assignmentExpression;
	}

	private final LexAssignmentExpression m_assignmentExpression;
}
