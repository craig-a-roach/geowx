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
abstract class Token {

	public boolean isAdditiveOperator() {
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isAdditiveOperator;
		return false;
	}

	public boolean isArgumentOperator() {
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isArgumentOperator;
		return false;
	}

	public boolean isAssignmentOperator() {
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isAssignmentOperator;
		return false;
	}

	public boolean isBooleanLiteral() {
		return (this instanceof TokenBooleanLiteral);
	}

	public boolean isCallOperator() {
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isCallOperator;
		return false;
	}

	public boolean isClauseMarkerKeyword() {
		if (this instanceof TokenKeyword) return ((TokenKeyword) this).keyword.isClauseMarker;
		return false;
	}

	public boolean isEqualityOperator() {
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isEqualityOperator;
		return false;
	}

	public boolean isIdentifier() {
		return (this instanceof TokenIdentifier);
	}

	public boolean isKeyword() {
		return (this instanceof TokenKeyword);
	}

	public boolean isKeyword(Keyword keyword) {
		if (keyword == null) throw new IllegalArgumentException("keyword is null");
		if (this instanceof TokenKeyword) return ((TokenKeyword) this).keyword == keyword;
		return false;
	}

	public boolean isLiteral() {
		return isNullLiteral() || isBooleanLiteral() || isNumericLiteral() || isStringLiteral() || isRegexpLiteral()
				|| isXmlLiteral();
	}

	public boolean isMemberOperator() {
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isMemberOperator;
		return false;
	}

	public boolean isMultiplicativeOperator() {
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isMultiplicativeOperator;
		return false;
	}

	public boolean isNullLiteral() {
		return (this instanceof TokenNullLiteral);
	}

	public boolean isNumericLiteral() {
		return (this instanceof TokenLiteralNumeric);
	}

	public boolean isPostfixOperator() {
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isPostfixOperator;
		return false;
	}

	public boolean isPunctuator() {
		return (this instanceof TokenPunctuator);
	}

	public boolean isPunctuator(Punctuator punctuator) {
		if (punctuator == null) throw new IllegalArgumentException("punctuator is null");
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator == punctuator;
		return false;
	}

	public boolean isRegexpLiteral() {
		return (this instanceof TokenLiteralRegexp);
	}

	public boolean isRelationalOperator(Beta beta) {
		if (beta == null) throw new IllegalArgumentException("beta is null");
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isRelationalOperator;
		if (this instanceof TokenKeyword) {
			final Keyword keyword = ((TokenKeyword) this).keyword;
			return (keyword == Keyword.IN && beta == Beta.NoIn) ? false : keyword.isRelationalOperator;
		}

		return false;
	}

	public boolean isStringLiteral() {
		return (this instanceof TokenLiteralString);
	}

	public boolean isUnaryOperator() {
		if (this instanceof TokenPunctuator) return ((TokenPunctuator) this).punctuator.isUnaryOperator;
		if (this instanceof TokenKeyword) return ((TokenKeyword) this).keyword.isUnaryOperator;
		return false;
	}

	public boolean isXmlLiteral() {
		return (this instanceof TokenLiteralXml);
	}

	public int lineIndex() {
		return lineIndex;
	}

	public int startIndex() {
		return startIndex;
	}

	public abstract String toScript();

	Token(int lineIndex, int startIndex) {
		this.lineIndex = lineIndex;
		this.startIndex = startIndex;
	}

	final int lineIndex;
	final int startIndex;
}
