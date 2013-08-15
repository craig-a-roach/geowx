/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
class CNeonShell extends CNeon {

	static final int GracefulShutdownMs = 3 * SEC_TO_MS;

	static final int ReapingIntervalMs = 60 * SEC_TO_MS;
	static final int MinThreads = 4;
	static final int MaxThreads = 16;
	static final int MaxSessionMessages = 10;

	static final String Node_ui = "ui";

	static final String Node_asset = "asset";
	static final String Node_index = "index";
	static final String Node_control = "control";
	static final String Node_source_js = "sourcejs";
	static final String Node_source_txt = "sourcetxt";
	static final String Node_edit_js = "editjs";
	static final String Node_edit_txt = "edittxt";
	static final String Node_console = "console";
	static final String Node_debug = "debug";
	static final String Node_profile = "profile";
	static final String Node_manage = "manage";
	static final String Node_assure_nav = "assurenav";
	static final String Node_assure_run_js = "assurerunjs";
	static final String Node_assure_run_tree = "assureruntree";
	static final String Node_shutdown = "shutdown";

	static final String Redirect = BerylliumPath.qAbsolutePath(Node_ui, Node_index);
	static final BerylliumPath Shutdown = BerylliumPath.newAbsolute(Node_ui, Node_shutdown);
	static final BerylliumPath Favicon = BerylliumPath.newAbsolute(Node_asset, "favicon.ico");
	static final String CSS_ui = BerylliumPath.qAbsolutePath(Node_asset, "ui.css");

	static final String CmTheme = "lesser-dark";

	static final BerylliumPath Base_cm = BerylliumPath.newAbsolute(Node_asset, "cm");
	static final BerylliumPath Base_cm_lib = Base_cm.newPath("lib", "util");
	static final BerylliumPath Base_cm_mode = Base_cm.newPath("mode");

	static final BerylliumPath Asset_cm_ext = BerylliumPath.newRelative(Node_asset, "cm", "ext");
	static final BerylliumPath Asset_cm_ext_extraKeys_F11 = Asset_cm_ext.newPath("extraKeys_F11.js");
	static final BerylliumPath Asset_cm_ext_extraKeys_Esc = Asset_cm_ext.newPath("extraKeys_Esc.js");

	static final BerylliumPath CSS_cm_theme = Base_cm.newPath("theme", CmTheme + ".css");

	static final BerylliumPath Base_cm_ext = Base_cm.newPath("ext");
	static final BerylliumPath CSS_cm_ext_neon = Base_cm_ext.newPath("neon.css");

	static final BerylliumPath CSS_cm = Base_cm.newPath("codemirror.css");
	static final BerylliumPath CSS_cm_lib_dialog = Base_cm_lib.newPath("dialog.css");

	static final BerylliumPath JS_cm_core = Base_cm.newPath("codemirror.js");
	static final BerylliumPath JS_cm_lib_dialog = Base_cm_lib.newPath("dialog.js");
	static final BerylliumPath JS_cm_lib_search = Base_cm_lib.newPath("search.js");
	static final BerylliumPath JS_cm_lib_searchcursor = Base_cm_lib.newPath("searchcursor.js");

	static final BerylliumPath JS_cm_mode_js = Base_cm_mode.newPath("javascript.js");
	static final BerylliumPath JS_cm_mode_py = Base_cm_mode.newPath("python.js");
	static final BerylliumPath JS_cm_mode_xml = Base_cm_mode.newPath("xmlpure.js");
	static final BerylliumPath JS_cm_mode_sh = Base_cm_mode.newPath("shell.js");

	static final String HelpURL = "http://wiki.met.co.nz/display/ARCH/AMPS+System+Review";

	private CNeonShell() {
	}
}
