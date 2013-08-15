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
class LexElementList extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_oLhs != null) {
			m_oLhs.compile(cc);
		}
		m_literalElement.compile(cc);
		cc.add(new VmAddToList());
	}

	@Override
	public String toScript() {
		final StringBuilder b = new StringBuilder();
		if (m_oLhs != null) {
			b.append(m_oLhs.toScript());
			b.append(',');
		}
		b.append(m_literalElement.toScript());
		return b.toString();
	}

	public static LexElementList createInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexLiteralElement oRhs = LexLiteralElement.createInstance(tr);
		if (oRhs == null) return null;

		LexElementList lhs = new LexElementList(null, oRhs);
		while (tr.current().isPunctuator(Punctuator.COMMA)) {
			tr.consume();
			lhs = new LexElementList(lhs, LexLiteralElement.newInstance(tr));
		}

		return lhs;
	}
	public static LexElementList newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexElementList o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting an ElementList", tr);
		return o;
	}
	private LexElementList(LexElementList oLhs, LexLiteralElement literalElement) {
		assert literalElement != null;
		m_oLhs = oLhs;
		m_literalElement = literalElement;
	}

	private final LexElementList m_oLhs;

	private final LexLiteralElement m_literalElement;
}
