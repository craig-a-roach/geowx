/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public interface IBerylliumJsonHttpTracker extends IBerylliumHttpTracker {

	public void raiseCompleteResponse(JsonObject jsonObject)
			throws JsonSchemaException, InterruptedException;
}
