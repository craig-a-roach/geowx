/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.beryllium.BerylliumBinaryHttpPayload;
import com.metservice.beryllium.BerylliumHttpBadRequestException;
import com.metservice.beryllium.BerylliumHttpNotFoundException;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.IBerylliumCommandHttpService;

/**
 * @author roach
 */
class CommitService implements IBerylliumCommandHttpService {

	@Override
	public BerylliumBinaryHttpPayload createPayload(BerylliumPath path, Request rq, JsonObject jrq)
			throws BerylliumHttpNotFoundException, BerylliumHttpBadRequestException, JsonSchemaException,
			InterruptedException {
		final String qcctwPath = jrq.accessor(CProp.path).datumQtwString();
		m_provider.commit(qcctwPath);
		return null;
	}

	public CommitService(IBerylliumMirrorProvider provider) {
		if (provider == null) throw new IllegalArgumentException("object is null");
		m_provider = provider;
	}
	private final IBerylliumMirrorProvider m_provider;
}
