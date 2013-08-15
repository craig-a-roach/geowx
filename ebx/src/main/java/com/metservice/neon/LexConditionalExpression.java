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
class LexConditionalExpression extends CompileableLex {
	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		m_form.compile(cc);
	}

	@Override
	public String toScript() {
		return m_form.toScript();
	}

	public static LexConditionalExpression createInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexLogicalOrExpression oLogicalOrExpression = LexLogicalOrExpression.createInstance(tr, alpha, beta,
				oLeftSideExpression);
		if (oLogicalOrExpression == null) return null;

		if (tr.current().isPunctuator(Punctuator.QUESTION)) {
			tr.consume();
			final LexAssignmentExpression trueAssignmentExpression = LexAssignmentExpression.newInstance(tr, Alpha.Normal,
					beta);
			tr.consumePunctuator(Punctuator.COLON);
			final LexAssignmentExpression falseAssignmentExpression = LexAssignmentExpression.newInstance(tr, Alpha.Normal,
					beta);
			return new LexConditionalExpression(oLogicalOrExpression, trueAssignmentExpression, falseAssignmentExpression);
		}
		return new LexConditionalExpression(oLogicalOrExpression);
	}

	public static LexConditionalExpression newInstance(TokenReader tr, Alpha alpha, Beta beta,
			LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexConditionalExpression o = createInstance(tr, alpha, beta, oLeftSideExpression);
		if (o == null) throw new EsSyntaxException("Expecting a ConditionalExpression", tr);
		return o;
	}

	private LexConditionalExpression(LexLogicalOrExpression logicalOrExpression) {
		m_form = new FormS(logicalOrExpression);
	}

	private LexConditionalExpression(LexLogicalOrExpression logicalOrExpression,
			LexAssignmentExpression trueAssignmentExpression, LexAssignmentExpression falseAssignmentExpression) {
		m_form = new FormC(logicalOrExpression, trueAssignmentExpression, falseAssignmentExpression);
	}

	private final Form m_form;

	private static abstract class Form {
		abstract void compile(CompilationContext cc)
				throws EsSyntaxException;

		abstract String toScript();

		protected Form(LexLogicalOrExpression logicalOrExpression) {
			assert logicalOrExpression != null;
			this.logicalOrExpression = logicalOrExpression;
		}
		final LexLogicalOrExpression logicalOrExpression;
	}

	private static class FormC extends Form {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			logicalOrExpression.compile(cc);
			final VmJumpConditional vmConditional = new VmJumpConditional(false);
			cc.add(vmConditional);
			trueAssignmentExpression.compile(cc);
			final VmJumpAlways vmSkip = new VmJumpAlways();
			cc.add(vmSkip);
			vmConditional.setJumpAddress(cc.nextAddress());
			falseAssignmentExpression.compile(cc);
			vmSkip.setJumpAddress(cc.nextAddress());
		}

		@Override
		public String toScript() {
			return logicalOrExpression.toScript() + "?" + trueAssignmentExpression.toScript() + ":"
					+ falseAssignmentExpression.toScript();
		}

		public FormC(LexLogicalOrExpression logicalOrExpression, LexAssignmentExpression trueAssignmentExpression,
				LexAssignmentExpression falseAssignmentExpression) {
			super(logicalOrExpression);
			assert trueAssignmentExpression != null;
			assert falseAssignmentExpression != null;
			this.trueAssignmentExpression = trueAssignmentExpression;
			this.falseAssignmentExpression = falseAssignmentExpression;
		}

		final LexAssignmentExpression trueAssignmentExpression;

		final LexAssignmentExpression falseAssignmentExpression;
	}

	private static class FormS extends Form {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			logicalOrExpression.compile(cc);
		}

		@Override
		public String toScript() {
			return logicalOrExpression.toScript();
		}

		public FormS(LexLogicalOrExpression logicalOrExpression) {
			super(logicalOrExpression);
		}
	}
}
