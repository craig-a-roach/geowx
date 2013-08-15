/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public abstract class BerylliumAbstractDownloadHttpTracker extends BerylliumAbstractHttpTracker implements
		IBerylliumDownloadHttpTracker {

	protected abstract void onCompleteResponse(BerylliumBinaryHttpPayload payload)
			throws InterruptedException;

	@Override
	public void raiseCompleteResponse(BerylliumBinaryHttpPayload payload)
			throws InterruptedException {
		if (!isCancelled()) {
			onCompleteResponse(payload);
			haveOutcome();
		}
	}

	protected BerylliumAbstractDownloadHttpTracker() {
	}

	protected BerylliumAbstractDownloadHttpTracker(Elapsed awaitInterval) {
		super(awaitInterval);
	}
}
