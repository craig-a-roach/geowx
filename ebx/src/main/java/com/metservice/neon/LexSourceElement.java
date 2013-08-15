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
abstract class LexSourceElement extends CompileableLex {

	abstract void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException;

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		cc.setLineIndex(m_sourceElementLineIndex);
		compileSourceElement(cc);
	}

	public static LexSourceElement createSourceElement(TokenReader tr)
			throws EsSyntaxException {
		final LexFunctionDefinition oFunctionDefinition = LexFunctionDefinition.createInstance(tr);
		if (oFunctionDefinition != null) return oFunctionDefinition;

		final LexStatement oStatement = LexStatement.createStatement(tr);
		if (oStatement != null) return oStatement;

		return null;
	}

	public static LexSourceElement newSourceElement(TokenReader tr)
			throws EsSyntaxException {
		final LexSourceElement o = createSourceElement(tr);
		if (o == null) throw new EsSyntaxException("Expecting a SourceElement", tr);
		return o;
	}
	protected LexSourceElement(int lineIndex) {
		m_sourceElementLineIndex = lineIndex;
	}

	private final int m_sourceElementLineIndex;
}
