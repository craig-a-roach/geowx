/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
abstract class ShellPageLeaf extends ShellPage {

	public final BerylliumPath assuranceNavPath() {
		return m_path.subPathHead(-2).newPath(CNeonShell.Node_assure_nav);
	}

	public final BerylliumPath assuranceRunJsPath() {
		return m_navBasePath.newPath(CNeonShell.Node_assure_run_js);
	}

	public final BerylliumPath consolePath() {
		return m_navBasePath.newPath(CNeonShell.Node_console);
	}

	public final BerylliumPath controlPath() {
		return m_navBasePath.newPath(CNeonShell.Node_control);
	}

	public final BerylliumPath debugPath() {
		return m_navBasePath.newPath(CNeonShell.Node_debug);
	}

	public final BerylliumPath editJsPath() {
		return m_navBasePath.newPath(CNeonShell.Node_edit_js);
	}

	public final BerylliumPath editTxtPath() {
		return m_navBasePath.newPath(CNeonShell.Node_edit_txt);
	}

	public final BerylliumPath indexPath() {
		return m_path.subPathHead(-2).newPath(CNeonShell.Node_index);
	}

	public final BerylliumPath managePath() {
		return m_path.subPathHead(-2).newPath(CNeonShell.Node_manage);
	}

	public final BerylliumPath profilePath() {
		return m_navBasePath.newPath(CNeonShell.Node_profile);
	}

	public final String qccSourceName() {
		return m_sourcePath.qtwNode(-1);
	}

	public final String qccSourcePath() {
		return m_sourcePath.toString();
	}

	public final BerylliumPath sourceJsPath() {
		return m_navBasePath.newPath(CNeonShell.Node_source_js);
	}

	public final BerylliumPath sourceTxtPath() {
		return m_navBasePath.newPath(CNeonShell.Node_source_txt);
	}

	public ShellPageLeaf(BerylliumPath path) {
		if (path == null) throw new IllegalArgumentException("object is null");
		m_path = path;
		m_navBasePath = path.subPath(0, -1);
		m_sourcePath = path.subPath(1, -1);
	}

	protected final BerylliumPath m_path;
	private final BerylliumPath m_navBasePath;
	private final BerylliumPath m_sourcePath;
}
