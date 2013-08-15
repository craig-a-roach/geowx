/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.PrintWriter;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
abstract class ShellPageLanguage extends ShellPageLeaf {

	private static final int BcQuota_Script = 128 * CArgon.K;

	private void addKeyBinding(StringBuilder sb, String id, BerylliumPath path) {
		final String oqFunction = oqKeyFunction(path);
		if (oqFunction != null) {
			if (sb.length() > 0) {
				sb.append(",\n");
			}
			sb.append("\"");
			sb.append(id);
			sb.append("\":");
			sb.append(oqFunction);
		}
	}

	private String newExtraKeys() {
		final StringBuilder sb = new StringBuilder();
		addKeyBinding(sb, "F11", CNeonShell.Asset_cm_ext_extraKeys_F11);
		addKeyBinding(sb, "Esc", CNeonShell.Asset_cm_ext_extraKeys_Esc);
		return sb.toString();
	}

	private String oqKeyFunction(BerylliumPath path) {
		final Class<?> ref = getClass();
		final Binary oSource = Binary.createFromClassPath(ref, path.qtwPath(), BcQuota_Script);
		return (oSource == null) ? null : oSource.newStringUTF8();
	}

	protected final void helpTable(PrintWriter writer) {
		tableBodyStart(writer, "Keyboard Help:");
		row(writer, true, "F11", "Full screen");
		row(writer, true, "Esc", "Exit full screen");
		row(writer, true, "Ctrl-F", "Find");
		row(writer, true, "Ctrl-G", "Find Next");
		row(writer, true, "Shift-Ctrl-F", "Replace");
		row(writer, true, "Shift-Ctrl-R", "Replace All");
		tableBodyEnd(writer);
	}

	protected final ShellLanguage language() {
		final String qccSourceName = qccSourceName();
		return ShellLanguage.newInstance(qccSourceName);
	}

	protected final void styleEditArea(PrintWriter writer, String tid, boolean readOnly, boolean lineNumbers) {
		final ShellLanguage language = language();
		final StringBuilder bcfg = new StringBuilder();
		final String sep = ",\n";
		ArgonText.append(bcfg, sep, "theme:\"" + CNeonShell.CmTheme + "\"");

		if (readOnly) {
			ArgonText.append(bcfg, sep, "readOnly: true");
		}
		if (lineNumbers) {
			ArgonText.append(bcfg, sep, "lineNumbers: true");
		}
		switch (language) {
			case EcmaScript: {
				ArgonText.append(bcfg, sep, "matchBrackets: true");
			}
			break;
			case Text: {
				ArgonText.append(bcfg, sep, "useCPP: false");
			}
			break;
			default:
		}
		final String zExtraKeys = newExtraKeys();
		if (zExtraKeys.length() > 0) {
			ArgonText.append(bcfg, sep, "extraKeys:  {\n" + zExtraKeys + "\n}");
		}
		final String cfg = "{" + bcfg.toString() + "}";
		javascriptSource(writer, "var editor = CodeMirror.fromTextArea(document.getElementById(\"" + tid + "\"), " + cfg + ");");
	}

	@Override
	public void scripts(PrintWriter writer) {
		javascriptLoad(writer, CNeonShell.JS_cm_core);
		javascriptLoad(writer, CNeonShell.JS_cm_lib_dialog);
		javascriptLoad(writer, CNeonShell.JS_cm_lib_search);
		javascriptLoad(writer, CNeonShell.JS_cm_lib_searchcursor);
		final ShellLanguage language = language();
		switch (language) {
			case EcmaScript: {
				javascriptLoad(writer, CNeonShell.JS_cm_mode_js);
			}
			break;
			case Xml: {
				javascriptLoad(writer, CNeonShell.JS_cm_mode_xml);
			}
			break;
			case Python: {
				javascriptLoad(writer, CNeonShell.JS_cm_mode_py);
			}
			break;
			case Shell: {
				javascriptLoad(writer, CNeonShell.JS_cm_mode_sh);
			}
			break;
			default:
		}
	}

	@Override
	public void styles(PrintWriter writer) {
		cssLink(writer, CNeonShell.CSS_cm);
		cssLink(writer, CNeonShell.CSS_cm_theme);
		cssLink(writer, CNeonShell.CSS_cm_ext_neon);
		cssLink(writer, CNeonShell.CSS_cm_lib_dialog);
	}

	public ShellPageLanguage(BerylliumPath path) {
		super(path);
	}
}
