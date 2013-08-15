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
class LexFieldList extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs != null) {
			m_oLhs.compile(cc);
		}
		m_literalField.compile(cc);
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		if (m_oLhs != null) {
			b.append(m_oLhs.toScript());
			b.append(',');
		}
		b.append(m_literalField.toScript());
		return b.toString();
	}

	public static LexFieldList createInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexLiteralField oRhs = LexLiteralField.createInstance(tr);
		if (oRhs == null) return null;

		LexFieldList lhs = new LexFieldList(null, oRhs);
		while (tr.current().isPunctuator(Punctuator.COMMA)) {
			tr.consume();
			lhs = new LexFieldList(lhs, LexLiteralField.newInstance(tr));
		}

		return lhs;
	}
	public static LexFieldList newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexFieldList o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a FieldList", tr);
		return o;
	}
	private LexFieldList(LexFieldList oLhs, LexLiteralField literalField) {
		assert literalField != null;
		m_oLhs = oLhs;
		m_literalField = literalField;
	}

	private final LexFieldList m_oLhs;
	private final LexLiteralField m_literalField;
}
