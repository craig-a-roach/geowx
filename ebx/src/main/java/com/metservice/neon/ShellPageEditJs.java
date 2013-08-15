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

import com.metservice.argon.ArgonClock;
import com.metservice.argon.DateFormatter;
import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
class ShellPageEditJs extends ShellPageLanguage {

	private static final String Fjscontent = "jscontent";
	private static final String Fcompile = "compile";

	private void renderInvalid(ShellGlobal g, PrintWriter writer) {
		final String qccSourcePath = qccSourcePath();
		itemAttention(writer, "Source code for " + qccSourcePath + " contains errors");
		final EsSource source;
		try {
			source = g.sourceLoader.newSource(qccSourcePath);
		} catch (final EsSourceLoadException ex) {
			itemAttention(writer, ex.getMessage());
			return;
		}
		final EsSourceHtml sourceHtml;
		try {
			sourceHtml = source.newSourceHtml();
		} catch (final EsSyntaxException ex) {
			codeBlock(writer, ex.getMessage());
			codeBlock(writer, source.format(false));
			return;
		}
		try {
			source.newCallable();
		} catch (final EsSyntaxException ex) {
			sourceHtml.writeErrorView(writer, ex, 10);
		}
	}

	private void saveContent(ShellSession sn, ShellGlobal g, String zJs, boolean compile, PrintWriter writer) {
		bulletsStart(writer);
		final String ztwJs = zJs.trim();
		if (ztwJs.length() == 0) {
			itemAttention(writer, "Script content is just whitespace");
		} else {
			final String qccSourcePath = qccSourcePath();
			boolean saved = false;
			try {
				g.sourceLoader.save(qccSourcePath, ztwJs);
				saved = true;
			} catch (final EsSourceSaveException ex) {
				itemAttention(writer, "Source code for " + qccSourcePath + " could not be saved");
				itemAttention(writer, ex);
			}
			if (saved) {
				final String now = DateFormatter.newPlatformDHMSFromTs(ArgonClock.tsNow());
				item(writer, "Saved " + qccSourcePath + " at " + now);
				if (compile) {
					if (g.sourceLoader.validate(qccSourcePath)) {
						item(writer, "No Syntax Errors");
					} else {
						renderInvalid(g, writer);
					}
				}
			}
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
		menubar(writer, indexPath(), "Index", controlPath(), "Control", m_path, "Restore", assuranceRunJsPath(), "Test",
				managePath(), "Manage", helpPath(), "Help");
		final String ozJs = rq.getParameter(Fjscontent);
		final boolean compile = isChecked(Fcompile, rq);
		if (ozJs != null) {
			saveContent(sn, g, ozJs, compile, writer);
		}

		final String ztwRootSourceText;
		try {
			ztwRootSourceText = g.sourceLoader.ztwSourceText(qccSourcePath, false);
		} catch (final EsSourceLoadException ex) {
			paraAttention(writer, "Source code for " + qccSourcePath + " is not available");
			return;
		}
		formStart(writer, m_path, true, false);
		fieldsetStart(writer, "EcmaScript Source");
		inputTextArea(writer, "jsa", Fjscontent, -1, -1, ztwRootSourceText);
		br(writer);
		buttonSubmit(writer, "Save");
		text(writer, "Compile?");
		inputCheckbox(writer, Fcompile, true);
		fieldsetEnd(writer);
		formEnd(writer);
		styleEditArea(writer, "jsa", false, true);
		helpTable(writer);
	}

	public ShellPageEditJs(BerylliumPath path) {
		super(path);
	}
}
