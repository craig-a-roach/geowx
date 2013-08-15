/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.Elapsed;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public abstract class BerylliumAbstractJsonHttpTracker extends BerylliumAbstractHttpTracker implements IBerylliumJsonHttpTracker {

	protected abstract void onCompleteResponse(JsonObject jsonObject)
			throws JsonSchemaException, InterruptedException;

	@Override
	public void raiseCompleteResponse(JsonObject jsonObject)
			throws JsonSchemaException, InterruptedException {
		if (!isCancelled()) {
			onCompleteResponse(jsonObject);
			haveOutcome();
		}
	}

	protected BerylliumAbstractJsonHttpTracker() {
	}

	protected BerylliumAbstractJsonHttpTracker(Elapsed awaitInterval) {
		super(awaitInterval);
	}
}
