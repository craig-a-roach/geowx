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
class ShellPageSourceJs extends ShellPageLeaf {

	private static final String ForiginalText = "originalText";
	private static final String FshowImports = "showImports";
	private static final String Fcompact = "compact";

	@Override
	public String qTitle() {
		return "Source /" + qccSourcePath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException {
		final PrintWriter writer = rp.getWriter();
		menubar(writer, indexPath(), "Index", controlPath(), "Control", editJsPath(), "Edit", helpPath(), "Help");

		final boolean originalText = isChecked(ForiginalText, rq, false);
		final boolean showImports = isChecked(FshowImports, rq, false);
		final boolean compact = isChecked(Fcompact, rq, false);

		final String qccSourcePath = qccSourcePath();

		if (originalText) {
			try {
				final String ztwText = g.sourceLoader.ztwSourceText(qccSourcePath, showImports);
				codeBlock(writer, ztwText);
			} catch (final EsSourceLoadException ex) {
				paraAttention(writer, "Source code for " + qccSourcePath + " is not available");
				return;
			}
		} else {
			final EsSourceHtml sourceHtml;
			try {
				sourceHtml = g.sourceLoader.newSourceHtml(qccSourcePath, showImports);
			} catch (final EsSourceLoadException ex) {
				paraAttention(writer, "Source code for " + qccSourcePath + " is not available");
				return;
			} catch (final EsSyntaxException ex) {
				paraAttention(writer, "Source code for " + qccSourcePath + " contains syntax error(s)");
				codeBlock(writer, ex.getMessage());
				return;
			}

			final String oqAuthors = sourceHtml.oqAuthors();
			final String oqPurpose = sourceHtml.oqPurpose();
			if (oqAuthors != null || oqPurpose != null) {
				tableBodyStart(writer, null);
				if (oqAuthors != null) {
					row(writer, true, "Author(s)", oqAuthors);
				}
				if (oqPurpose != null) {
					row(writer, true, "Purpose", oqPurpose);
				}
				tableBodyEnd(writer);
			}
			sourceHtml.writeListingView(writer, compact);
		}

		formStart(writer, m_path, true, false);
		fieldsetStart(writer, "EcmaScript Source");
		tableBodyStart(writer, null);
		rowInputCheckbox(writer, "Original Text ?", ForiginalText, originalText);
		rowInputCheckbox(writer, "Show Imports ?", FshowImports, showImports);
		rowInputCheckbox(writer, "Compact ?", Fcompact, compact);
		tableBodyEnd(writer);
		tableSubmit(writer, "Update");
		fieldsetEnd(writer);
		formEnd(writer);
	}

	public ShellPageSourceJs(BerylliumPath path) {
		super(path);
	}
}
