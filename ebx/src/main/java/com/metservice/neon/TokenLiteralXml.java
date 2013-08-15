/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.xml.W3cDom;

/**
 * 
 * @author roach
 */
class TokenLiteralXml extends Token {

	@Override
	public String toScript() {
		return dom.toString();
	}

	@Override
	public String toString() {
		return toScript();
	}

	TokenLiteralXml(int lineIndex, int startIndex, W3cDom dom) {
		super(lineIndex, startIndex);
		assert dom != null;
		this.dom = dom;
	}

	public final W3cDom dom;
}
