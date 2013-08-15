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
class LexWhileStatement extends LexStatement {
	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		cc.add(new VmPushLabelScope());
		final VmAddLabel vmContinueLabel = VmAddLabel.newContinue();
		final VmAddLabel vmBreakLabel = VmAddLabel.newBreak();
		cc.add(vmContinueLabel);
		cc.add(vmBreakLabel);
		final InstructionAddress loopEntryAddress = cc.nextAddress();
		final VmJumpConditional vmControl = new VmJumpConditional(false);
		m_condition.compile(cc);
		cc.add(vmControl);
		m_statement.compile(cc);
		final InstructionAddress continueAddress = cc.nextAddress();
		final VmJumpAlways vmLoop = new VmJumpAlways();
		vmLoop.setJumpAddress(loopEntryAddress);
		cc.add(vmLoop);
		final InstructionAddress loopExitAddress = cc.nextAddress();
		vmContinueLabel.setJumpAddress(continueAddress);
		vmBreakLabel.setJumpAddress(loopExitAddress);
		vmControl.setJumpAddress(loopExitAddress);
		cc.add(VmRemoveLabel.newBreak());
		cc.add(VmRemoveLabel.newContinue());
		cc.add(new VmPopLabelScope());
		cc.add(VmSetCompletion.newNormal());
	}

	@Override
	public String toScript() {
		return "while" + m_condition.toScript() + "\n" + m_statement.toScript();
	}
	public static LexWhileStatement createWhile(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.WHILE)) {
			final int lineIndex = tr.current().lineIndex;
			tr.consume();
			final LexParenthesizedExpression condition = LexParenthesizedExpression.newInstance(tr);
			final LexStatement statement = LexStatement.newStatement(tr);
			return new LexWhileStatement(lineIndex, condition, statement);
		}
		return null;
	}

	private LexWhileStatement(int lineIndex, LexParenthesizedExpression condition, LexStatement statement) {
		super(lineIndex);
		assert condition != null;
		assert statement != null;
		m_condition = condition;
		m_statement = statement;
	}

	private final LexParenthesizedExpression m_condition;
	private final LexStatement m_statement;
}
