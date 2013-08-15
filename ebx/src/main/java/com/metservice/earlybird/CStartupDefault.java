/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import com.metservice.beryllium.BerylliumHttpConnectorType;

/**
 * @author roach
 */
class CStartupDefault {

	static final int KmlPort = 8080;
	static final int KmlMinThreads = 4;
	static final int KmlMaxThreads = 16;
	static final BerylliumHttpConnectorType HttpConnectorType = BerylliumHttpConnectorType.PLATFORM;

	private CStartupDefault() {
	}
}
