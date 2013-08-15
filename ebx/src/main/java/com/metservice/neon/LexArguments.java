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
class LexArguments extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		cc.add(new VmNewList());
		if (m_oArgumentList != null) {
			m_oArgumentList.compile(cc);
		}
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		b.append("(");
		if (m_oArgumentList != null) {
			b.append(m_oArgumentList.toScript());
		}
		b.append(")");
		return b.toString();
	}

	public static LexArguments createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isPunctuator(Punctuator.LPAREN)) {
			tr.consume();
			LexArgumentList oArgumentList = null;
			if (!tr.current().isPunctuator(Punctuator.RPAREN)) {
				oArgumentList = LexArgumentList.newInstance(tr);
			}
			tr.consumePunctuator(Punctuator.RPAREN);
			return new LexArguments(oArgumentList);
		}

		return null;
	}

	public static LexArguments newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexArguments o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting Arguments", tr);
		return o;
	}

	private LexArguments(LexArgumentList oArgumentList) {
		m_oArgumentList = oArgumentList;
	}

	private final LexArgumentList m_oArgumentList;
}
