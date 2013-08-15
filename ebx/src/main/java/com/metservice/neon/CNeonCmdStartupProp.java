/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

/**
 * @author roach
 */
class CNeonCmdStartupProp {

	static final String ShellPort = "shellPort";
	static final String SourcePath = "sourcePath";
	static final String ShellSessionMaxIdle = "shellSessionMaxIdle";
	static final String ShellConsoleQuota = "shellConsoleQuota";
	static final String CallableCacheLineBudget = "callableCacheLineBudget";
	static final String AutoDebugPattern = "autoDebugPattern";
	static final String FilterPatternConsole = "consoleFilterPattern";
	static final String FilterPatternJmx = "jmxFilterPattern";
	static final String FilterPatternLog = "logFilterPattern";

	private CNeonCmdStartupProp() {
	}
}
