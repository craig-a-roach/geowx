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
class LexCallExpression extends CompileableLex {
	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		m_form.compile(cc);
	}

	@Override
	public String toScript() {
		return m_form.toScript();
	}

	public static LexCallExpression newInstance(TokenReader tr, Alpha alpha, LexMemberExpression memberExpression)
			throws EsSyntaxException {
		if (memberExpression == null) throw new IllegalArgumentException("memberExpression is null");

		final LexArguments arguments = LexArguments.newInstance(tr);
		LexCallExpression lhs = new LexCallExpression(memberExpression, arguments);
		while (tr.current().isCallOperator()) {
			if (tr.current().isPunctuator(Punctuator.DOT)) {
				tr.consume();
				if (tr.current().isIdentifier()) {
					final Token identifierToken = tr.current();
					tr.consume();
					lhs = new LexCallExpression(lhs, identifierToken);
				} else
					throw new EsSyntaxException("Expecting an identifier", tr);
			} else if (tr.current().isPunctuator(Punctuator.LSQUARE)) {
				tr.consume();
				final LexExpression expression = LexExpression.newInstance(tr, Alpha.Normal, Beta.AllowIn);
				tr.consumePunctuator(Punctuator.RSQUARE);
				lhs = new LexCallExpression(lhs, expression);
			} else {
				lhs = new LexCallExpression(lhs, LexArguments.newInstance(tr));
			}

		}
		return lhs;
	}

	private LexCallExpression(LexCallExpression lhs, LexArguments arguments) {
		m_form = new FormLA(lhs, arguments);
	}

	private LexCallExpression(LexCallExpression lhs, LexExpression expression) {
		m_form = new FormLE(lhs, expression);
	}

	private LexCallExpression(LexCallExpression lhs, Token identifierToken) {
		m_form = new FormLI(lhs, identifierToken);
	}

	private LexCallExpression(LexMemberExpression memberExpression, LexArguments arguments) {
		m_form = new FormM(memberExpression, arguments);
	}

	private final Form m_form;

	private static abstract class Form {
		abstract void compile(CompilationContext cc)
				throws EsSyntaxException;

		abstract String toScript();

		protected Form() {
		}
	}

	private static abstract class FormL extends Form {
		protected FormL(LexCallExpression lhs) {
			assert lhs != null;
			this.lhs = lhs;
		}
		final LexCallExpression lhs;
	}

	private static class FormLA extends FormL {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			lhs.compile(cc);
			arguments.compile(cc);
			cc.add(new VmCall());
		}

		@Override
		String toScript() {
			return lhs.toScript() + arguments.toScript();
		}

		public FormLA(LexCallExpression lhs, LexArguments arguments) {
			super(lhs);
			assert arguments != null;
			this.arguments = arguments;
		}
		LexArguments arguments;
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

		public FormLE(LexCallExpression lhs, LexExpression expression) {
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

		public FormLI(LexCallExpression lhs, Token identifierToken) {
			super(lhs);
			assert identifierToken != null;
			this.identifierToken = identifierToken;
		}
		final Token identifierToken;
	}

	private static class FormM extends Form {
		@Override
		void compile(CompilationContext cc)
				throws EsSyntaxException {
			memberExpression.compile(cc);
			arguments.compile(cc);
			cc.add(new VmCall());
		}

		@Override
		String toScript() {
			return memberExpression.toScript() + arguments.toScript();
		}

		public FormM(LexMemberExpression memberExpression, LexArguments arguments) {
			assert memberExpression != null;
			assert arguments != null;
			this.memberExpression = memberExpression;
			this.arguments = arguments;
		}
		final LexMemberExpression memberExpression;
		final LexArguments arguments;
	}
}
