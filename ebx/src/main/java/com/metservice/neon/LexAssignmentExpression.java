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
class LexAssignmentExpression extends CompileableLex {
	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		m_form.compile(cc);
	}

	@Override
	public String toScript() {
		return m_form.toScript();
	}

	public static LexAssignmentExpression createInstance(TokenReader tr, Alpha alpha, Beta beta)
			throws EsSyntaxException {
		final LexLeftSideExpression oLeftSideExpression = LexLeftSideExpression.createInstance(tr, alpha);
		if (oLeftSideExpression == null) {
			final LexConditionalExpression oConditionalExpression = LexConditionalExpression.createInstance(tr, alpha, beta,
					null);
			if (oConditionalExpression == null) return null;
			return new LexAssignmentExpression(oConditionalExpression);
		}

		if (tr.current().isAssignmentOperator()) {
			final Token operatorToken = tr.current();
			tr.consume();
			final LexAssignmentExpression assignmentExpression = LexAssignmentExpression.newInstance(tr, Alpha.Normal, beta);
			return new LexAssignmentExpression(oLeftSideExpression, operatorToken, assignmentExpression);
		}

		final LexConditionalExpression conditionalExpression = LexConditionalExpression.newInstance(tr, alpha, beta,
				oLeftSideExpression);
		return new LexAssignmentExpression(conditionalExpression);
	}

	public static LexAssignmentExpression newInstance(TokenReader tr, Alpha alpha, Beta beta)
			throws EsSyntaxException {
		final LexAssignmentExpression o = createInstance(tr, alpha, beta);
		if (o == null) throw new EsSyntaxException("Expecting an AssignmentExpression", tr);
		return o;
	}

	private LexAssignmentExpression(LexConditionalExpression conditionalExpression) {
		m_form = new FormC(conditionalExpression);
	}

	private LexAssignmentExpression(LexLeftSideExpression leftSideExpression, Token assignmentOperatorToken,
			LexAssignmentExpression assignmentExpression) {
		m_form = new FormA(leftSideExpression, assignmentOperatorToken, assignmentExpression);
	}

	private final Form m_form;

	private static abstract class Form {
		abstract void compile(CompilationContext cc)
				throws EsSyntaxException;

		abstract String toScript();

		protected Form() {
		}
	}

	private static class FormA extends Form {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			final Punctuator assignmentOperator = ((TokenPunctuator) assignmentOperatorToken).punctuator;
			leftSideExpression.compile(cc);
			switch (assignmentOperator) {
				case EQUALS: {
					assignmentExpression.compile(cc);
				}
				break;
				case AND_ASSIGN: {
					cc.add(new VmGetValue(true));
					final VmLogicalAnd vmAnd = new VmLogicalAnd();
					cc.add(vmAnd);
					assignmentExpression.compile(cc);
					vmAnd.setJumpAddress(cc.nextAddress());
				}
				break;
				case OR_ASSIGN: {
					cc.add(new VmGetValue(true));
					final VmLogicalOr vmOr = new VmLogicalOr();
					cc.add(vmOr);
					assignmentExpression.compile(cc);
					vmOr.setJumpAddress(cc.nextAddress());
				}
				break;
				case PLUS_ASSIGN: {
					cc.add(new VmGetValue(true));
					assignmentExpression.compile(cc);
					cc.add(VmAdditive.newPlus());
				}
				break;
				case MINUS_ASSIGN: {
					cc.add(new VmGetValue(true));
					assignmentExpression.compile(cc);
					cc.add(VmAdditive.newMinus());
				}
				break;
				case MULTIPLY_ASSIGN: {
					cc.add(new VmGetValue(true));
					assignmentExpression.compile(cc);
					cc.add(VmMultiplicative.newMultiply());
				}
				break;
				case DIVIDE_ASSIGN: {
					cc.add(new VmGetValue(true));
					assignmentExpression.compile(cc);
					cc.add(VmMultiplicative.newDivide());
				}
				break;
				case REM_ASSIGN: {
					cc.add(new VmGetValue(true));
					assignmentExpression.compile(cc);
					cc.add(VmMultiplicative.newRemainder());
				}
				break;
				default: {
					throw new EsCompilerException("Assignment operator " + assignmentOperator + " not yet supported");
				}
			}
			cc.add(VmPutValue.newKeepValue());
		}

		@Override
		public String toScript() {
			return leftSideExpression.toScript() + assignmentOperatorToken.toScript() + assignmentExpression.toScript();
		}

		public FormA(LexLeftSideExpression leftSideExpression, Token assignmentOperatorToken,
				LexAssignmentExpression assignmentExpression) {
			assert leftSideExpression != null;
			assert assignmentOperatorToken != null;
			assert assignmentExpression != null;
			this.leftSideExpression = leftSideExpression;
			this.assignmentOperatorToken = assignmentOperatorToken;
			this.assignmentExpression = assignmentExpression;
		}
		final LexLeftSideExpression leftSideExpression;

		final Token assignmentOperatorToken;

		final LexAssignmentExpression assignmentExpression;
	}// A

	private static class FormC extends Form {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			conditionalExpression.compile(cc);
		}

		@Override
		public String toScript() {
			return conditionalExpression.toScript();
		}

		public FormC(LexConditionalExpression conditionalExpression) {
			assert conditionalExpression != null;
			this.conditionalExpression = conditionalExpression;
		}
		final LexConditionalExpression conditionalExpression;
	}// C
}
