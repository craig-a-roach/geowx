/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.metservice.argon.ElapsedFactory;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
class TestImpJsonTracker extends BerylliumAbstractJsonHttpTracker {

	public boolean await(int secs)
			throws InterruptedException {
		return tryOutcome(ElapsedFactory.newElapsed(secs, TimeUnit.SECONDS));
	}

	@Override
	public void onCompleteResponse(JsonObject jsonObject)
			throws JsonSchemaException, InterruptedException {
		m_completeJsonResponse.set(jsonObject);
	}

	public String qResponse() {
		final JsonObject oResponse = m_completeJsonResponse.get();
		return oResponse == null ? "???" : oResponse.toString();
	}

	public TestImpJsonTracker(TestImpNetProbe probe, TestImpHostPort hp, BerylliumPath uri, String qid) {
		this.hostPort = hp;
		this.uri = uri;
		this.qid = qid;
	}
	public final TestImpHostPort hostPort;
	public final BerylliumPath uri;
	public final String qid;
	private final AtomicReference<JsonObject> m_completeJsonResponse = new AtomicReference<JsonObject>();
}
