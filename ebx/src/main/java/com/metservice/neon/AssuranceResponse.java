/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author roach
 */
class AssuranceResponse extends EsResponse {

	@Override
	protected void saveGlobals(EsRequest request, EsGlobal global, EsExecutionContext ecx)
			throws InterruptedException {
	}

	@Override
	protected void saveReturn(EsRequest request, EsExecutionContext ecx, IEsOperand callResult)
			throws InterruptedException {
		m_refReturn.set(zReturn(ecx, callResult));
	}

	@Override
	protected void saveThrow(EsRequest request, EsExecutionContext ecx, EsCompletionThrow completion)
			throws InterruptedException {
		m_refThrow.set(ztwThrow(ecx, completion));
	}

	public AssuranceRunReport newRunReport() {
		final String ozThrow = m_refThrow.get();
		if (ozThrow != null) return AssuranceRunReport.newThrow(ozThrow);
		final String ozReturn = m_refReturn.get();
		return AssuranceRunReport.newOk(ozReturn);
	}

	public AssuranceResponse() {
	}
	private final AtomicReference<String> m_refThrow = new AtomicReference<String>();
	private final AtomicReference<String> m_refReturn = new AtomicReference<String>();
}
