/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import java.util.concurrent.atomic.AtomicReference;

import com.metservice.argon.Elapsed;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;
import com.metservice.beryllium.BerylliumAbstractJsonHttpTracker;

/**
 * @author roach
 */
abstract class JsonK<R extends JsonR> extends BerylliumAbstractJsonHttpTracker {

	private void fail(String reason) {
		final String t = trackerType();
		final String m = "Invalid response from " + t + " peer..." + reason;
		m_probe.warnMirror(m);
	}

	protected abstract R newResponse(JsonObject src)
			throws JsonSchemaException;

	@Override
	protected void onCompleteResponse(JsonObject src)
			throws JsonSchemaException, InterruptedException {
		m_response.set(newResponse(src));
	}

	protected abstract String trackerType();

	public R getResponse() {
		return m_response.get();
	}

	@Override
	public void onCompleteMalformedResponse()
			throws InterruptedException {
		fail("Malformed response");
	}

	@Override
	public void onStatusUnexpected(int status)
			throws InterruptedException {
		fail("Unexpected HTTP status code" + status);
	}

	@Override
	public void onTooSlow(Elapsed expiryInterval) {
		fail("Gave up after " + expiryInterval);
	}

	@Override
	public void onUnresponsive()
			throws InterruptedException {
		fail("Not responding");
	}

	protected JsonK(IBerylliumMirrorProbe probe) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		m_probe = probe;
		m_response = new AtomicReference<R>();
	}
	private final IBerylliumMirrorProbe m_probe;
	private final AtomicReference<R> m_response;
}
