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

/**
 * @author roach
 */
class ShellPageIndex extends ShellPage {

	@Override
	public String qTitle() {
		return "Home";
	}

	@Override
	public boolean render(ShellGlobal g, Request rq, HttpServletResponse rp, PrintWriter writer)
			throws BerylliumApiException, IOException, ServletException {
		menubar(writer, CShell.Shutdown, "Shutdown");
		contentStart(writer);
		para(writer, "Hello from earlybird");
		contentEnd(writer);
		return true;
	}

	public ShellPageIndex() {
	}
}
