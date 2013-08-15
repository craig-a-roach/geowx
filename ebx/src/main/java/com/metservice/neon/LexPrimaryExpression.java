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
class LexPrimaryExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_orSimpleExpression != null) {
			m_orSimpleExpression.compile(cc);
			return;
		}

		if (m_orFunctionExpression != null) {
			m_orFunctionExpression.compile(cc);
			return;
		}

		if (m_orObjectLiteral != null) {
			m_orObjectLiteral.compile(cc);
			return;
		}

		throw new EsCompilerException("Unsupported PrimaryExpression", cc.here());
	}

	@Override
	public String toScript() {
		if (m_orSimpleExpression != null) return m_orSimpleExpression.toScript();
		if (m_orFunctionExpression != null) return m_orFunctionExpression.toScript();
		if (m_orObjectLiteral != null) return m_orObjectLiteral.toScript();
		return "";
	}

	public static LexPrimaryExpression createInstance(TokenReader tr, Alpha alpha)
			throws EsSyntaxException {
		final LexSimpleExpression oSimpleExpression = LexSimpleExpression.createInstance(tr);
		if (oSimpleExpression != null) return new LexPrimaryExpression(oSimpleExpression);

		if (alpha == Alpha.Normal) {
			final LexFunctionExpression oFunctionExpression = LexFunctionExpression.createInstance(tr);
			if (oFunctionExpression != null) return new LexPrimaryExpression(oFunctionExpression);
			final LexObjectLiteral oObjectLiteral = LexObjectLiteral.createInstance(tr);
			if (oObjectLiteral != null) return new LexPrimaryExpression(oObjectLiteral);
		}

		return null;
	}
	public static LexPrimaryExpression newInstance(TokenReader tr, Alpha alpha)
			throws EsSyntaxException {
		final LexPrimaryExpression o = createInstance(tr, alpha);
		if (o == null) throw new EsSyntaxException("Expecting a PrimaryExpression", tr);
		return o;
	}
	private LexPrimaryExpression(LexFunctionExpression functionExpression) {
		assert functionExpression != null;
		m_orSimpleExpression = null;
		m_orFunctionExpression = functionExpression;
		m_orObjectLiteral = null;
	}

	private LexPrimaryExpression(LexObjectLiteral objectLiteral) {
		m_orSimpleExpression = null;
		m_orFunctionExpression = null;
		m_orObjectLiteral = objectLiteral;
	}

	private LexPrimaryExpression(LexSimpleExpression simpleExpression) {
		assert simpleExpression != null;
		m_orSimpleExpression = simpleExpression;
		m_orFunctionExpression = null;
		m_orObjectLiteral = null;
	}

	private final LexSimpleExpression m_orSimpleExpression;
	private final LexFunctionExpression m_orFunctionExpression;
	private final LexObjectLiteral m_orObjectLiteral;
}
