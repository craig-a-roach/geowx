/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumPathQuery;

/**
 * @author roach
 */
class ShellPageShutdown extends ShellPage {

	private static final String Qaction = "action";
	private static final String Qaction_confirmed = "confirmed";

	@Override
	public String qTitle() {
		return "Confirm Shutdown";
	}

	@Override
	public boolean render(ShellGlobal g, Request rq, HttpServletResponse rp, PrintWriter writer)
			throws BerylliumApiException, IOException, ServletException {
		final String ozAction = rq.getParameter(Qaction);
		if (ozAction != null && ozAction.equals(Qaction_confirmed)) {
			g.setStatusBar("Shutdown in progress");
			rp.sendRedirect(CShell.Redirect);
			g.notifyShutdown();
			return false;
		}
		final BerylliumPathQuery hrefConfirmed = CShell.Shutdown.newPathQuery(Qaction, Qaction_confirmed);
		title(writer, "Please confirm shutdown request...");
		navlist(writer, CShell.Redirect, "Cancel", hrefConfirmed, "Proceed");
		return true;
	}

	public ShellPageShutdown() {
	}
}
