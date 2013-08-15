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
class LexForStatement extends LexStatement {

	@Override
	public void compileSourceElement(CompilationContext cc)
			throws EsSyntaxException {
		m_form.compile(cc);
	}

	@Override
	public String toScript() {
		return m_form.toScript();
	}

	public static LexForStatement createFor(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.FOR)) {
			final int lineIndex = tr.current().lineIndex;
			tr.consume();
			tr.consumePunctuator(Punctuator.LPAREN);

			tr.mark();
			final LexForInitializer oForInitializer = tr.current().isPunctuator(Punctuator.SEMICOLON) ? null
					: LexForInitializer.newInstance(tr);

			if (tr.current().isPunctuator(Punctuator.SEMICOLON)) {
				tr.consumePunctuator(Punctuator.SEMICOLON);
				LexExpression oContinueExpression = null;
				if (!tr.current().isPunctuator(Punctuator.SEMICOLON)) {
					oContinueExpression = LexExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn);
				}
				tr.consumePunctuator(Punctuator.SEMICOLON);

				LexExpression oEndExpression = null;
				if (!tr.current().isPunctuator(Punctuator.RPAREN)) {
					oEndExpression = LexExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn);
				}
				tr.consumePunctuator(Punctuator.RPAREN);

				final LexStatement statement = LexStatement.newStatement(tr);
				return new LexForStatement(lineIndex, oForInitializer, oContinueExpression, oEndExpression, statement);
			}
			tr.reset();
			final LexForInBinding inBinding = LexForInBinding.newInstance(tr);
			if (!tr.current().isKeyword(Keyword.IN))
				throw new EsSyntaxException("Expecting 'in' keyword in 'for-in' loop", tr);
			tr.consume();
			final LexExpression iteratorExpression = LexExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn);
			tr.consumePunctuator(Punctuator.RPAREN);

			final LexStatement statement = LexStatement.newStatement(tr);
			return new LexForStatement(lineIndex, inBinding, iteratorExpression, statement);
		}
		return null;
	}

	private LexForStatement(int lineIndex, LexForInBinding inBinding, LexExpression iteratorExpression, LexStatement statement) {
		super(lineIndex);
		m_form = new FormI(inBinding, iteratorExpression, statement);
	}

	private LexForStatement(int lineIndex, LexForInitializer oInitializer, LexExpression oContinueExpression,
			LexExpression oEndExpression, LexStatement statement) {
		super(lineIndex);
		m_form = new FormT(oInitializer, oContinueExpression, oEndExpression, statement);
	}

	private final Form m_form;

	private static abstract class Form {
		abstract void compile(CompilationContext cc)
				throws EsSyntaxException;

		abstract String toScript();

		protected Form(LexStatement statement) {
			assert statement != null;
			this.statement = statement;
		}
		final LexStatement statement;
	}

	private static class FormI extends Form {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			cc.add(new VmPushLabelScope());
			final VmAddLabel vmContinueLabel = VmAddLabel.newContinue();
			final VmAddLabel vmBreakLabel = VmAddLabel.newBreak();
			cc.add(vmContinueLabel);
			cc.add(vmBreakLabel);

			iteratorExpression.compile(cc);
			cc.add(new VmEvaluatePropertyNameList());
			final InstructionAddress loopEntryAddress = cc.nextAddress();
			final VmJumpListNext vmControl = new VmJumpListNext();
			cc.add(vmControl);
			inBinding.compile(cc);
			cc.add(new VmTranspose());
			cc.add(VmPutValue.newDiscardOperands());
			statement.compile(cc);

			final VmJumpAlways vmLoop = new VmJumpAlways();
			vmLoop.setJumpAddress(loopEntryAddress);
			cc.add(vmLoop);
			final InstructionAddress loopExitAddress = cc.nextAddress();
			vmContinueLabel.setJumpAddress(loopEntryAddress);
			vmBreakLabel.setJumpAddress(loopExitAddress);
			vmControl.setJumpAddress(loopExitAddress);
			cc.add(VmRemoveLabel.newBreak());
			cc.add(VmRemoveLabel.newContinue());
			cc.add(new VmPopLabelScope());
			cc.add(VmSetCompletion.newNormal());
		}

		@Override
		public String toScript() {
			final StringBuilder b = new StringBuilder();
			b.append("for(");
			b.append(inBinding.toScript());
			b.append(" in ");
			b.append(iteratorExpression.toScript());
			b.append(")\n");
			b.append(statement.toScript());
			return b.toString();
		}

		public FormI(LexForInBinding inBinding, LexExpression iteratorExpression, LexStatement statement) {
			super(statement);
			assert inBinding != null;
			assert iteratorExpression != null;
			this.inBinding = inBinding;
			this.iteratorExpression = iteratorExpression;
		}

		final LexForInBinding inBinding;

		final LexExpression iteratorExpression;
	}

	private static class FormT extends Form {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			cc.add(new VmPushLabelScope());
			final VmAddLabel vmContinueLabel = VmAddLabel.newContinue();
			final VmAddLabel vmBreakLabel = VmAddLabel.newBreak();
			cc.add(vmContinueLabel);
			cc.add(vmBreakLabel);
			if (oInitializer != null) {
				oInitializer.compile(cc);
				cc.add(new VmPop(1));
			}
			final InstructionAddress loopEntryAddress = cc.nextAddress();
			final VmJumpConditional vmControl = new VmJumpConditional(false);
			if (oContinueExpression != null) {
				oContinueExpression.compile(cc);
				cc.add(vmControl);
			}
			statement.compile(cc);
			final InstructionAddress continueAddress = cc.nextAddress();
			if (oEndExpression != null) {
				cc.advanceLineIndex();
				oEndExpression.compile(cc);
				cc.add(new VmPop(1));
			}
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
			final StringBuilder b = new StringBuilder();
			b.append("for(");
			if (oInitializer != null) {
				b.append(oInitializer.toScript());
			}
			b.append(';');
			if (oContinueExpression != null) {
				b.append(oContinueExpression.toScript());
			}
			b.append(';');
			if (oEndExpression != null) {
				b.append(oEndExpression.toScript());
			}
			b.append(")\n");
			b.append(statement.toScript());
			return b.toString();
		}

		public FormT(LexForInitializer oInitializer, LexExpression oContinueExpression, LexExpression oEndExpression,
				LexStatement statement) {
			super(statement);
			this.oInitializer = oInitializer;
			this.oContinueExpression = oContinueExpression;
			this.oEndExpression = oEndExpression;
		}
		final LexForInitializer oInitializer;

		final LexExpression oContinueExpression;

		final LexExpression oEndExpression;
	}

}
