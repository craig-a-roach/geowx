/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import com.metservice.neon.EmClass;
import com.metservice.neon.EmMutableObject;
import com.metservice.neon.EsExecutionContext;
import com.metservice.neon.EsObject;

/**
 * @author roach
 */
public class HttpErrorEm extends EmMutableObject<HttpError, HttpError> {

	@Override
	protected HttpError newTuple(EsExecutionContext ecx, HttpError construct)
			throws InterruptedException {
		return construct;
	}

	@Override
	protected void putDefaultProperties(EsExecutionContext ecx) {
	}

	@Override
	protected void putInstanceProperties(EsExecutionContext ecx, HttpError construct) {
		putViewInteger(CProp.statusCode, construct.statusCode);
	}

	public void constructError(HttpError he) {
		setConstruct(he);
	}

	@Override
	public EsObject createObject() {
		return new HttpErrorEm(this);
	}

	public HttpErrorEm(EmClass emClass) {
		super(emClass);
	}

	public HttpErrorEm(HttpErrorEm prototype) {
		super(prototype);
	}
}
