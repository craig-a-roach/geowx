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
class TokenLiteralString extends Token {
	@Override
	public String toScript() {
		return "\'" + zccValue + "\'";
	}
	@Override
	public String toString() {
		return toScript();
	}

	TokenLiteralString(int lineIndex, int startIndex, String zccValue) {
		super(lineIndex, startIndex);
		assert zccValue != null;
		this.zccValue = zccValue;
	}
	public final String zccValue;
}
