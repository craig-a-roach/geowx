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
class LexLiteralField extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		m_assignmentExpression.compile(cc);
		cc.add(new VmPutField(m_qccIdentifier));
	}

	@Override
	public String toScript() {
		return m_qccIdentifier + ":" + m_assignmentExpression.toScript();
	}

	public static LexLiteralField createInstance(TokenReader tr)
			throws EsSyntaxException {
		final Token token = tr.current();
		if (token.isIdentifier()) {
			tr.consume();
			tr.consumePunctuator(Punctuator.COLON);
			final String qccIdentifier = ((TokenIdentifier) token).qccIdentifier;
			return new LexLiteralField(qccIdentifier, LexAssignmentExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn));
		}

		if (token.isStringLiteral()) {
			tr.consume();
			tr.consumePunctuator(Punctuator.COLON);
			final String zccIdentifier = ((TokenLiteralString) token).zccValue;
			if (zccIdentifier.length() == 0) throw new EsSyntaxException("Empty field identifier", tr);
			return new LexLiteralField(zccIdentifier, LexAssignmentExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn));
		}
		return null;
	}
	public static LexLiteralField newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexLiteralField o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a LiteralField", tr);
		return o;
	}

	private LexLiteralField(String qccIdentifier, LexAssignmentExpression assignmentExpression) {
		assert qccIdentifier != null && qccIdentifier.length() > 0;
		assert assignmentExpression != null;
		m_qccIdentifier = qccIdentifier;
		m_assignmentExpression = assignmentExpression;
	}

	private final String m_qccIdentifier;

	private final LexAssignmentExpression m_assignmentExpression;
}
