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
import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
class ShellPageControl extends ShellPageLeaf {

	private static final String Fdebug = "debug";
	private static final String Fprofile = "profile";

	private void renderAssurance(PrintWriter writer, AssuranceSource source) {
		tableBodyStart(writer, "Assurance");
		row(writer, true, "Status", source.qState());
		final String ztwReport = source.ztwReport();
		if (ztwReport.length() > 0) {
			row(writer, true, "Report", ztwReport);
		}
		tableBodyEnd(writer);
	}

	private void renderDebugState(PrintWriter writer, DebugState debugState) {
		tableBodyStart(writer, "Debug Session");
		rowStart(writer);
		thStart(writer);
		text(writer, "Status");
		thEnd(writer);
		tdStart(writer);
		link(writer, debugPath(), debugState.lineHere());
		tdEnd(writer);
		rowEnd(writer);
		tableBodyEnd(writer);
	}

	@Override
	public String qTitle() {
		return "Script /" + qccSourcePath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException {
		final BerylliumSupportId sid = sn.idSupport();
		final PrintWriter writer = rp.getWriter();
		final String qccSourcePath = qccSourcePath();

		final boolean enableDebugEx = g.debugger.isEnabled(sid, qccSourcePath);
		final boolean enableDebugNeo = isChecked(Fdebug, rq, enableDebugEx);
		final BerylliumPath oDebugPath = enableDebugNeo ? debugPath() : null;

		final boolean enableProfileEx = g.profiler.isEnabled(sid, qccSourcePath);
		final boolean enableProfileNeo = isChecked(Fprofile, rq, enableProfileEx);
		final BerylliumPath oProfilePath = enableProfileNeo ? profilePath() : null;

		final AssuranceSource oAssuranceSource = g.assurance.findSource(qccSourcePath);
		final BerylliumPath oRefreshPath = oAssuranceSource == null ? null : m_path;

		final DebugState oDebugState = g.debugger.getState(sid, qccSourcePath);

		menubar(writer, indexPath(), "Index", sourceJsPath(), "Source", consolePath(), "Console", oDebugPath, "Debug",
				oProfilePath, "Profile", editJsPath(), "Edit", assuranceRunJsPath(), "Test", assuranceNavPath(), "Assure",
				managePath(), "Manage", oRefreshPath, "Refresh", helpPath(), "Help");

		formStart(writer, m_path, true, false);
		fieldsetStart(writer, "Execution Features");
		tableBodyStart(writer, null);

		rowInputCheckbox(writer, "Debug ?", Fdebug, enableDebugNeo);
		if (enableDebugNeo != enableDebugEx) {
			g.debugger.enable(sid, qccSourcePath, enableDebugNeo);
		}

		rowInputCheckbox(writer, "Profile ?", Fprofile, enableProfileNeo);
		if (enableProfileNeo != enableProfileEx) {
			g.profiler.enable(sid, qccSourcePath, enableProfileNeo);
		}

		tableBodyEnd(writer);
		tableSubmit(writer, "Update");
		fieldsetEnd(writer);
		formEnd(writer);
		if (oDebugState != null) {
			renderDebugState(writer, oDebugState);
		}

		if (oAssuranceSource != null) {
			renderAssurance(writer, oAssuranceSource);
		}
	}

	public ShellPageControl(BerylliumPath path) {
		super(path);
	}
}
