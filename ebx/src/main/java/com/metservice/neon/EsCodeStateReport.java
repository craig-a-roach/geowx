/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonText;
import com.metservice.argon.text.ArgonNumber;

/**
 * 
 * @author roach
 */
public class EsCodeStateReport {

	private void makeSourceLine(StringBuilder b) {
		if (m_oSource == null) {
			b.append("Line ");
			b.append(ArgonNumber.intToDec3(m_lineIndex));
		} else {
			b.append(m_oSource.lineHere(m_lineIndex));
		}
	}

	private void makeVariables(StringBuilder b) {
		if (m_oqVariables != null) {
			b.append("Variables\n");
			b.append(m_oqVariables);
			b.append('\n');
		}
	}

	public String sourceLine() {
		final StringBuilder b = new StringBuilder(256);
		makeSourceLine(b);
		return b.toString();
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder(512);
		makeSourceLine(b);
		b.append('\n');
		makeVariables(b);
		return b.toString();
	}

	public EsCodeStateReport(EsSource oSource, int lineIndex, String ozVariables) {
		m_oSource = oSource;
		m_lineIndex = lineIndex;
		m_oqVariables = ArgonText.oqtw(ozVariables);
	}
	private final EsSource m_oSource;
	private final int m_lineIndex;
	private final String m_oqVariables;
}
