/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Ds;

/**
 * 
 * @author roach
 */
public class EsParsedProgram {

	public SourceCallable newCallable()
			throws EsSyntaxException {
		return m_program.newCompilable(m_scriptSource);
	}

	public String toScript() {
		return m_program.toScript();
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("program", m_program);
		return ds.s();
	}

	static EsParsedProgram newInstance(TokenReader tokenReader)
			throws EsSyntaxException {
		if (tokenReader == null) throw new IllegalArgumentException("tokenReader is null");

		final LexProgram program = LexProgram.newInstance(tokenReader);
		return new EsParsedProgram(tokenReader.scriptSource(), program);
	}

	private EsParsedProgram(EsSource scriptSource, LexProgram program) {
		assert scriptSource != null;
		assert program != null;
		m_scriptSource = scriptSource;
		m_program = program;
	}

	private final EsSource m_scriptSource;
	private final LexProgram m_program;
}
