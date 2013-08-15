/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
public class NeonScriptRunException extends NeonScriptException {

	public final String causeMessage() {
		return m_causeMessage;
	}

	NeonScriptRunException(String diagnostic, String causeMessage) {
		super(diagnostic);
		m_causeMessage = causeMessage == null || causeMessage.length() == 0 ? "Unspecified" : causeMessage;
	}

	private final String m_causeMessage;
}
