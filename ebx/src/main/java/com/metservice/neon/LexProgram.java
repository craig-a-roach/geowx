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
class LexProgram extends Lex {

	public SourceCallable newCompilable(EsSource source)
			throws EsSyntaxException {
		if (source == null) throw new IllegalArgumentException("source is null");
		final SourceCallable callable = new SourceCallable(null, true, source);
		final CompilationContext cc = new CompilationContext(source, callable);
		for (int i = 0; i < m_zptSourceElements.length; i++) {
			m_zptSourceElements[i].compile(cc);
		}
		return callable;
	}

	@Override
	public String toScript() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < m_zptSourceElements.length; i++) {
			sb.append(m_zptSourceElements[i].toScript());
		}
		return sb.toString();
	}

	public static LexProgram newInstance(TokenReader tr)
			throws EsSyntaxException {
		final List<LexSourceElement> zlSourceElements = new ArrayList<LexSourceElement>();
		while (tr.more()) {
			zlSourceElements.add(LexSourceElement.newSourceElement(tr));
		}
		return new LexProgram(zlSourceElements);
	}

	private LexProgram(List<LexSourceElement> zlSourceElements) {
		m_zptSourceElements = zlSourceElements.toArray(new LexSourceElement[zlSourceElements.size()]);
	}

	private final LexSourceElement[] m_zptSourceElements;
}
