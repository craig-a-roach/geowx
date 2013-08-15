/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.argon.Ds;
import com.metservice.argon.text.ArgonSplitter;
import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
class ShellPageManage extends ShellPageHub {

	private static final String Fcmd = "cmd";
	private static final String Fcmdn = "cmd_";

	private static final Pattern Splitter = Pattern.compile("\\s+");
	private static final String Verb_new = "new";
	private static final String Verb_on = "on";
	private static final String Verb_off = "off";
	private static final String Verb_rm = "rm";
	private static final String Verb_mkdir = "mkdir";
	private static final String Verb_rmdir = "rmdir";

	private static final String Verb_newusing = "newusing";
	private static final String Verb_re = "re";
	private static final String Verb_redir = "redir";
	private static final String[] Verbs1 = { Verb_new, Verb_on, Verb_off, Verb_rm, Verb_mkdir, Verb_rmdir };
	private static final String[] Verbs2 = { Verb_newusing, Verb_re, Verb_redir };

	private static int expected(String qlctwVerb)
			throws CmdException {
		for (int i = 0; i < Verbs1.length; i++) {
			if (qlctwVerb.equals(Verbs1[i])) return 1;
		}
		for (int i = 0; i < Verbs2.length; i++) {
			if (qlctwVerb.equals(Verbs2[i])) return 2;
		}
		throw new CmdException("Unrecognised command '" + qlctwVerb + "'");
	}

	private static void helpPanel(PrintWriter writer) {
		assert writer != null;
		tableBodyStart(writer, "Options");
		rowHeader(writer, "Verb", "Arguments", "Action");
		row(writer, true, Verb_new, "fileName", "Create new empty offline file if name is unique");
		row(writer, true, Verb_on, "[fileName]", "Bring file online");
		row(writer, true, Verb_off, "[fileName]", "Take file offline for editing");
		row(writer, true, Verb_newusing, "[fileNameSource]\tfileNameNew",
				"Create new offline file using source in same directory if name is unique");
		row(writer, true, Verb_rm, "[fileName]", "Remove file");
		row(writer, true, Verb_re, "[fileNameFrom]\tfileNameTo", "Rename file in same directory if new name is unique");
		row(writer, true, Verb_mkdir, "[directoryName]", "Create new directory");
		row(writer, true, Verb_rmdir, "[directoryName]", "Remove empty directory");
		row(writer, true, Verb_redir, "[directoryNameFrom]\tdirectoryNameTo",
				"Rename directory under same parent if new name is unique");
		tableBodyEnd(writer);
		para(writer, "When entering resource commands, the first [argument] is the resource name.");
	}

	private String execCommand(ShellGlobal g, String zcmd, String oqtwArg0)
			throws CmdException {
		assert zcmd != null;
		final String ztwCmd = zcmd.trim();
		if (ztwCmd.length() == 0) return null;
		final String[] zptqtwCmd = ArgonSplitter.zptqtwSplit(ztwCmd, Splitter);
		final int lenCmd = zptqtwCmd.length;
		if (lenCmd == 0) throw new CmdException("Empty command");
		final String qlctwVerb = zptqtwCmd[0].toLowerCase();
		final int argcExpected = expected(qlctwVerb);
		final String[] xptqtwArgs;
		if (oqtwArg0 == null) {
			xptqtwArgs = new String[lenCmd - 1];
			for (int i = 1; i < lenCmd; i++) {
				xptqtwArgs[i - 1] = zptqtwCmd[i];
			}
		} else {
			xptqtwArgs = new String[lenCmd];
			xptqtwArgs[0] = oqtwArg0;
			for (int i = 1; i < lenCmd; i++) {
				xptqtwArgs[i] = zptqtwCmd[i];
			}
		}
		final int argcActual = xptqtwArgs.length;
		if (argcActual != argcExpected) {
			final String msg = "Malformed '" + qlctwVerb + "' command; expecting " + argcExpected + " arguments";
			throw new CmdException(msg);
		}
		return execVerb(g, qlctwVerb, xptqtwArgs);
	}

	private String execMkDir(ShellGlobal g, String qtwNode) {
		try {
			final BerylliumPath neo = subProviderPath(qtwNode);
			g.kc.sourceProvider.makeDirectory(neo.qtwPath());
			return null;
		} catch (final EsSourceSaveException ex) {
			return "Could not create subdirectory; " + Ds.message(ex);
		}
	}

