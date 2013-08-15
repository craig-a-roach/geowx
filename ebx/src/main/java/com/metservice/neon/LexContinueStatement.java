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
class LexContinueStatement extends LexStatement {

	@Override
	void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		cc.add(VmSetCompletion.newContinue());
	}

	@Override
	public String toScript() {
		return "continue;\n";
	}

	public static LexContinueStatement createContinue(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.CONTINUE)) {
			final int lineIndex = tr.current().lineIndex;
			tr.consume();
			tr.consumePunctuator(Punctuator.SEMICOLON);
			return new LexContinueStatement(lineIndex);
		}
		return null;
	}

	private LexContinueStatement(int lineIndex) {
		super(lineIndex);
	}
}
