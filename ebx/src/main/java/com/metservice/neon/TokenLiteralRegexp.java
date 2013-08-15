/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.regex.Pattern;

import com.metservice.argon.Ds;

/**
 * 
 * @author roach
 */
class TokenLiteralRegexp extends Token {

	@Override
	public String toScript() {
		final StringBuilder b = new StringBuilder();
		b.append('/');
		b.append(pattern.pattern());
		b.append('/');
		if (caseInsensitive) {
			b.append('i');
		}
		if (multiLine) {
			b.append('m');
		}
		return b.toString();
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("pattern", pattern);
		return ds.s();
	}

	TokenLiteralRegexp(int lineIndex, int cposIndex, Pattern pattern, boolean caseInsensitive, boolean multiLine) {
		super(lineIndex, cposIndex);
		assert pattern != null;
		this.pattern = pattern;
		this.caseInsensitive = caseInsensitive;
		this.multiLine = multiLine;
	}
	public final Pattern pattern;
	public final boolean caseInsensitive;
	public final boolean multiLine;
}