	private String execNew(ShellGlobal g, String qtwNode)
			throws CmdException {
		try {
			final ShellLanguage language = ShellLanguage.newInstance(qtwNode);
			final String qtwNodeWip = NeonFileExtension.applyWip(qtwNode);
			final BerylliumPath neo = subProviderPath(qtwNodeWip);
			final String zSrc = zInitSource(language);
			g.kc.sourceProvider.freshSource(neo.qtwPath(), zSrc);
			return null;
		} catch (final EsSourceSaveException ex) {
			throw new CmdException(ex);
		}
	}

	private String execNewUsing(ShellGlobal g, String qtwNodeSource, String qtwNodeNew)
			throws CmdException {
		final String qtwNodeNewWip = NeonFileExtension.applyWip(qtwNodeNew);
		try {
			if (!qtwNodeSource.equals(qtwNodeNewWip)) {
				final BerylliumPath source = subProviderPath(qtwNodeSource);
				final BerylliumPath dest = subProviderPath(qtwNodeNewWip);
				g.kc.sourceProvider.copy(source.qtwPath(), dest.qtwPath());
			}
			return null;
		} catch (final EsSourceLoadException ex) {
			throw new CmdException(ex);
		} catch (final EsSourceSaveException ex) {
			throw new CmdException(ex);
		}
	}

	private String execOff(ShellGlobal g, String qtwNode)
			throws CmdException {
		final String qtwNodeBase = NeonFileExtension.qcctwBaseName(qtwNode);
		final String qtwNodeWip = NeonFileExtension.applyWip(qtwNodeBase);
		return execRe(g, qtwNodeBase, qtwNodeWip);
	}

	private String execOn(ShellGlobal g, String qtwNode)
			throws CmdException {
		final String qtwNodeBase = NeonFileExtension.qcctwBaseName(qtwNode);
		final String qtwNodeWip = NeonFileExtension.applyWip(qtwNodeBase);
		return execRe(g, qtwNodeWip, qtwNodeBase);
	}

	private String execRe(ShellGlobal g, String qtwNodeFrom, String qtwNodeTo)
			throws CmdException {
		try {
			if (!qtwNodeFrom.equals(qtwNodeTo)) {
				final BerylliumPath from = subProviderPath(qtwNodeFrom);
				final BerylliumPath to = subProviderPath(qtwNodeTo);
				g.kc.sourceProvider.rename(from.qtwPath(), to.qtwPath());
			}
			return null;
		} catch (final EsSourceSaveException ex) {
			throw new CmdException(ex);
		}
	}

	private String execReDir(ShellGlobal g, String qtwNodeFrom, String qtwNodeTo)
			throws CmdException {
		try {
			if (!qtwNodeFrom.equals(qtwNodeTo)) {
				final BerylliumPath from = subProviderPath(qtwNodeFrom);
				final BerylliumPath to = subProviderPath(qtwNodeTo);
				g.kc.sourceProvider.renameDirectory(from.qtwPath(), to.qtwPath());
			}
			return null;
		} catch (final EsSourceSaveException ex) {
			throw new CmdException(ex);
		}
	}

	private String execRm(ShellGlobal g, String qtwNode)
			throws CmdException {
		try {
			final BerylliumPath neo = subProviderPath(qtwNode);
			g.kc.sourceProvider.remove(neo.qtwPath());
			return null;
		} catch (final EsSourceSaveException ex) {
			throw new CmdException(ex);
		}
	}

	private String execRmDir(ShellGlobal g, String qtwNode)
			throws CmdException {
		try {
			final BerylliumPath neo = subProviderPath(qtwNode);
			g.kc.sourceProvider.removeDirectory(neo.qtwPath());
			return null;
		} catch (final EsSourceSaveException ex) {
			throw new CmdException(ex);
		}
	}

	private String execVerb(ShellGlobal g, String qlctwVerb, String[] xptqtwArgs)
			throws CmdException {
		if (qlctwVerb.equals(Verb_new)) return execNew(g, xptqtwArgs[0]);
		if (qlctwVerb.equals(Verb_on)) return execOn(g, xptqtwArgs[0]);
		if (qlctwVerb.equals(Verb_off)) return execOff(g, xptqtwArgs[0]);
		if (qlctwVerb.equals(Verb_newusing)) return execNewUsing(g, xptqtwArgs[0], xptqtwArgs[1]);
		if (qlctwVerb.equals(Verb_rm)) return execRm(g, xptqtwArgs[0]);
		if (qlctwVerb.equals(Verb_re)) return execRe(g, xptqtwArgs[0], xptqtwArgs[1]);
		if (qlctwVerb.equals(Verb_mkdir)) return execMkDir(g, xptqtwArgs[0]);
		if (qlctwVerb.equals(Verb_rmdir)) return execRmDir(g, xptqtwArgs[0]);
		if (qlctwVerb.equals(Verb_redir)) return execReDir(g, xptqtwArgs[0], xptqtwArgs[1]);
		return null;
	}

