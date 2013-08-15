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
class LexFormalParameter extends Lex {

	public String qccIdentifier() {
		return ((TokenIdentifier) m_identifierToken).qccIdentifier;
	}

	@Override
	public String toScript() {
		return m_identifierToken.toScript();
	}

	public static LexFormalParameter createInstance(TokenReader tr)
			throws EsSyntaxException {
		if (tr.current().isIdentifier()) {
			final Token identifierToken = tr.current();
			tr.consume();
			return new LexFormalParameter(identifierToken);
		}

		return null;
	}

	public static LexFormalParameter newInstance(TokenReader tr)
			throws EsSyntaxException {
		final LexFormalParameter o = createInstance(tr);
		if (o == null) throw new EsSyntaxException("Expecting a FormalParameter", tr);
		return o;
	}
	private LexFormalParameter(Token identifierToken) {
		assert identifierToken != null;
		m_identifierToken = identifierToken;
	}

	private final Token m_identifierToken;
}
