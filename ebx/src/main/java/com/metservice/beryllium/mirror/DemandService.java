/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.Binary;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.beryllium.BerylliumBinaryHttpPayload;
import com.metservice.beryllium.BerylliumHttpBadRequestException;
import com.metservice.beryllium.BerylliumHttpNotFoundException;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.CBeryllium;
import com.metservice.beryllium.IBerylliumDownloadHttpService;

/**
 * @author roach
 */
class DemandService implements IBerylliumDownloadHttpService {

	@Override
	public BerylliumBinaryHttpPayload newPayload(BerylliumPath path, Request rq, JsonObject jrq)
			throws BerylliumHttpNotFoundException, BerylliumHttpBadRequestException, JsonSchemaException,
			InterruptedException {
		final String qcctwPath = jrq.accessor(CProp.path).datumQtwString();
		final BerylliumBinaryHttpPayload oPayload = m_provider.createPayload(qcctwPath);
		if (oPayload != null) return oPayload;
		return new BerylliumBinaryHttpPayload(CBeryllium.BinaryContentType, Binary.Empty, ArgonClock.tsNow());
	}

	public DemandService(IBerylliumMirrorProvider provider) {
		if (provider == null) throw new IllegalArgumentException("object is null");
		m_provider = provider;
	}
	private final IBerylliumMirrorProvider m_provider;
}
