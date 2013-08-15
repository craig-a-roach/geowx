/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.Ds;

/**
 * 
 * @author roach
 */
public abstract class EsRunException extends RuntimeException {

	private static final String STDMESSAGE = "EcmaScript Runtime Exception";

	public String causeMessage() {
		if (m_zlEsCodeStateReports.isEmpty()) return m_causeMessage;
		final EsCodeStateReport origin = m_zlEsCodeStateReports.get(0);
		return m_causeMessage + "\n" + origin.sourceLine();
	}

	public Throwable getCauseThrowable() {
		return m_oCauseThrowable;
	}

	@Override
	public String getMessage() {
		final StringBuilder b = new StringBuilder(1024);
		b.append("Cause:\n");
		b.append(m_causeMessage);
		b.append("\n");
		if (m_ozAuthors != null) {
			b.append("Author(s): ");
			b.append(m_ozAuthors);
			b.append("\n");
		}
		if (m_ozPurpose != null) {
			b.append("Purpose: ");
			b.append(m_ozPurpose);
			b.append("\n");
		}
		b.append("Stack=======\n");
		final int depth = m_zlEsCodeStateReports.size();
		for (int i = 0; i < depth; i++) {
			final int d = i + 1;
			b.append("Stack At Depth " + d + " of " + depth + " -----\n");
			final EsCodeStateReport codeStateReport = m_zlEsCodeStateReports.get(i);
			b.append(codeStateReport);
			b.append("----End of Stack At Depth  " + d + "\n");
		}
		return b.toString();
	}

	@Override
	public String toString() {
		return getMessage();
	}

	public void unwind(EsSource source, int lineIndex, EsExecutionContext ecx) {
		if (source == null) throw new IllegalArgumentException("object is null");
		if (ecx == null) throw new IllegalArgumentException("object is null");
		if (m_ozAuthors == null) {
			m_ozAuthors = source.meta().qAuthors;
		}
		if (m_ozPurpose == null) {
			m_ozPurpose = source.meta().qPurpose;
		}
		final String zVariables = ecx.showStackVariables(1);
		final EsCodeStateReport codeStateReport = new EsCodeStateReport(source, lineIndex, zVariables);
		m_zlEsCodeStateReports.add(codeStateReport);
	}

	protected EsRunException(String cause) {
		super(STDMESSAGE);
		m_causeMessage = cause == null || cause.length() == 0 ? "Unspecified" : cause;
		m_oCauseThrowable = null;
	}

	protected EsRunException(Throwable cause) {
		super(STDMESSAGE);
		m_causeMessage = Ds.format(cause, false);
		m_oCauseThrowable = cause;
	}

	private final String m_causeMessage;
	private final Throwable m_oCauseThrowable;
	private final List<EsCodeStateReport> m_zlEsCodeStateReports = new ArrayList<EsCodeStateReport>(16);
	private String m_ozAuthors;
	private String m_ozPurpose;
}
