/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
class ShellPageEditTxt extends ShellPageLanguage {

	private static final String Ftxtcontent = "txtcontent";

	private void saveContent(ShellSession sn, ShellGlobal g, String zTxt, PrintWriter writer) {
		bulletsStart(writer);
		final String ztw = zTxt.trim();
		final String qccSourcePath = qccSourcePath();
		boolean saved = false;
		try {
			g.kc.sourceProvider.putSource(qccSourcePath, ztw);
			saved = true;
		} catch (final EsSourceSaveException ex) {
			itemAttention(writer, "Source code for " + qccSourcePath + " could not be saved");
			itemAttention(writer, ex);
		}
		if (saved) {
			item(writer, "Saved " + qccSourcePath);
		}
		bulletsEnd(writer);
	}

	@Override
	public String qTitle() {
		return "Edit /" + qccSourcePath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException {
		final PrintWriter writer = rp.getWriter();
		final String qccSourcePath = qccSourcePath();
		menubar(writer, indexPath(), "Index", m_path, "Restore", managePath(), "Manage", helpPath(), "Help");
		final String ozTxt = rq.getParameter(Ftxtcontent);
		if (ozTxt != null) {
			saveContent(sn, g, ozTxt, writer);
		}

		final String ztwRootSourceText;
		try {
			ztwRootSourceText = g.kc.sourceProvider.source(qccSourcePath);
		} catch (final EsSourceLoadException ex) {
			paraAttention(writer, "Source code for " + qccSourcePath + " is not available");
			return;
		}
		formStart(writer, m_path, true, false);
		fieldsetStart(writer, "Source");
		inputTextArea(writer, "txta", Ftxtcontent, -1, -1, ztwRootSourceText);
		br(writer);
		buttonSubmit(writer, "Save");
		fieldsetEnd(writer);
		formEnd(writer);
		styleEditArea(writer, "txta", false, true);
		helpTable(writer);
	}

	public ShellPageEditTxt(BerylliumPath path) {
		super(path);
	}
}
