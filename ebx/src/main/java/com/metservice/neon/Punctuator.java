/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.ICodedEnum;

/**
 * 
 * @author roach
 */
enum Punctuator implements ICodedEnum {
	DOT(".", "dot", "[MB]"),
	EQUALS("=", "equals", "[AS]"),
	COMMA(",", "comma", ""),
	SEMICOLON(";", "semicolon", ""),
	PLUS("+", "plus", "[AD][UN]"),
	MINUS("-", "minus", "[AD][UN]"),
	STAR("*", "star", "[MU]"),
	FWDSLASH("/", "forward-slash", "[MU]"),
	PERCENT("%", "percent", "[MU]"),
	EXCLAMATION("!", "exclamation-mark", "[UN]"),
	LPAREN("(", "left parenthesis", "[AR]"),
	RPAREN(")", "right parenthesis", ""),
	LSQUARE("[", "left square bracket", "[MB]"),
	RSQUARE("]", "right square bracket", ""),
	LBRACE("{", "left brace", ""),
	RBRACE("}", "right brace", ""),
	COLON(":", "colon", ""),
	LOGICAL_OR("||", "logical or", ""),
	LOGICAL_AND("&&", "logical and", ""),
	EQ_RELATION("==", "equals relation", "[EQ]"),
	EQS_RELATION("===", "strict equals relation", "[EQ]"),
	NEQ_RELATION("!=", "not equals relation", "[EQ]"),
	NEQS_RELATION("!==", "strict not equals relation", "[EQ]"),
	LT_RELATION("<", "less-than relation", "[RE]"),
	GT_RELATION(">", "greater-than relation", "[RE]"),
	LEQ_RELATION("<=", "less-than-or-equal-to relation", "[RE]"),
	GEQ_RELATION(">=", "greater-than-or-equal-to relation", "[RE]"),
	PLUSPLUS("++", "plus-plus", "[PF]"),
	MINUSMINUS("--", "minus-minus", "[PF]"),
	MULTIPLY_ASSIGN("*=", "multiply-assign", "[AS]"),
	DIVIDE_ASSIGN("/=", "divide-assign", "[AS]"),
	REM_ASSIGN("%=", "remainder-assign", "[AS]"),
	PLUS_ASSIGN("+=", "plus-assign", "[AS]"),
	MINUS_ASSIGN("-=", "minus-assign", "[AS]"),
	AND_ASSIGN("&=", "and-assign", "[AS]"),
	OR_ASSIGN("|=", "or-assign", "[AS]"),
	QUESTION("?", "question mark", ""), ;

	public String qCode() {
		return qCode;
	}

	Punctuator(String qCode, String qDescription, String attributeMask) {
		assert qCode != null && qCode.length() > 0;
		assert qDescription != null && qDescription.length() > 0;
		this.qCode = qCode;
		this.qDescription = qDescription;
		this.isAssignmentOperator = attributeMask.contains("[AS]");
		this.isEqualityOperator = attributeMask.contains("[EQ]");
		this.isRelationalOperator = attributeMask.contains("[RE]");
		this.isAdditiveOperator = attributeMask.contains("[AD]");
		this.isMultiplicativeOperator = attributeMask.contains("[MU]");
		this.isUnaryOperator = attributeMask.contains("[UN]");
		this.isPostfixOperator = attributeMask.contains("[PF]");
		this.isMemberOperator = attributeMask.contains("[MB]");
		this.isArgumentOperator = attributeMask.contains("[AR]");
		this.isCallOperator = isMemberOperator || isArgumentOperator;
	}
	public static final CodedEnumTable<Punctuator> Table = new CodedEnumTable<Punctuator>(Punctuator.class, true, Punctuator
			.values());
	public final String qCode;
	public final String qDescription;
	public final boolean isAssignmentOperator;
	public final boolean isEqualityOperator;
	public final boolean isRelationalOperator;
	public final boolean isAdditiveOperator;
	public final boolean isMultiplicativeOperator;
	public final boolean isUnaryOperator;
	public final boolean isPostfixOperator;
	public final boolean isMemberOperator;
	public final boolean isArgumentOperator;

	public final boolean isCallOperator;
}
