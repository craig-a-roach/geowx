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
class LexFunctionExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_orAnonymousFunction != null) {
			m_orAnonymousFunction.compile(cc);
			return;
		}

		if (m_orNamedFunction != null) {
			m_orNamedFunction.compile(cc);
			return;
		}
	}

	@Override
	public String toScript() {
		if (m_orAnonymousFunction != null) return m_orAnonymousFunction.toScript();
		if (m_orNamedFunction != null) return m_orNamedFunction.toScript();
		return "";
	}

	public static LexFunctionExpression createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.FUNCTION)) {
			if (tr.peek().isIdentifier()) return new LexFunctionExpression(LexNamedFunction.newInstance(tr, false));
			return new LexFunctionExpression(LexAnonymousFunction.newInstance(tr));
		}
		return null;
	}

	public static LexFunctionExpression newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexFunctionExpression o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a NewExpression", tr);
		return o;
	}

	private LexFunctionExpression(LexAnonymousFunction anonymousFunction) {
		assert anonymousFunction != null;
		m_orAnonymousFunction = anonymousFunction;
		m_orNamedFunction = null;
	}

	private LexFunctionExpression(LexNamedFunction namedFunction) {
		assert namedFunction != null;
		m_orAnonymousFunction = null;
		m_orNamedFunction = namedFunction;
	}

	private final LexAnonymousFunction m_orAnonymousFunction;

	private final LexNamedFunction m_orNamedFunction;
}
