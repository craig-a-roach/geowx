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
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException, InterruptedException {
		final boolean isShellProcess = g.kc.cfg.getShellProcess();
		final String ozAction = rq.getParameter(Qaction);
		if (isShellProcess && ozAction != null && ozAction.equals(Qaction_confirmed)) {
			sn.sendMessage("Shutdown in progress....");
			rp.sendRedirect(CNeonShell.Redirect);
			g.notifyShutdown();
			return;
		}
		final PrintWriter writer = rp.getWriter();
		if (isShellProcess) {
			final BerylliumPathQuery hrefConfirmed = CNeonShell.Shutdown.newPathQuery(Qaction, Qaction_confirmed);
			title(writer, "Please confirm shutdown request...");
			navlist(writer, CNeonShell.Redirect, "Cancel", hrefConfirmed, "Proceed");
		} else {
			title(writer, "Shutdown not available...");
			link(writer, CNeonShell.Redirect, "Cancel");
		}
	}

	public ShellPageShutdown() {
	}
}
