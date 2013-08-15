/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import java.util.List;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.beryllium.BerylliumHttpBadRequestException;
import com.metservice.beryllium.BerylliumHttpNotFoundException;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.IBerylliumJsonHttpService;

/**
 * @author roach
 */
class DiscoverService implements IBerylliumJsonHttpService {

	@Override
	public void handle(BerylliumPath path, Request rq, JsonObject jrq, JsonObject jrp)
			throws BerylliumHttpNotFoundException, BerylliumHttpBadRequestException, JsonSchemaException,
			InterruptedException {
		final DiscoverQ q = new DiscoverQ(jrq);
		final List<String> zlDemandPathsAsc = m_provider.discoverDemandPathsAsc(q.qcctwHiex());
		final List<String> zlCommitPathsAsc = m_provider.commitPathsAsc(q.zlWipPathsAsc());
		final DiscoverR r = new DiscoverR(zlDemandPathsAsc, zlCommitPathsAsc);
		r.saveTo(jrp);
	}

	public DiscoverService(IBerylliumMirrorProvider provider) {
		if (provider == null) throw new IllegalArgumentException("object is null");
		m_provider = provider;
	}
	private final IBerylliumMirrorProvider m_provider;
}
