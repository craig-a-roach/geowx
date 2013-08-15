/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import org.eclipse.jetty.server.Request;

import com.metservice.neon.EmAbstractInstaller;
import com.metservice.neon.EsExecutionContext;

/**
 * @author roach
 */
public class HttpEmInstaller extends EmAbstractInstaller {

	@Override
	public void install(EsExecutionContext ecx)
			throws InterruptedException {
		if (m_oemHttpRequest != null) {
			putView(ecx, CProp.GlobalHttpRequest, m_oemHttpRequest);
		}
		putClass(ecx, HttpErrorEmClass.Constructor);
		putClass(ecx, HttpResponseEmClass.Constructor);
	}

	public HttpEmInstaller() {
		m_oemHttpRequest = null;
	}

	public HttpEmInstaller(Request rq, int bcQuota) {
		if (rq == null) throw new IllegalArgumentException("object is null");
		m_oemHttpRequest = new HttpRequestEm(rq, bcQuota);
	}

	private final HttpRequestEm m_oemHttpRequest;
}
