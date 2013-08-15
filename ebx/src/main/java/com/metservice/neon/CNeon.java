/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.CArgon;
import com.metservice.beryllium.BerylliumHttpConnectorType;

/**
 * @author roach
 */
abstract class CNeon implements CArgon {

	static final String Vendor = "com.metservice";

	static final ArgonServiceId ServiceId = new ArgonServiceId(Vendor, "neon");

	static final int QuotaClasspathSourceBc = 1 * M;
	static final int QuotaBinaryBc = 64 * M;
	static final int CallableCacheInitialCapacity = 1024;

	static final int DefaultCallableCacheLineBudget = 64 * K;
	static final int DefaultQuotaFileSourceBc = 4 * M;

	static final BerylliumHttpConnectorType DefaultShellSessionConnectorType = BerylliumHttpConnectorType.PLATFORM;
	static final int DefaultShellSessionMaxIdleSecs = 30 * MIN_TO_SEC;
	static final int DefaultShellConsoleQuota = 1000;
	static final boolean DefaultShellProcess = false;

	static final String ThreadPrefix = "neon-";

	protected CNeon() {
	}
}
