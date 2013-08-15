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
class LexArrayLiteral extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		cc.add(new VmNewList());
		if (m_oElementList != null) {
			m_oElementList.compile(cc);
		}
		cc.add(new VmPushArray());
	}

	@Override
	public String toScript() {
		final StringBuilder b = new StringBuilder();
		b.append('[');
		if (m_oElementList != null) {
			b.append(m_oElementList.toScript());
		}
		b.append(']');
		return b.toString();
	}

	public static LexArrayLiteral createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isPunctuator(Punctuator.LSQUARE)) {
			tr.consume();
			final LexElementList oElementList = LexElementList.createInstance(tr);
			tr.consumePunctuator(Punctuator.RSQUARE);
			return new LexArrayLiteral(oElementList);
		}
		return null;
	}
	public static LexArrayLiteral newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexArrayLiteral o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting an ArrayLiteral", tr);
		return o;
	}

	private LexArrayLiteral(LexElementList oElementList) {
		m_oElementList = oElementList;
	}

	private final LexElementList m_oElementList;
}
