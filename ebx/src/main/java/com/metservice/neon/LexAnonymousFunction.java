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
class LexAnonymousFunction extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		final SourceCallable callable = m_formalParametersAndBody.newCallable(null, false, cc.source());
		cc.add(new VmPushCallable(callable));
	}

	@Override
	public String toScript() {
		return "function" + m_formalParametersAndBody.toScript();
	}

	public static LexAnonymousFunction createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.FUNCTION)) {
			tr.consume();
			return new LexAnonymousFunction(LexFormalParametersAndBody.newInstance(tr));
		}
		return null;
	}

	public static LexAnonymousFunction newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexAnonymousFunction o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting an AnonymousFunction", tr);
		return o;
	}

	private LexAnonymousFunction(LexFormalParametersAndBody formalParametersAndBody) {
		assert formalParametersAndBody != null;
		m_formalParametersAndBody = formalParametersAndBody;
	}

	private final LexFormalParametersAndBody m_formalParametersAndBody;
}