	private void renderList(ShellGlobal g, PrintWriter writer, Map<String, String> zmRe) {
		final String zccProviderPath = providerPath().ztwPath();
		final List<? extends INeonSourceDescriptor> zlAsc = zlSubDescriptors(g);
		final int count = zlAsc.size();
		tableHeadStart(writer, "Scripts in /" + zccProviderPath);
		rowHeader(writer, "Resource", "Last Modified", "Visibility", "Command");
		tableHeadEndBodyStart(writer);
		for (int i = 0; i < count; i++) {
			final INeonSourceDescriptor sd = zlAsc.get(i);
			final BerylliumPath oHref = getEditSubPath(sd, true);
			if (oHref != null) {
				final String qccNode = sd.qccNode();
				rowStart(writer);
				tdStart(writer);
				link(writer, oHref, sd.qccNode());
				tdEnd(writer);
				tdStart(writer);
				text(writer, qLastModified(sd));
				tdEnd(writer);
				tdStart(writer);
				text(writer, qVisibility(sd));
				tdEnd(writer);
				tdStart(writer);
				final String ozRe = zmRe.get(qccNode);
				final String zRe = ozRe == null ? "" : ozRe;
				inputText(writer, Fcmdn + qccNode, zRe, 40);
				tdEnd(writer);
				rowEnd(writer);
			}
		}
		tableBodyEnd(writer);
	}

	private String zInitSource(ShellLanguage language) {
		assert language != null;
		if (language != ShellLanguage.EcmaScript) return "";
		return "AUTHOR('name');\nPURPOSE('todo');\n";
	}

	@Override
	public String qTitle() {
		return "Manage /" + providerPath().ztwPath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException {
		final PrintWriter writer = rp.getWriter();
		menubar(writer, oParentManagePath(), "Parent", indexPath(), "Index", assureNavPath(), "Assure", helpPath(), "Help");
		contentStart(writer);
		final BerylliumPath selfPath = selfPath();
		formStart(writer, selfPath, true, false);
		final List<String> zlAttention = new ArrayList<String>();
		final String zExCmd = "";
		final String zNeoCmd = zFieldValue(Fcmd, rq, zExCmd);
		String zReCmd = "";
		try {
			execCommand(g, zNeoCmd, null);
		} catch (final CmdException ex) {
			zlAttention.add(ex.getMessage());
			zReCmd = zNeoCmd;
		}
		final Map<String, String> zmCmdEx = Collections.emptyMap();
		final Map<String, String> zmNeoCmd = zmFieldValue(Fcmdn, rq, zmCmdEx);
		final Map<String, String> zmReCmd = new HashMap<String, String>();
		final List<String> zlNodeNamesAsc = new ArrayList<String>(zmNeoCmd.keySet());
		Collections.sort(zlNodeNamesAsc);
		for (final String qccNodeName : zlNodeNamesAsc) {
			final String ozNeoCmdN = zmNeoCmd.get(qccNodeName);
			final String zNeoCmdN = ozNeoCmdN == null ? "" : ozNeoCmdN.trim();
			try {
				execCommand(g, zNeoCmdN, qccNodeName);
			} catch (final CmdException ex) {
				zlAttention.add(ex.getMessage());
				zmReCmd.put(qccNodeName, zNeoCmdN);
			}
		}
		tableBodyStart(writer, "Command");
		rowInputText(writer, "", Fcmd, zReCmd, 80);
		tableBodyEnd(writer);
		renderList(g, writer, zmReCmd);
		buttonSubmit(writer, "Execute");
		formEnd(writer);
		if (!zlAttention.isEmpty()) {
			bulletsStart(writer);
			for (final String attention : zlAttention) {
				itemAttention(writer, attention);
			}
			bulletsStart(writer);
		}
		contentEnd(writer);
		sidebarStart(writer);
		helpPanel(writer);
		sidebarEnd(writer);
	}

	public ShellPageManage(BerylliumPath path) {
		super(path);
	}

	private static class CmdException extends Exception {

		public CmdException(String message) {
			super(message);
		}

		public CmdException(Throwable cause) {
			super(Ds.message(cause));
		}
	}
}
