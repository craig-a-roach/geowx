/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.Real;

/**
 * 
 * @author roach
 */
class LexSimpleExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_orToken != null) {
			compile(m_orToken, cc);
			return;
		}

		if (m_orParenthesizedExpression != null) {
			m_orParenthesizedExpression.compile(cc);
			return;
		}

		if (m_orArrayLiteral != null) {
			m_orArrayLiteral.compile(cc);
			return;
		}

		throw new EsCompilerException("Unsupported SimpleExpression", cc.here());
	}

	@Override
	public String toScript() {
		if (m_orToken != null) return m_orToken.toScript();
		if (m_orParenthesizedExpression != null) return m_orParenthesizedExpression.toScript();
		if (m_orArrayLiteral != null) return m_orArrayLiteral.toScript();
		return "";
	}

	private static void compile(Token token, CompilationContext cc)
			throws EsSyntaxException {
		if (token instanceof TokenLiteralString) {
			final String zccValue = ((TokenLiteralString) token).zccValue;
			cc.add(new VmPushLiteral(new EsPrimitiveString(zccValue)));
			return;
		}

		if (token instanceof TokenLiteralNumeric) {
			if (token instanceof TokenLiteralNumericIntegral) {
				final int value = ((TokenLiteralNumericIntegral) token).value;
				cc.add(new VmPushLiteral(new EsPrimitiveNumberInteger(value)));
				return;
			}

			if (token instanceof TokenLiteralNumericReal) {
				final Real value = ((TokenLiteralNumericReal) token).value;
				cc.add(new VmPushLiteral(new EsPrimitiveNumberReal(value)));
				return;
			}

			if (token instanceof TokenLiteralNumericDouble) {
				final double value = ((TokenLiteralNumericDouble) token).value;
				cc.add(new VmPushLiteral(new EsPrimitiveNumberDouble(value)));
				return;
			}

			if (token instanceof TokenLiteralNumericElapsed) {
				final long value = ((TokenLiteralNumericElapsed) token).sms;
				cc.add(new VmPushLiteral(new EsPrimitiveNumberElapsed(value)));
				return;
			}

			throw new EsCompilerException("Unsupported numeric token", cc.here(token));
		}

		if (token instanceof TokenBooleanLiteral) {
			final boolean value = ((TokenBooleanLiteral) token).value();
			cc.add(new VmPushLiteral(EsPrimitiveBoolean.instance(value)));
			return;
		}

		if (token instanceof TokenNullLiteral) {
			cc.add(new VmPushLiteral(EsPrimitiveNull.Instance));
			return;
		}

		if (token instanceof TokenIdentifier) {
			final String qccIdentifier = ((TokenIdentifier) token).qccIdentifier;
			cc.add(new VmResolveIdentifier(qccIdentifier));
			return;
		}

		if (token.isKeyword(Keyword.THIS)) {
			cc.add(new VmPushThis());
			return;
		}

		if (token instanceof TokenLiteralRegexp) {
			final TokenLiteralRegexp regexpLiteralToken = (TokenLiteralRegexp) token;
			cc.add(new VmNewRegExp(regexpLiteralToken.pattern));
			return;
		}

		if (token instanceof TokenLiteralXml) {
			final TokenLiteralXml xmlLiteralToken = (TokenLiteralXml) token;
			cc.add(new VmNewW3cDom(xmlLiteralToken.dom));
			return;
		}

		throw new EsCompilerException("Unsupported token", cc.here(token));
	}

	public static LexSimpleExpression createInstance(TokenReader tr)
			throws EsSyntaxException {
		final Token token = tr.current();
		if (token.isLiteral() || token.isIdentifier()) {
			tr.consume();
			return new LexSimpleExpression(token);
		}

		if (token.isKeyword(Keyword.THIS)) {
			tr.consume();
			return new LexSimpleExpression(token);
		}

		final LexParenthesizedExpression oParenthesizedExpression = LexParenthesizedExpression.createInstance(tr);
		if (oParenthesizedExpression != null) return new LexSimpleExpression(oParenthesizedExpression);

		final LexArrayLiteral oArrayLiteral = LexArrayLiteral.createInstance(tr);
		if (oArrayLiteral != null) return new LexSimpleExpression(oArrayLiteral);

		return null;
	}

	public static LexSimpleExpression newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexSimpleExpression o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a SimpleExpression", tr);
		return o;
	}

	private LexSimpleExpression(LexArrayLiteral arrayLiteral) {
		assert arrayLiteral != null;
		m_orToken = null;
		m_orParenthesizedExpression = null;
		m_orArrayLiteral = arrayLiteral;
	}

	private LexSimpleExpression(LexParenthesizedExpression parenthesizedExpression) {
		assert parenthesizedExpression != null;
		m_orToken = null;
		m_orParenthesizedExpression = parenthesizedExpression;
		m_orArrayLiteral = null;
	}

	private LexSimpleExpression(Token token) {
		assert token != null;
		m_orToken = token;
		m_orParenthesizedExpression = null;
		m_orArrayLiteral = null;
	}

	private final Token m_orToken;

	private final LexParenthesizedExpression m_orParenthesizedExpression;

	private final LexArrayLiteral m_orArrayLiteral;
}
