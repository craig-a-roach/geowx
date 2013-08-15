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
class LexIfStatement extends LexStatement {

	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		m_condition.compile(cc);
		final VmJumpConditional vmConditional = new VmJumpConditional(false);
		cc.add(vmConditional);
		final InstructionAddress falseAddress;
		final VmJumpAlways oVmSkip;
		final InstructionAddress oContinueAddress;
		m_trueStatement.compile(cc);
		if (m_oFalseStatement == null) {
			oVmSkip = null;
			oContinueAddress = null;
			falseAddress = cc.nextAddress();
		} else {
			oVmSkip = new VmJumpAlways();
			cc.add(oVmSkip);
			falseAddress = cc.nextAddress();
			m_oFalseStatement.compile(cc);
			oContinueAddress = cc.nextAddress();
		}
		vmConditional.setJumpAddress(falseAddress);
		if (oVmSkip != null) {
			oVmSkip.setJumpAddress(oContinueAddress);
		}
		cc.add(VmSetCompletion.newNormal());
	}

	@Override
	public String toScript() {
		final String zElse = (m_oFalseStatement == null ? "" : "else\n" + m_oFalseStatement.toScript());
		return "if" + m_condition.toScript() + "\n" + m_trueStatement.toScript() + zElse;
	}

	public static LexIfStatement createIf(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.IF)) {
			final int lineIndex = tr.current().lineIndex;
			tr.consume();
			final LexParenthesizedExpression condition = LexParenthesizedExpression.newInstance(tr);
			final LexStatement trueStatement = LexStatement.newStatement(tr);
			LexStatement oFalseStatement = null;
			if (tr.current().isKeyword(Keyword.ELSE)) {
				tr.consume();
				oFalseStatement = LexStatement.newStatement(tr);
			}
			return new LexIfStatement(lineIndex, condition, trueStatement, oFalseStatement);
		}
		return null;
	}

	private LexIfStatement(int lineIndex, LexParenthesizedExpression condition, LexStatement trueStatement,
			LexStatement oFalseStatement) {
		super(lineIndex);
		assert condition != null;
		assert trueStatement != null;
		m_condition = condition;
		m_trueStatement = trueStatement;
		m_oFalseStatement = oFalseStatement;
	}

	private final LexParenthesizedExpression m_condition;
	private final LexStatement m_trueStatement;
	private final LexStatement m_oFalseStatement;
}
