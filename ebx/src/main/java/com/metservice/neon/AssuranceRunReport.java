/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
class AssuranceRunReport {

	private static final String StateSyntax = "BAD SYNTAX";
	private static final String StateThrow = "EXCEPTION THROWN";
	private static final String StateRuntime = "RUNTIME EXCEPTION";
	private static final String StateAssertPass = "ASSERTIONS PASSED";
	private static final String StateAssertFail = "ASSERTION FAILED";

	private static final String FlagPass = "\u2714";
	private static final String FlagFail = "\u2717";

	public static AssuranceRunReport newAssert(String ozReport) {
		return new AssuranceRunReport(false, FlagFail, StateAssertFail, ozReport);
	}

	public static AssuranceRunReport newOk(String ozReport) {
		return new AssuranceRunReport(true, FlagPass, StateAssertPass, ozReport);
	}

	public static AssuranceRunReport newRuntime(String ozReport) {
		return new AssuranceRunReport(false, FlagFail, StateRuntime, ozReport);
	}

	public static AssuranceRunReport newSyntax(String ozReport) {
		return new AssuranceRunReport(false, FlagFail, StateSyntax, ozReport);
	}

	public static AssuranceRunReport newThrow(String ozReport) {
		return new AssuranceRunReport(false, FlagFail, StateThrow, ozReport);
	}

	public boolean isPass() {
		return m_pass;
	}

	public String qFlag() {
		return m_qFlag;
	}

	public String qState() {
		return m_qState;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_qState);
		if (m_ztwReport.length() > 0) {
			sb.append(": ");
			sb.append(m_ztwReport);
		}
		return sb.toString();
	}

	public String ztwReport() {
		return m_ztwReport;
	}

	private AssuranceRunReport(boolean pass, String qFlag, String qState, String ozReport) {
		assert qFlag != null && qFlag.length() > 0;
		assert qState != null && qState.length() > 0;
		m_pass = pass;
		m_qFlag = qFlag;
		m_qState = qState;
		m_ztwReport = ArgonText.ztw(ozReport);
	}
	private final boolean m_pass;
	private final String m_qFlag;
	private final String m_qState;
	private final String m_ztwReport;
}
