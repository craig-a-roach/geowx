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
public abstract class BerylliumAbstractUploadHttpTracker extends BerylliumAbstractHttpTracker implements IBerylliumUploadHttpTracker {

	protected abstract void onCompleteResponse(BerylliumBinaryHttpPayload oPayload)
			throws InterruptedException;

	@Override
	public void raiseCompleteResponse(BerylliumBinaryHttpPayload oPayload)
			throws InterruptedException {
		if (!isCancelled()) {
			onCompleteResponse(oPayload);
			haveOutcome();
		}
	}

	protected BerylliumAbstractUploadHttpTracker() {
	}

	protected BerylliumAbstractUploadHttpTracker(Elapsed awaitInterval) {
		super(awaitInterval);
	}
}
