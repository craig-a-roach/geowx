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
abstract class LexStatement extends LexSourceElement {
	public static LexStatement createStatement(TokenReader tr)
			throws EsSyntaxException {
		final Token current = tr.current();
		if (current.isClauseMarkerKeyword()) return null;

		LexStatement o = LexVariableDefinitionStatement.createVariableDefinition(tr);
		if (o != null) return o;

		o = LexBlock.createBlock(tr);
		if (o != null) return o;

		o = LexIfStatement.createIf(tr);
		if (o != null) return o;

		o = LexReturnStatement.createReturn(tr);
		if (o != null) return o;

		o = LexForStatement.createFor(tr);
		if (o != null) return o;

		o = LexWhileStatement.createWhile(tr);
		if (o != null) return o;

		o = LexBreakStatement.createBreak(tr);
		if (o != null) return o;

		o = LexContinueStatement.createContinue(tr);
		if (o != null) return o;

		o = LexThrowStatement.createThrow(tr);
		if (o != null) return o;

		o = LexSwitchStatement.createSwitch(tr);
		if (o != null) return o;

		o = LexExpressionStatement.createExpression(tr);
		if (o != null) return o;

		return null;
	}

	public static LexStatement newStatement(TokenReader tr)
			throws EsSyntaxException {
		final LexStatement o = createStatement(tr);
		if (o == null) throw new EsSyntaxException("Expecting a statement", tr);
		return o;
	}

	protected LexStatement(int lineIndex) {
		super(lineIndex);
	}

}
