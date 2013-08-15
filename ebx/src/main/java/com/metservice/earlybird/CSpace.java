/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.Elapsed;
import com.metservice.argon.ElapsedFactory;

/**
 * @author roach
 */
class CSpace {

	static final String Vendor = "com.metservice";

	static final ArgonServiceId ServiceId = new ArgonServiceId(Vendor, "earlybird");

	static final String ThreadPrefix = "earlybird--";

	static final Elapsed HttpGracefulShutdown = ElapsedFactory.newElapsedConstant("3s");
	static final Elapsed ExecutorShutdownAwait = ElapsedFactory.newElapsedConstant("10s");

	private CSpace() {
	}
}
