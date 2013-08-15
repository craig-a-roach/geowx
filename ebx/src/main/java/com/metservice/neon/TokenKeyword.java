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
class TokenKeyword extends TokenReservedWord {
	@Override
	public String toScript() {
		return keyword.qCode;
	}

	@Override
	public String toString() {
		return toScript();
	}

	TokenKeyword(int lineIndex, int startIndex, Keyword keyword) {
		super(lineIndex, startIndex);
		assert keyword != null;
		this.keyword = keyword;
	}
	public final Keyword keyword;
}
