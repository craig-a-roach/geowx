/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.Callable;

/**
 * @author roach
 */
class AssuranceRun implements Callable<AssuranceRunReport> {

	@Override
	public AssuranceRunReport call()
			throws Exception {
		try {
			final AssuranceResponse response = new AssuranceResponse();
			m_space.run(m_rq, response);
			return response.newRunReport();
		} catch (final NeonScriptCompileException ex) {
			return AssuranceRunReport.newSyntax(ex.getMessage());
		} catch (final NeonScriptAssertException ex) {
			return AssuranceRunReport.newAssert(ex.getMessage());
		} catch (final NeonScriptRunException ex) {
			return AssuranceRunReport.newRuntime(ex.getMessage());
		} catch (final NeonScriptException ex) {
			return AssuranceRunReport.newRuntime(ex.getMessage());
		}
	}

	public AssuranceRun(NeonSpace space, EsRequest rq) {
		assert space != null;
		assert rq != null;
		m_space = space;
		m_rq = rq;
	}
	private final NeonSpace m_space;
	private final EsRequest m_rq;
}
