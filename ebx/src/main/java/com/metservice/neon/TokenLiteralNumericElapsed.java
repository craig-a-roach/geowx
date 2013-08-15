/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ElapsedFormatter;

/**
 * 
 * @author roach
 */
class TokenLiteralNumericElapsed extends TokenLiteralNumeric {
	@Override
	public String toScript() {
		return ElapsedFormatter.formatSingleUnit(sms);
	}

	@Override
	public String toString() {
		return toScript();
	}

	TokenLiteralNumericElapsed(int lineIndex, int startIndex, long sms) {
		super(lineIndex, startIndex);
		this.sms = sms;
	}
	public final long sms;
}
