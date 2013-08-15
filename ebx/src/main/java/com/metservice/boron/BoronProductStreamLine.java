/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public class BoronProductStreamLine implements IBoronProduct {

	public boolean isPrompt() {
		return m_isPrompt;
	}

	public boolean isStdErr() {
		return m_type == OutStreamType.StdErr;
	}

	@Override
	public boolean isTerminal() {
		return false;
	}

	@Override
	public String toString() {
		return "LINE " + m_type + " '" + m_zLine + "'" + (m_isPrompt ? " PROMPT" : "");
	}

	public String zLine() {
		return m_zLine;
	}

	BoronProductStreamLine(OutStreamType type, boolean isPrompt, String zLine) {
		if (type == null) throw new IllegalArgumentException("object is null");
		if (zLine == null) throw new IllegalArgumentException("object is null");
		m_type = type;
		m_isPrompt = isPrompt;
		m_zLine = zLine;
	}

	private final OutStreamType m_type;
	private final boolean m_isPrompt;
	private final String m_zLine;
}
