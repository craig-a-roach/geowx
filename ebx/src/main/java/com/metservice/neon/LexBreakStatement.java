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
class LexBreakStatement extends LexStatement {
	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		cc.add(VmSetCompletion.newBreak());
	}

	@Override
	public String toScript() {
		return "break;\n";
	}

	public static LexBreakStatement createBreak(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.BREAK)) {
			final int lineIndex = tr.current().lineIndex;
			tr.consume();
			tr.consumePunctuator(Punctuator.SEMICOLON);
			return new LexBreakStatement(lineIndex);
		}
		return null;
	}

	private LexBreakStatement(int lineIndex) {
		super(lineIndex);
	}
}
