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

import com.metservice.argon.ArgonNumber;
import com.metservice.argon.ArgonTransformer;
import com.metservice.argon.DateFormatter;
import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.BerylliumPathQuery;

/**
 * @author roach
 */
class ShellPageConsole extends ShellPageLeaf {

	private static final String FabsoluteTiming = "absoluteTiming";
	private static final String FlistForward = "listForward";
	private static final String FshowTrace = "showTrace";

	private static final String Qaction = "action";
	private static final String Qaction_clear = "clear";

	private void applyAction(ShellSession sn, ShellGlobal g, Request rq) {
		final String ozAction = rq.getParameter(Qaction);
		if (ozAction == null) return;

		final String qccSourcePath = qccSourcePath();

		if (ozAction.equals(Qaction_clear)) {
			g.console.clear(qccSourcePath);
			sn.sendMessage("Messages cleared");
			return;
		}
		sn.sendMessage("Unknown action '" + ozAction + "'");
	}

	private void applyControl(Request rq, ShellSessionStateConsole sns, PrintWriter writer)
			throws InterruptedException {

		tableBodyStart(writer, null);
		rowStart(writer);

		tdStart(writer);
		formStart(writer, m_path, true, false);
		fieldsetStart(writer, "View Control");
		tableBodyStart(writer, null);
		applyControlListForward(writer, rq, sns);
		applyControlShowTrace(writer, rq, sns);
		applyControlAbsoluteTiming(writer, rq, sns);
		tableBodyEnd(writer);
		tableSubmit(writer, "Update");
		fieldsetEnd(writer);
		formEnd(writer);
		tdEnd(writer);

		tdStart(writer);
		fieldsetStart(writer, "Actions...");
		final BerylliumPathQuery hrefClear = m_path.newPathQuery(Qaction, Qaction_clear);
		navlist(writer, hrefClear, "Clear");
		fieldsetEnd(writer);
		tdEnd(writer);

		rowEnd(writer);
		tableBodyEnd(writer);
	}

	private void applyControlAbsoluteTiming(PrintWriter writer, Request rq, ShellSessionStateConsole sns) {
		final boolean ex = sns.absoluteTiming();
		final boolean neo = isChecked(FabsoluteTiming, rq, ex);
		if (neo != ex) {
			sns.absoluteTiming(neo);
		}
		rowInputCheckbox(writer, "Show Absolute Times ?", FabsoluteTiming, neo);
	}

	private void applyControlListForward(PrintWriter writer, Request rq, ShellSessionStateConsole sns) {
		final boolean ex = sns.listForward();
		final boolean neo = isChecked(FlistForward, rq, ex);
		if (neo != ex) {
			sns.listForward(neo);
		}
		rowInputCheckbox(writer, "Earliest First ?", FlistForward, neo);
	}

	private void applyControlShowTrace(PrintWriter writer, Request rq, ShellSessionStateConsole sns) {
		final boolean ex = sns.showTrace();
		final boolean neo = isChecked(FshowTrace, rq, ex);
		if (neo != ex) {
			sns.showTrace(neo);
		}
		rowInputCheckbox(writer, "Show Script Trace ?", FshowTrace, neo);
	}

	@Override
	public String qTitle() {
		return "Console /" + qccSourcePath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException, InterruptedException {
		final PrintWriter writer = rp.getWriter();
		final String qccSourcePath = qccSourcePath();
		;
		final ShellSessionStateConsole sns = sn.stateConsole(qccSourcePath);
		menubar(writer, indexPath(), "Index", controlPath(), "Control", m_path, "Refresh", sourceJsPath(), "Source",
				helpPath(), "Help");
		applyAction(sn, g, rq);
		applyControl(rq, sns, writer);

		final boolean listForward = sns.listForward();
		final boolean absoluteTiming = sns.absoluteTiming();
		final boolean showTrace = sns.showTrace();

		final ConsoleEntry[] ozptEntries = g.console.ozptEntries(qccSourcePath, ConsoleFilter.Any);
		if (ozptEntries == null) {
			paraAttention(writer, "Console Not Available");
			return;
		}
		final int entryCount = ozptEntries.length;
		if (entryCount == 0) {
			para(writer, "No Messages");
			return;
		}

		writer.println("<table><caption>Messages</caption><tbody>");
		final int ilast = entryCount - 1;
		long tsRef = 0L;
		for (int i = 0; i <= ilast; i++) {
			final int eindex = listForward ? i : ilast - i;
			final ConsoleEntry e = ozptEntries[eindex];
			if (!showTrace && (e.type == ConsoleType.EmitTrace)) {
				continue;
			}
			final String qType = e.type.name();
			final long msRel;
			if (i == 0) {
				msRel = 0L;
				tsRef = e.ts;
			} else {
				msRel = e.ts - tsRef;
			}
			if (i % 2 == 1) {
				writer.print("<tr class=\"odd\">");
			} else {
				writer.print("<tr>");
			}
			writer.print("<th>");
			if (absoluteTiming) {
				writer.print(DateFormatter.newT8FromTs(e.ts));
			} else {
				writer.print(ArgonNumber.longToDec(msRel, 6));
			}
			writer.print("</th>");

			writer.print("<td>");
			writer.print(qType);
			writer.print("</td>");

			writer.print("<td><span class=\"con" + qType + "\">");
			writer.print(ArgonTransformer.zHtmlEncodePCDATA(e.zLine));
			writer.print("</span></td>");

			writer.println("</tr>");
		}
		writer.println("</tbody></table>");
	}

	public ShellPageConsole(BerylliumPath path) {
		super(path);
	}
}
