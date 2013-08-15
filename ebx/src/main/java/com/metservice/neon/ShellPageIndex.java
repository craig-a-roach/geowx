/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
class ShellPageIndex extends ShellPageHub {

	private static final String FautoDebugPattern = "autoDebugPattern";

	private void renderAuto(ShellSession sn, ShellGlobal g, Request rq, PrintWriter writer) {
		final BerylliumPath selfPath = selfPath();
		formStart(writer, selfPath, true, false);
		tableBodyStart(writer, "Auto Debug...");
		renderAutoDebug(sn, g, rq, writer);
		tableBodyEnd(writer);
		buttonSubmit(writer, "Change");
		formEnd(writer);
	}

	private void renderAutoDebug(ShellSession sn, ShellGlobal g, Request rq, PrintWriter writer) {
		final BerylliumSupportId sid = sn.idSupport();
		final Pattern oExPattern = g.debugger.getAutoPattern(sid);
		final String zExPattern = oExPattern == null ? "" : oExPattern.pattern();
		String zNeoPattern = zFieldValue(FautoDebugPattern, rq, zExPattern);
		if (!zExPattern.equals(zNeoPattern)) {
			try {
				final Pattern oNeoPattern = zNeoPattern.length() == 0 ? null : Pattern.compile(zNeoPattern);
				g.debugger.setAutoPattern(sid, oNeoPattern);
			} catch (final PatternSyntaxException ex) {
				sn.sendMessage("Malformed regex..." + ex.getMessage());
				zNeoPattern = zExPattern;
			}
		}
		rowInputText(writer, "Source Path Pattern", FautoDebugPattern, zNeoPattern, 40);
	}

	private void renderConsole(ShellGlobal g, PrintWriter writer) {
		final List<String> zlqccSourcePathsAsc = g.console.zlqccSourcePathsAsc(false);
		if (zlqccSourcePathsAsc.isEmpty()) return;
		fieldsetStart(writer, "Console");
		filelistStart(writer);
		for (final String qccSourcePath : zlqccSourcePathsAsc) {
			final ConsoleSource oSource = g.console.findSource(qccSourcePath);
			if (oSource != null) {
				final BerylliumPath href = BerylliumPath.newAbsolute(CNeonShell.Node_ui, qccSourcePath,
						CNeonShell.Node_console);
				filelistLink(writer, href, oSource.qccSourcePath);
			}
		}
		filelistEnd(writer);
		fieldsetEnd(writer);
	}

	private void renderList(ShellGlobal g, PrintWriter writer) {
		final String zccProviderPath = providerPath().ztwPath();
		final List<? extends INeonSourceDescriptor> zlAsc = zlSubDescriptors(g);
		final int count = zlAsc.size();
		tableHeadStart(writer, "Scripts in /" + zccProviderPath);
		rowHeader(writer, "Resource", "Last Modified");
		tableHeadEndBodyStart(writer);
		for (int i = 0; i < count; i++) {
			final INeonSourceDescriptor sd = zlAsc.get(i);
			if (sd.isWip()) {
				continue;
			}
			final BerylliumPath oHref = getEditSubPath(sd, false);
			if (oHref != null) {
				rowStart(writer);
				tdStart(writer);
				link(writer, oHref, sd.qccNode());
				tdEnd(writer);
				tdStart(writer);
				text(writer, qLastModified(sd));
				tdEnd(writer);
				rowEnd(writer);
			}
		}
		tableBodyEnd(writer);
	}

	private void renderSessionDebugger(BerylliumSupportId sid, ShellGlobal g, PrintWriter writer) {
		final DebuggerSession oSession = g.debugger.findSession(sid);
		if (oSession == null) return;
		final List<String> zlqccSourcePathsAsc = oSession.zlqccSourcePathsAsc(false);
		if (zlqccSourcePathsAsc.isEmpty()) return;
		fieldsetStart(writer, "Debugger: " + sid);
		filelistStart(writer);
		for (final String qccSourcePath : zlqccSourcePathsAsc) {
			final DebuggerSessionSource oSource = oSession.findSource(qccSourcePath);
			final DebugState oState = oSource == null ? null : oSource.getState();
			if (oState != null) {
				final BerylliumPath href = BerylliumPath.newAbsolute(CNeonShell.Node_ui, qccSourcePath,
						CNeonShell.Node_debug);
				filelistLink(writer, href, oState.lineHere());
			}
		}
		filelistEnd(writer);
		fieldsetEnd(writer);
	}

	private void renderSessionProfiler(BerylliumSupportId sid, ShellGlobal g, PrintWriter writer) {
		final ProfilerSession oSession = g.profiler.findSession(sid);
		if (oSession == null) return;
		final List<String> zlqccSourcePathsAsc = oSession.zlqccSourcePathsAsc();
		if (zlqccSourcePathsAsc.isEmpty()) return;
		fieldsetStart(writer, "Profiler: " + sid);
		filelistStart(writer);
		for (final String qccSourcePath : zlqccSourcePathsAsc) {
			final ProfilerSessionSource oSource = oSession.findSource(qccSourcePath);
			if (oSource == null) {
				continue;
			}
			final BerylliumPath href = BerylliumPath.newAbsolute(CNeonShell.Node_ui, qccSourcePath, CNeonShell.Node_profile);
			filelistLink(writer, href, oSource.qccSourcePath);
		}
		filelistEnd(writer);
		fieldsetEnd(writer);
	}

	private void renderSessions(ShellSession sn, ShellGlobal g, PrintWriter writer) {
		final BerylliumSupportId sidSelf = sn.idSupport();
		for (final BerylliumSupportId sid : g.debugger.zlSupportIdsAsc()) {
			if (sid.equals(sidSelf)) {
				renderSessionDebugger(sid, g, writer);
			} else {
				para(writer, "Remote Debug: " + sid);
			}
		}
		for (final BerylliumSupportId sid : g.profiler.zlSupportIdsAsc()) {
			renderSessionProfiler(sid, g, writer);
		}
	}

	@Override
	public String qTitle() {
		return "Index /" + providerPath().ztwPath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException {
		final PrintWriter writer = rp.getWriter();
		menubar(writer, oParentIndexPath(), "Parent", managePath(), "Manage", assureNavPath(), "Assure", oShutdownPath(g),
				"Shutdown", helpPath(), "Help");
		contentStart(writer);
		if (!g.isProcess()) {
			renderAuto(sn, g, rq, writer);
		}
		renderList(g, writer);
		contentEnd(writer);
		sidebarStart(writer);
		renderConsole(g, writer);
		renderSessions(sn, g, writer);
		sidebarEnd(writer);
	}

	public ShellPageIndex(BerylliumPath path) {
		super(path);
	}
}
