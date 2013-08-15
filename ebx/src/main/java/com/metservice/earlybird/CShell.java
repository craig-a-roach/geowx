/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
class CShell {

	static final int MinThreads = 4;
	static final int MaxThreads = 8;

	static final String TitlePrefix = "earlybird";

	static final String Node_ui = "ui";

	static final String Node_asset = "asset";
	static final String Node_index = "index";
	static final String Node_shutdown = "shutdown";

	static final String Redirect = BerylliumPath.qAbsolutePath(Node_ui, Node_index);
	static final BerylliumPath Shutdown = BerylliumPath.newAbsolute(Node_ui, Node_shutdown);
	static final BerylliumPath Favicon = BerylliumPath.newAbsolute(Node_asset, "favicon.ico");
	static final String CSS_ui = BerylliumPath.qAbsolutePath(Node_asset, "ui.css");
}
