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
class LexUnaryExpression extends CompileableLex {

	public static LexUnaryExpression createInstance(TokenReader tr, Alpha alpha, LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		if (oLeftSideExpression == null && tr.current().isUnaryOperator()) {
			final Token operatorToken = tr.current();
			tr.consume();
			return new LexUnaryExpression(operatorToken, LexUnaryExpression.newInstance(tr, Alpha.Normal, null));
		}

		final LexPostfixExpression oPostfixExpression = LexPostfixExpression.createInstance(tr, alpha, oLeftSideExpression);
		if (oPostfixExpression != null) return new LexUnaryExpression(oPostfixExpression);

		return null;
	}

	public static LexUnaryExpression newInstance(TokenReader tr, Alpha alpha, LexLeftSideExpression oLeftSideExpression)
			throws EsSyntaxException {
		final LexUnaryExpression o = createInstance(tr, alpha, oLeftSideExpression);
		if (o == null) throw new EsSyntaxException("Expecting a UnaryExpression", tr);
		return o;
	}

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		m_form.compile(cc);
	}

	@Override
	public String toScript() {
		return m_form.toScript();
	}

	private LexUnaryExpression(LexPostfixExpression postfixExpression) {
		m_form = new FormP(postfixExpression);
	}

	private LexUnaryExpression(Token unaryOperatorToken, LexUnaryExpression unaryExpression) {
		m_form = new FormU(unaryOperatorToken, unaryExpression);
	}

	private final Form m_form;

	private static abstract class Form {

		abstract void compile(CompilationContext cc)
				throws EsSyntaxException;

		abstract String toScript();

		protected Form() {
		}
	}

	private static class FormP extends Form {

		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			postfixExpression.compile(cc);
		}

		@Override
		public String toScript() {
			return postfixExpression.toScript();
		}

		public FormP(LexPostfixExpression postfixExpression) {
			assert postfixExpression != null;
			this.postfixExpression = postfixExpression;
		}
		final LexPostfixExpression postfixExpression;
	}

	private static class FormU extends Form {

		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			m_unaryExpression.compile(cc);
			if (m_unaryOperatorToken instanceof TokenPunctuator) {
				final Punctuator punctuator = ((TokenPunctuator) m_unaryOperatorToken).punctuator;
				switch (punctuator) {
					case PLUS:
						cc.add(VmUnary.newToNumber());
						return;
					case MINUS:
						cc.add(VmUnary.newNegate());
						return;
					case EXCLAMATION:
						cc.add(VmUnary.newLogicalNot());
						return;
					default:
				}
			} else if (m_unaryOperatorToken instanceof TokenKeyword) {
				final Keyword keyword = ((TokenKeyword) m_unaryOperatorToken).keyword;
				switch (keyword) {
					case ISDEFINED:
						cc.add(VmUnary.newIsDefined());
						return;
					case SUBTYPEOF:
						cc.add(VmUnary.newSubTypeOf());
						return;
					case TYPEOF:
						cc.add(VmUnary.newTypeOf());
						return;
					case DELETE:
						cc.add(VmUnary.newDelete());
						return;
					case VOID:
						cc.add(VmUnary.newVoid());
						return;
					default:
				}
			}

			throw new EsCompilerException("Unsupported unary operator", cc.here(m_unaryOperatorToken));
		}

		@Override
		public String toScript() {
			return m_unaryOperatorToken.toScript() + m_unaryExpression.toScript();
		}

		public FormU(Token unaryOperatorToken, LexUnaryExpression unaryExpression) {
			m_unaryOperatorToken = unaryOperatorToken;
			m_unaryExpression = unaryExpression;
		}

		private final Token m_unaryOperatorToken;
		private final LexUnaryExpression m_unaryExpression;
	}
}
