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
class TokenNullLiteral extends TokenReservedWord {

	@Override
	public String toScript() {
		return nullLiteral.toString().toLowerCase();
	}

	@Override
	public String toString() {
		return toScript();
	}

	TokenNullLiteral(int lineIndex, int startIndex, NullLiteral nullLiteral) {
		super(lineIndex, startIndex);
		assert nullLiteral != null;
		this.nullLiteral = nullLiteral;
	}

	public final NullLiteral nullLiteral;
}
