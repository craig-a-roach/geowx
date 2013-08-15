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
class LexMemberExpression extends CompileableLex {
	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		m_form.compile(cc);
	}

	@Override
	public String toScript() {
		return m_form.toScript();
	}

	private static LexMemberExpression createFirst(TokenReader tr, Alpha alpha)
			throws EsSyntaxException {
		if (tr.current().isKeyword(Keyword.NEW)) {
			final Token newOperator = tr.current();
			tr.consume();
			final LexMemberExpression memberExpression = LexMemberExpression.newInstance(tr, Alpha.Normal);
			final LexArguments arguments = LexArguments.newInstance(tr);
			return new LexMemberExpression(newOperator, memberExpression, arguments);
		}

		final LexPrimaryExpression oPrimaryExpression = LexPrimaryExpression.createInstance(tr, alpha);
		if (oPrimaryExpression != null) return new LexMemberExpression(oPrimaryExpression);

		return null;
	}

	public static LexMemberExpression createInstance(TokenReader tr, Alpha alpha)
			throws EsSyntaxException {
		final LexMemberExpression oFirst = createFirst(tr, alpha);
		if (oFirst == null) return null;
		return newInstance(tr, alpha, oFirst);
	}

	public static LexMemberExpression newInstance(TokenReader tr, Alpha alpha)
			throws EsSyntaxException {
		final LexMemberExpression o = createInstance(tr, alpha);
		if (o == null) throw new EsSyntaxException("Expecting a MemberExpression", tr);
		return o;
	}

	public static LexMemberExpression newInstance(TokenReader tr, Alpha alpha, LexMemberExpression first)
			throws EsSyntaxException {
		if (first == null) throw new IllegalArgumentException("first is null");

		LexMemberExpression lhs = first;
		while (tr.current().isMemberOperator()) {
			if (tr.current().isPunctuator(Punctuator.DOT)) {
				tr.consume();
				if (tr.current().isIdentifier()) {
					final Token identifierToken = tr.current();
					tr.consume();
					lhs = new LexMemberExpression(lhs, identifierToken);
				} else
					throw new EsSyntaxException("Expecting an identifier", tr);
			} else {
				tr.consume();
				final LexExpression expression = LexExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn);
				tr.consumePunctuator(Punctuator.RSQUARE);
				lhs = new LexMemberExpression(lhs, expression);
			}
		}
		return lhs;
	}

	private LexMemberExpression(LexMemberExpression lhs, LexExpression expression) {
		m_form = new FormLE(lhs, expression);
	}

	private LexMemberExpression(LexMemberExpression lhs, Token identifierToken) {
		m_form = new FormLI(lhs, identifierToken);
	}

	private LexMemberExpression(LexPrimaryExpression primaryExpression) {
		m_form = new FormE(primaryExpression);
	}

	private LexMemberExpression(Token newOperator, LexMemberExpression memberExpression, LexArguments arguments) {
		m_form = new FormN(newOperator, memberExpression, arguments);
	}

	private final Form m_form;

	private static abstract class Form {
		abstract void compile(CompilationContext cc)
				throws EsSyntaxException;

		abstract String toScript();

		protected Form() {
		}
	}

	private static class FormE extends Form {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			primaryExpression.compile(cc);
		}

		@Override
		String toScript() {
			return primaryExpression.toScript();
		}

		public FormE(LexPrimaryExpression primaryExpression) {
			assert primaryExpression != null;
			this.primaryExpression = primaryExpression;
		}
		final LexPrimaryExpression primaryExpression;
	}

	private static abstract class FormL extends Form {
		protected FormL(LexMemberExpression lhs) {
			assert lhs != null;
			this.lhs = lhs;
		}
		final LexMemberExpression lhs;
	}

	private static class FormLE extends FormL {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			lhs.compile(cc);
			expression.compile(cc);
			cc.add(new VmAccessProperty());
		}

		@Override
		String toScript() {
			return lhs.toScript() + "[" + expression.toScript() + "]";
		}

		public FormLE(LexMemberExpression lhs, LexExpression expression) {
			super(lhs);
			assert expression != null;
			this.expression = expression;
		}
		LexExpression expression;
	}

	private static class FormLI extends FormL {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			final String qccIdentifier = ((TokenIdentifier) identifierToken).qccIdentifier;
			lhs.compile(cc);
			cc.add(new VmPushLiteral(new EsPrimitiveString(qccIdentifier)));
			cc.add(new VmAccessProperty());
		}

		@Override
		String toScript() {
			return lhs.toScript() + "." + identifierToken.toScript();
		}

		public FormLI(LexMemberExpression lhs, Token identifierToken) {
			super(lhs);
			assert identifierToken != null;
			this.identifierToken = identifierToken;
		}
		final Token identifierToken;
	}

	private static class FormN extends Form {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			memberExpression.compile(cc);
			arguments.compile(cc);
			cc.add(new VmConstruct());
		}

		@Override
		String toScript() {
			return "new " + memberExpression.toScript() + arguments.toScript();
		}

		public FormN(Token newOperator, LexMemberExpression memberExpression, LexArguments arguments) {
			assert newOperator != null;
			assert memberExpression != null;
			assert arguments != null;
			this.memberExpression = memberExpression;
			this.arguments = arguments;
		}
		final LexMemberExpression memberExpression;

		final LexArguments arguments;
	}
}
