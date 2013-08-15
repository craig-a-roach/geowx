/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import com.metservice.argon.Ds;
import com.metservice.neon.EsCompletionThrow;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsGlobal;
import com.metservice.neon.EsRequest;
import com.metservice.neon.EsResponse;
import com.metservice.neon.IEsOperand;
import com.metservice.neon.NeonImpException;
import com.metservice.neon.NeonScriptCompileException;
import com.metservice.neon.NeonScriptException;

/**
 * @author roach
 */
public class HttpNeonRouter extends EsResponse {

	private static final String CsqHttp = "Http error response code";

	@Override
	protected void saveGlobals(EsRequest request, EsGlobal global, EsExecutionContext ecx)
			throws InterruptedException {
	}

	@Override
	protected void saveReturn(EsRequest request, EsExecutionContext ecx, IEsOperand callResult)
			throws InterruptedException {
		m_refTuple.set(esObjectReturn(ecx, callResult, HttpResponseEm.class).newTuple(ecx));
	}

	@Override
	protected void saveThrow(EsRequest request, EsExecutionContext ecx, EsCompletionThrow completion)
			throws InterruptedException {
		m_refThrow.set(ztwThrow(ecx, completion));
	}

	public Ds send(HttpServletResponse rp)
			throws ServletException, IOException {

		final String ozThrow = m_refThrow.get();
		if (ozThrow == null) {
			returnValue(m_refTuple).send(rp);
			return null;
		}
		HttpError.newInstance(ozThrow).send(rp);
		return Ds.invalidBecause(ozThrow, CsqHttp);
	}

	public void sendError(HttpServletResponse rp, InterruptedException ex)
			throws IOException {
		HttpError.newInstance(ex).send(rp);
	}

	public Ds sendError(HttpServletResponse rp, NeonImpException ex)
			throws IOException {
		HttpError.newInstance(ex).send(rp);
		return Ds.triedTo("Run script", ex);
	}

	public Ds sendError(HttpServletResponse rp, NeonScriptException ex)
			throws IOException {
		HttpError.newInstance(ex).send(rp);
		if (ex instanceof NeonScriptCompileException) return Ds.triedTo("Compile script", ex, CsqHttp);

		return Ds.triedTo("Run script", ex, CsqHttp);
	}

	public Ds sendError(HttpServletResponse rp, RuntimeException ex)
			throws IOException {
		HttpError.newInstance(ex).send(rp);
		return Ds.triedTo("Run script", ex, CsqHttp);
	}

	public HttpNeonRouter() {
	}
	private final AtomicReference<String> m_refThrow = new AtomicReference<String>();
	private final AtomicReference<HttpResponseTuple> m_refTuple = new AtomicReference<HttpResponseTuple>();
}
