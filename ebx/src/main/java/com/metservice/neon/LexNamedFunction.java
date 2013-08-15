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
class LexNamedFunction extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		final String qccFunctionName = ((TokenIdentifier) m_identifierToken).qccIdentifier;
		final SourceCallable callable = m_formalParametersAndBody.newCallable(qccFunctionName, m_isDeclared, cc.source());
		if (m_isDeclared) {
			if (!cc.add(new VmDeclareFunction(qccFunctionName, callable))) {
				final String m = "Duplicate definition of function '" + qccFunctionName + "'";
				throw new EsSyntaxException(m, cc);
			}
		} else {
			cc.add(new VmPushCallable(callable));
		}
	}

	@Override
	public String toScript() {
		return "function " + m_identifierToken.toScript() + m_formalParametersAndBody.toScript();
	}

	public static LexNamedFunction createInstance(TokenReader tr, boolean declared)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.FUNCTION)) {
			tr.consume();
			if (tr.current().isIdentifier()) {
				final Token identifierToken = tr.current();
				tr.consume();
				return new LexNamedFunction(identifierToken, LexFormalParametersAndBody.newInstance(tr), declared);
			}
			throw new EsSyntaxException("Expecting an Identifier", tr);
		}
		return null;
	}

	public static LexNamedFunction newInstance(TokenReader tr, boolean declared)
			throws EsSyntaxException {
		final LexNamedFunction o = createInstance(tr, declared);
		if (o == null) throw new EsSyntaxException("Expecting a NamedFunction", tr);
		return o;
	}

	private LexNamedFunction(Token identifierToken, LexFormalParametersAndBody formalParametersAndBody, boolean isDeclared) {
		assert identifierToken != null;
		assert formalParametersAndBody != null;
		m_identifierToken = identifierToken;
		m_formalParametersAndBody = formalParametersAndBody;
		m_isDeclared = isDeclared;
	}

	private final Token m_identifierToken;
	private final LexFormalParametersAndBody m_formalParametersAndBody;
	private final boolean m_isDeclared;
}
