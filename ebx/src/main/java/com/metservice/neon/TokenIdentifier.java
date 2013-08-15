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
class TokenIdentifier extends Token {
	@Override
	public String toScript() {
		return qccIdentifier;
	}

	@Override
	public String toString() {
		return toScript();
	}

	TokenIdentifier(int lineIndex, int startIndex, String qccIdentifier) {
		super(lineIndex, startIndex);
		assert qccIdentifier != null && qccIdentifier.length() > 0;
		this.qccIdentifier = qccIdentifier;
	}
	public final String qccIdentifier;
}
