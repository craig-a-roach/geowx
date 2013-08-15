/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author roach
 */
class LexFormalParametersAndBody extends Lex {

	public SourceCallable newCallable(String oqName, boolean isDeclared, EsSource source)
			throws EsSyntaxException {
		final SourceCallable callable = new SourceCallable(oqName, isDeclared, source);
		final LexFormalParameter[] zptFormalParameters = m_formalParameters.zptFormalParameters();
		for (int i = 0; i < zptFormalParameters.length; i++) {
			callable.declareFormalParameter(zptFormalParameters[i].qccIdentifier());
		}

		final CompilationContext cc = new CompilationContext(source, callable);
		for (int i = 0; i < m_zptSourceElements.length; i++) {
			m_zptSourceElements[i].compile(cc);
		}
		return callable;
	}

	@Override
	public String toScript() {
		final StringBuilder b = new StringBuilder();
		b.append('(');
		b.append(m_formalParameters.toScript());
		b.append("){\n");
		for (int i = 0; i < m_zptSourceElements.length; i++) {
			b.append(m_zptSourceElements[i].toScript());
		}
		b.append("}\n");
		return b.toString();
	}

	public static LexFormalParametersAndBody createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isPunctuator(Punctuator.LPAREN)) {
			tr.consume();
			final LexFormalParameters formalParameters = LexFormalParameters.newInstance(tr);
			tr.consumePunctuator(Punctuator.RPAREN);
			tr.consumePunctuator(Punctuator.LBRACE);
			final List<LexSourceElement> zlSourceElements = new ArrayList<LexSourceElement>();
			while (!tr.current().isPunctuator(Punctuator.RBRACE)) {
				zlSourceElements.add(LexSourceElement.newSourceElement(tr));
			}
			tr.consumePunctuator(Punctuator.RBRACE);
			return new LexFormalParametersAndBody(formalParameters, zlSourceElements);
		}
		return null;
	}

	public static LexFormalParametersAndBody newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexFormalParametersAndBody o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting FormalParameters and FunctionBody", tr);
		return o;
	}

	private LexFormalParametersAndBody(LexFormalParameters formalParameters, List<LexSourceElement> zlSourceElements) {
		assert formalParameters != null;
		assert zlSourceElements != null;
		m_formalParameters = formalParameters;
		m_zptSourceElements = zlSourceElements.toArray(new LexSourceElement[zlSourceElements.size()]);
	}
	private final LexFormalParameters m_formalParameters;
	private final LexSourceElement[] m_zptSourceElements;
}
