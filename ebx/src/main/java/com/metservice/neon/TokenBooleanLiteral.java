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
class TokenBooleanLiteral extends TokenReservedWord {

	@Override
	public String toScript() {
		return booleanLiteral.toString().toLowerCase();
	}

	@Override
	public String toString() {
		return toScript();
	}

	public boolean value() {
		return booleanLiteral == BooleanLiteral.TRUE;
	}

	TokenBooleanLiteral(int lineIndex, int startIndex, BooleanLiteral booleanLiteral) {
		super(lineIndex, startIndex);
		assert booleanLiteral != null;
		this.booleanLiteral = booleanLiteral;
	}
	public final BooleanLiteral booleanLiteral;
}
