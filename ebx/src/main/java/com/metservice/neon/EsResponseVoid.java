/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author roach
 */
public class EsResponseVoid extends EsResponse {

	@Override
	protected void saveGlobals(EsRequest request, EsGlobal global, EsExecutionContext ecx)
			throws InterruptedException {
	}

	@Override
	protected void saveReturn(EsRequest request, EsExecutionContext ecx, IEsOperand callResult)
			throws InterruptedException {
	}

	@Override
	protected void saveThrow(EsRequest request, EsExecutionContext ecx, EsCompletionThrow completion)
			throws InterruptedException {
		m_refThrow.set(ztwThrow(ecx, completion));
	}

	public String getThrowMessage() {
		return m_refThrow.get();
	}

	public EsResponseVoid() {
	}
	private final AtomicReference<String> m_refThrow = new AtomicReference<String>();
}
