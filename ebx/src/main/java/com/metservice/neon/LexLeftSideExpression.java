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
class LexLeftSideExpression extends CompileableLex {

	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		if (m_orMemberExpression != null) {
			m_orMemberExpression.compile(cc);
			return;
		}

		if (m_orCallExpression != null) {
			m_orCallExpression.compile(cc);
			return;
		}
	}

	@Override
	public String toScript() {
		if (m_orMemberExpression != null) return m_orMemberExpression.toScript();
		if (m_orCallExpression != null) return m_orCallExpression.toScript();
		return "";
	}

	public static LexLeftSideExpression createInstance(TokenReader tr, Alpha alpha)
			throws EsSyntaxException {
		final LexMemberExpression oMemberExpression = LexMemberExpression.createInstance(tr, alpha);
		if (oMemberExpression == null) return null;

		if (tr.current().isArgumentOperator())
			return new LexLeftSideExpression(LexCallExpression.newInstance(tr, alpha, oMemberExpression));
		return new LexLeftSideExpression(oMemberExpression);
	}

	public static LexLeftSideExpression newInstance(TokenReader tr, Alpha alpha)
			throws EsSyntaxException {
		final LexLeftSideExpression o = createInstance(tr, alpha);
		if (o == null) throw new EsSyntaxException("Expecting a LeftSideExpression", tr);
		return o;
	}

	private LexLeftSideExpression(LexCallExpression callExpression) {
		assert callExpression != null;
		m_orCallExpression = callExpression;
		m_orMemberExpression = null;
	}

	private LexLeftSideExpression(LexMemberExpression memberExpression) {
		assert memberExpression != null;
		m_orMemberExpression = memberExpression;
		m_orCallExpression = null;
	}

	private final LexMemberExpression m_orMemberExpression;
	private final LexCallExpression m_orCallExpression;
}
