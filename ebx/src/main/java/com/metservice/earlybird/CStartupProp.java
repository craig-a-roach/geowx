/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

/**
 * @author roach
 */
class CStartupProp {

	static final String KmlPort = "kmlPort";
	static final String GridPath = "gridPath";
	static final String StationUrl = "stationUrl";
	static final String ServiceConnectorType = "serviceConnectorType";
	static final String KmlServiceMinThreads = "kmlServiceMinThreads";
	static final String KmlServiceMaxThreads = "kmlServiceMaxThreads";
	static final String FilterPatternConsole = "consoleFilterPattern";
	static final String FilterPatternJmx = "jmxFilterPattern";
	static final String FilterPatternLog = "logFilterPattern";

	private CStartupProp() {
	}
}
