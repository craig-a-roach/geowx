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
class LexFunctionDefinition extends LexSourceElement {

	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		m_namedFunction.compile(cc);
	}

	@Override
	public String toScript() {
		return m_namedFunction.toScript();
	}

	public static LexFunctionDefinition createInstance(TokenReader tr)
			throws EsSyntaxException {
		final int lineIndex = tr.current().lineIndex;
		final LexNamedFunction oNamedFunction = LexNamedFunction.createInstance(tr, true);
		if (oNamedFunction != null) return new LexFunctionDefinition(lineIndex, oNamedFunction);
		return null;
	}

	public static LexFunctionDefinition newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexFunctionDefinition o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a FunctionDefinition", tr);
		return o;
	}

	public LexFunctionDefinition(int lineIndex, LexNamedFunction namedFunction) {
		super(lineIndex);
		assert namedFunction != null;
		m_namedFunction = namedFunction;
	}

	private final LexNamedFunction m_namedFunction;
}
