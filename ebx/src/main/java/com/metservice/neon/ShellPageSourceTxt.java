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
class ShellPageSourceTxt extends ShellPageLanguage {

	@Override
	public String qTitle() {
		return "Source /" + qccSourcePath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException {
		final PrintWriter writer = rp.getWriter();
		menubar(writer, indexPath(), "Index", editTxtPath(), "Edit", helpPath(), "Help");

		final String qccSourcePath = qccSourcePath();
		try {
			final String ztwText = g.kc.sourceProvider.source(qccSourcePath);
			textArea(writer, "txta", ztwText);
			styleEditArea(writer, "txta", true, true);
		} catch (final EsSourceLoadException ex) {
			paraAttention(writer, "Source code for " + qccSourcePath + " is not available");
			return;
		}
	}

	public ShellPageSourceTxt(BerylliumPath path) {
		super(path);
	}
}
