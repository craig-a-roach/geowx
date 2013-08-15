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
class LexVariableDeclaration extends CompileableLex {
	@Override
	public void compile(CompilationContext cc)
			throws EsSyntaxException {
		final String qccIdentifier = ((TokenIdentifier) m_identifierToken).qccIdentifier;
		if (!cc.declareVariable(qccIdentifier)) {
			final String m = "Duplicate declaration of variable '" + qccIdentifier + "'";
			throw new EsSyntaxException(m, cc);
		}
		if (m_oInitializerAssignmentExpression == null) {
			if (m_keepReference) {
				cc.add(new VmResolveIdentifier(qccIdentifier));
			}
		} else {
			cc.add(new VmResolveIdentifier(qccIdentifier));
			m_oInitializerAssignmentExpression.compile(cc);
			if (m_keepReference) {
				cc.add(VmPutValue.newKeepReference());
			} else {
				cc.add(VmPutValue.newDiscardOperands());
			}
		}
	}

	@Override
	public String toScript() {
		final StringBuffer b = new StringBuffer();
		b.append(m_identifierToken.toScript());
		if (m_oInitializerAssignmentExpression != null) {
			b.append("=");
			b.append(m_oInitializerAssignmentExpression.toScript());
		}
		return b.toString();
	}

	public static LexVariableDeclaration createInstance(TokenReader tr, Beta beta, boolean keepReference)
			throws EsSyntaxException {
		if (tr.current().isIdentifier()) {
			final Token identifierToken = tr.current();
			tr.consume();
			LexAssignmentExpression oInitializerAssignmentExpression = null;
			if (tr.current().isPunctuator(Punctuator.EQUALS)) {
				tr.consume();
				oInitializerAssignmentExpression = LexAssignmentExpression.newInstance(tr, Alpha.Normal, beta);
			}
			return new LexVariableDeclaration(identifierToken, oInitializerAssignmentExpression, keepReference);
		}
		return null;
	}
	public static LexVariableDeclaration newInstance(TokenReader tr, Beta beta, boolean keepReference)
			throws EsSyntaxException {
		final LexVariableDeclaration o = createInstance(tr, beta, keepReference);
		if (o == null) throw new EsSyntaxException("Expecting a VariableDeclaration", tr);
		return o;
	}
	private LexVariableDeclaration(Token identifierToken, LexAssignmentExpression oInitializerAssignmentExpression,
			boolean keepReference) {
		assert identifierToken != null;
		m_identifierToken = identifierToken;
		m_oInitializerAssignmentExpression = oInitializerAssignmentExpression;
		m_keepReference = keepReference;
	}
	private final Token m_identifierToken;

	private final LexAssignmentExpression m_oInitializerAssignmentExpression;

	private final boolean m_keepReference;
}
