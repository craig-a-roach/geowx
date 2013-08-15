/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import javax.servlet.http.HttpServletResponse;

import com.metservice.neon.EmClass;
import com.metservice.neon.EmMutableObject;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsObject;

/**
 * @author roach
 */
public class HttpResponseEm extends EmMutableObject<HttpResponseConstruct, HttpResponseTuple> {

	@Override
	protected HttpResponseTuple newTuple(EsExecutionContext ecx, HttpResponseConstruct construct)
			throws InterruptedException {
		return new HttpResponseTuple(ecx, construct, this);
	}

	@Override
	protected void putDefaultProperties(EsExecutionContext ecx) {
	}

	@Override
	protected void putInstanceProperties(EsExecutionContext ecx, HttpResponseConstruct c) {
		if (c.oStatusCode != null) {
			putUpdateInteger(CProp.statusCode, c.oStatusCode.intValue());
		}
		if (c.oMimeContent != null) {
			putUpdate(CProp.contentType, c.oMimeContent.qtwContentType);
			putUpdate(ecx, CProp.content, c.oMimeContent.content);
		}
	}

	public void constructContent(MimeContent mc) {
		setConstruct(new HttpResponseConstruct(HttpServletResponse.SC_OK, mc));
	}

	public void constructEmpty() {
		setConstruct(new HttpResponseConstruct(null, null));
	}

	public void constructStatus(int statusCode) {
		setConstruct(new HttpResponseConstruct(Integer.valueOf(statusCode), null));
	}

	@Override
	public EsObject createObject() {
		return new HttpResponseEm(this);
	}

	public HttpResponseEm(EmClass emClass) {
		super(emClass);
	}

	public HttpResponseEm(HttpResponseEm prototype) {
		super(prototype);
	}
}
