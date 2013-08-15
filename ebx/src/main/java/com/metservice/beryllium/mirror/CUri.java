/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
class CUri {

	public static final BerylliumPath Discover = BerylliumPath.newAbsolute("discover");
	public static final BerylliumPath Demand = BerylliumPath.newAbsolute("demand");
	public static final BerylliumPath Save = BerylliumPath.newAbsolute("save");
	public static final BerylliumPath Commit = BerylliumPath.newAbsolute("commit");

	private CUri() {
	}
}
