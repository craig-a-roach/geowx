/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.wdt;

import java.io.File;

/**
 * @author roach
 */
class Result {

	public Result(File oGribFile, long msExec) {
		m_oGribFile = oGribFile;
		m_oDiagnostic = null;
		m_msExec = msExec;
	}

	public Result(String diagnostic) {
		m_oGribFile = null;
		m_oDiagnostic = diagnostic;
		m_msExec = 0L;
	}
	private final File m_oGribFile;
	private final String m_oDiagnostic;
	private final long m_msExec;
}
