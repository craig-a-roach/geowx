/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.io.File;

import com.metservice.argon.ArgonText;
import com.metservice.boron.BoronApiException;
import com.metservice.boron.BoronExitCode;
import com.metservice.boron.BoronProcessId;
import com.metservice.boron.BoronSpace;
import com.metservice.neon.EmViewObject;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsPrimitiveNumberInteger;

/**
 * @author roach
 */
class ProcessCompletionEm extends EmViewObject {

	private void diagnostic(StringBuilder sb, boolean includeStdOut) {
		if (m_oqIncomplete != null) {
			ArgonText.append(sb, ", ", "Incomplete: " + m_oqIncomplete);
		}
		final String qExitCode = m_oExitCode == null ? "none" : m_oExitCode.toString();
		ArgonText.append(sb, ", ", "ExitCode: " + qExitCode);
		if (m_cancelled) {
			ArgonText.append(sb, ", ", "Cancelled");
		}
		if (m_restartCount > 0) {
			ArgonText.append(sb, "\n", "Restarts: " + m_restartCount);
		}
		if (m_ozStdErrReport != null) {
			sb.append("\nStdErr>\n");
			sb.append(m_ozStdErrReport);
			sb.append("\n<StdErr");
		}
		if (includeStdOut && m_ozStdOutReport != null) {
			sb.append("\nStdOut>\n");
			sb.append(m_ozStdOutReport);
			sb.append("\n<StdOut");
		}
	}

	public File cndirProcess()
			throws BoronApiException {
		return m_space.cndirProcess(m_processId);
	}

	public String diagnostic() {
		final StringBuilder sb = new StringBuilder();
		diagnostic(sb, false);
		sb.append("\nBoron ProcessId: ");
		sb.append(m_processId.qId());
		return sb.toString();
	}

	@Override
	public void putProperties(EsExecutionContext ecx)
			throws InterruptedException {
		if (m_oqIncomplete != null) {
			putView(CProp.incomplete, m_oqIncomplete);
		}
		if (m_oExitCode != null) {
			putView(CProp.exitCode, EsPrimitiveNumberInteger.newInstance(m_oExitCode.value()));
		}
		putViewInteger(CProp.restartCount, m_restartCount);
		putViewBoolean(CProp.cancelled, m_cancelled);
		if (m_ozStdErrReport != null) {
			putView(CProp.stdErrReport, m_ozStdErrReport);
		}
		if (m_ozStdOutReport != null) {
			putView(CProp.stdOutReport, m_ozStdOutReport);
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Boron ProcessId: ");
		sb.append(m_processId.qId());
		diagnostic(sb, true);
		sb.append("\nProcess Directory: ");
		try {
			sb.append(cndirProcess());
		} catch (final BoronApiException ex) {
			sb.append(ex.getMessage());
		}
		return sb.toString();
	}

	public ProcessCompletionEm(BoronSpace space, BoronProcessId processId, String oqIncomplete, BoronExitCode oExitCode,
			boolean cancelled, String ozStdErrReport, String ozStdOutReport, int restartCount) {
		super(ProcessCompletionEmClass.Instance);
		if (space == null) throw new IllegalArgumentException("object is null");
		if (processId == null) throw new IllegalArgumentException("object is null");
		m_space = space;
		m_processId = processId;
		m_oqIncomplete = oqIncomplete;
		m_oExitCode = oExitCode;
		m_cancelled = cancelled;
		m_ozStdErrReport = ozStdErrReport;
		m_ozStdOutReport = ozStdOutReport;
		m_restartCount = restartCount;
	}

	private final BoronSpace m_space;
	private final BoronProcessId m_processId;
	private final String m_oqIncomplete;
	private final BoronExitCode m_oExitCode;
	private final boolean m_cancelled;
	private final String m_ozStdErrReport;
	private final String m_ozStdOutReport;
	private final int m_restartCount;
}
