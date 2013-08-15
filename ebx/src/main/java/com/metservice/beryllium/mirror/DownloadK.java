/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import java.util.concurrent.atomic.AtomicReference;

import com.metservice.argon.Elapsed;
import com.metservice.beryllium.BerylliumAbstractDownloadHttpTracker;
import com.metservice.beryllium.BerylliumBinaryHttpPayload;

/**
 * @author roach
 */
abstract class DownloadK extends BerylliumAbstractDownloadHttpTracker {

	private void fail(String reason) {
		final String t = trackerType();
		final String m = "Invalid response from " + t + " peer..." + reason;
		m_probe.warnMirror(m);
	}

	protected abstract String trackerType();

	public BerylliumBinaryHttpPayload getResponse() {
		return m_response.get();
	}

	@Override
	public void onCompleteMalformedResponse()
			throws InterruptedException {
		fail("Malformed response");
	}

	@Override
	public void onCompleteResponse(BerylliumBinaryHttpPayload payload)
			throws InterruptedException {
		m_response.set(payload);
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

	protected DownloadK(IBerylliumMirrorProbe probe) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		m_probe = probe;
		m_response = new AtomicReference<BerylliumBinaryHttpPayload>();
	}
	private final IBerylliumMirrorProbe m_probe;
	private final AtomicReference<BerylliumBinaryHttpPayload> m_response;
}
