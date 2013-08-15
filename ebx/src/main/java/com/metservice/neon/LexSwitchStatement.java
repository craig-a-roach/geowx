/*
 * Copyright 2008 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * 
 * @author roach
 */
class LexSwitchStatement extends LexStatement {
	private void compileCaseStatements(CompilationContext cc, LexCaseClause caseClause, boolean isDefault)
			throws EsSyntaxException {
		assert caseClause != null;
		cc.setLineIndex(caseClause.clauseLineIndex());
		final LexStatement[] zptTargetStatements = caseClause.zptStatements();
		final int ilast = zptTargetStatements.length - 1;
		for (int i = 0; i <= ilast; i++) {
			zptTargetStatements[i].compile(cc);
		}
		if (m_isChoose && !isDefault) {
			final LexStatement oLast = ilast >= 0 ? zptTargetStatements[ilast] : null;
			final boolean isLastBreak = (oLast instanceof LexBreakStatement);
			if (!isLastBreak) {
				cc.add(VmSetCompletion.newBreak());
			}
		}
	}

	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		cc.add(new VmPushLabelScope());
		final VmAddLabel vmBreakLabel = VmAddLabel.newBreak();
		cc.add(vmBreakLabel);
		m_selector.compile(cc);
		cc.add(new VmSwapForValue());
		final LexCaseClause[] zptSpecificCaseClauses = m_caseBlock.zptSpecificCaseClauses();
		final LexCaseClause oDefaultCaseClause = m_caseBlock.getDefaultCaseClause();
		final int specificCount = zptSpecificCaseClauses.length;
		final VmCaseJump[] zptSpecificJumps = new VmCaseJump[specificCount];
		for (int i = 0; i < specificCount; i++) {
			final LexCaseClause specificCaseClause = zptSpecificCaseClauses[i];
			final LexExpression oExpression = specificCaseClause.getExpression();
			if (oExpression != null) {
				oExpression.compile(cc);
				final VmCaseJump caseJump = new VmCaseJump();
				zptSpecificJumps[i] = caseJump;
				cc.add(caseJump);
			}
		}
		final VmJumpAlways nomatchJump = new VmJumpAlways();
		cc.add(nomatchJump);

		for (int i = 0; i < specificCount; i++) {
			final LexCaseClause specificCaseClause = zptSpecificCaseClauses[i];
			final VmCaseJump oCaseJump = zptSpecificJumps[i];
			if (oCaseJump != null) {
				final InstructionAddress caseTarget = cc.nextAddress();
				oCaseJump.setJumpAddress(caseTarget);
				compileCaseStatements(cc, specificCaseClause, false);
			}
		}

		if (oDefaultCaseClause != null) {
			final InstructionAddress defaultTarget = cc.nextAddress();
			nomatchJump.setJumpAddress(defaultTarget);
			compileCaseStatements(cc, oDefaultCaseClause, true);
		}

		final InstructionAddress switchExitAddress = cc.nextAddress();
		if (oDefaultCaseClause == null) {
			nomatchJump.setJumpAddress(switchExitAddress);
		}

		vmBreakLabel.setJumpAddress(switchExitAddress);

		cc.add(VmRemoveLabel.newBreak());
		cc.add(new VmPopLabelScope());
		cc.add(VmSetCompletion.newNormal());
	}

	@Override
	public String toScript() {
		final String kwd = m_isChoose ? "choose" : "switch";
		return kwd + m_selector.toScript() + "\n" + m_caseBlock.toScript();
	}

	public static LexSwitchStatement createSwitch(TokenReader tr)
			throws EsSyntaxException {
		final Token current = tr.current();
		final boolean isChoose = current.isKeyword(Keyword.CHOOSE);
		if (isChoose || current.isKeyword(Keyword.SWITCH)) {
			final int lineIndex = tr.current().lineIndex;
			tr.consume();
			final LexParenthesizedExpression selector = LexParenthesizedExpression.newInstance(tr);
			final LexCaseBlock caseBlock = LexCaseBlock.newInstance(tr);
			return new LexSwitchStatement(lineIndex, selector, caseBlock, isChoose);
		}
		return null;
	}

	private LexSwitchStatement(int lineIndex, LexParenthesizedExpression selector, LexCaseBlock caseBlock, boolean isChoose) {
		super(lineIndex);
		m_selector = selector;
		m_caseBlock = caseBlock;
		m_isChoose = isChoose;
	}

	private final LexParenthesizedExpression m_selector;

	private final LexCaseBlock m_caseBlock;

	private final boolean m_isChoose;
}
