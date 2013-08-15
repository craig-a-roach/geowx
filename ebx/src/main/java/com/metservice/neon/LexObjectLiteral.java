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
class LexObjectLiteral extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		cc.add(new VmNewObject());
		if (m_oFieldList != null) {
			m_oFieldList.compile(cc);
		}
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		b.append('{');
		if (m_oFieldList != null) {
			b.append(m_oFieldList.toScript());
		}
		b.append('}');
		return b.toString();
	}

	public static LexObjectLiteral createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isPunctuator(Punctuator.LBRACE)) {
			tr.consume();
			final LexFieldList oFieldList = LexFieldList.createInstance(tr);
			tr.consumePunctuator(Punctuator.RBRACE);
			return new LexObjectLiteral(oFieldList);
		}
		return null;
	}
	public static LexObjectLiteral newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexObjectLiteral o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting an ObjectLiteral", tr);
		return o;
	}

	private LexObjectLiteral(LexFieldList oFieldList) {
		m_oFieldList = oFieldList;
	}

	private final LexFieldList m_oFieldList;
}
