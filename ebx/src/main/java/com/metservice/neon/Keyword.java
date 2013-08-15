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
enum Keyword implements ICodedEnum {
	VAR("var", ""),
	IF("if", ""),
	ELSE("else", "[CM]"),
	FUNCTION("function", ""),
	RETURN("return", ""),
	THROW("throw", ""),
	DO("do", ""),
	FOR("for", ""),
	WHILE("while", ""),
	BREAK("break", ""),
	CONTINUE("continue", ""),
	SWITCH("switch", ""),
	CHOOSE("choose", ""),
	CASE("case", "[CM]"),
	DEFAULT("default", "[CM]"),
	IN("in", "[RE]"),
	INSTANCEOF("instanceof", "[RE]"),
	NEW("new", ""),
	DELETE("delete", "[UN]"),
	THIS("this", ""),
	TYPEOF("typeof", "[UN]"),
	SUBTYPEOF("subtypeof", "[UN]"),
	ISDEFINED("isdefined", "[UN]"),
	VOID("void", "[UN]");

	public String qCode() {
		return qCode;
	}

	Keyword(String qCode, String attributeMask) {
		assert qCode != null && qCode.length() > 0;
		this.qCode = qCode;
		this.isRelationalOperator = attributeMask.contains("[RE]");
		this.isUnaryOperator = attributeMask.contains("[UN]");
		this.isClauseMarker = attributeMask.contains("[CM]");
	}
	public static final CodedEnumTable<Keyword> Table = new CodedEnumTable<Keyword>(Keyword.class, true, Keyword.values());
	public final String qCode;
	public final boolean isRelationalOperator;
	public final boolean isUnaryOperator;
	public final boolean isClauseMarker;
}
