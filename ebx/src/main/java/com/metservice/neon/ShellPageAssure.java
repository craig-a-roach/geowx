/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
class ShellPageAssure extends ShellPageHub {

	private boolean isAssure(ShellGlobal g, INeonSourceDescriptor sd) {
		final BerylliumPath sourcePath = subProviderPath(sd.qccNode());
		return g.assurance.isAssure(sourcePath);
	}

	private String qState(ShellGlobal g, INeonSourceDescriptor sd) {
		final BerylliumPath sourcePath = subProviderPath(sd.qccNode());
		final String zState = g.assurance.zStateTree(sourcePath);
		return zState.length() == 0 ? "-" : zState;
	}

	private void renderList(ShellGlobal g, PrintWriter writer) {
		final String zccProviderPath = providerPath().ztwPath();
		final List<? extends INeonSourceDescriptor> zlAsc = zlSubDescriptors(g);
		final int count = zlAsc.size();
		tableHeadStart(writer, "Scripts in /" + zccProviderPath);
		rowHeader(writer, "Resource", "Action", "Status");
		tableHeadEndBodyStart(writer);
		for (int i = 0; i < count; i++) {
			final INeonSourceDescriptor sd = zlAsc.get(i);
			final boolean isAssure = isAssure(g, sd);
			if (!isAssure) {
				continue;
			}
			final BerylliumPath oNavHref = getAssureNavSubPath(sd);
			final BerylliumPath oRunHref = getAssureRunSubPath(sd);
			if (oNavHref == null || oRunHref == null) {
				continue;
			}
			final String qState = qState(g, sd);
			rowStart(writer);
			tdStart(writer);
			link(writer, oNavHref, sd.qccNode());
			tdEnd(writer);
			tdStart(writer);
			link(writer, oRunHref, "Run Test");
			tdEnd(writer);
			tdStart(writer);
			text(writer, qState);
			tdEnd(writer);
			rowEnd(writer);
		}
		tableBodyEnd(writer);
	}

	@Override
	public String qTitle() {
		return "Assure /" + providerPath().ztwPath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException, InterruptedException {
		final PrintWriter writer = rp.getWriter();
		menubar(writer, oParentAssurePath(), "Parent", managePath(), "Manage", indexPath(), "Index", selfPath(), "Refresh",
				helpPath(), "Help");
		contentStart(writer);
		renderList(g, writer);
	}

	public ShellPageAssure(BerylliumPath path) {
		super(path);
	}
}
