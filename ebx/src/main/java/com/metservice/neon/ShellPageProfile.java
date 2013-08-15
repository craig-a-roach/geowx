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
class ShellPageProfile extends ShellPageLeaf {

	@Override
	public String qTitle() {
		return "Profile /" + qccSourcePath();
	}

	@Override
	public void render(ShellSession sn, ShellGlobal g, Request rq, HttpServletResponse rp)
			throws BerylliumApiException, IOException, ServletException, InterruptedException {
		final BerylliumSupportId sid = sn.idSupport();
		final PrintWriter writer = rp.getWriter();
		final String qccSourcePath = qccSourcePath();
		menubar(writer, indexPath(), "Index", controlPath(), "Control", m_path, "Refresh", helpPath(), "Help");
		final ProfileAggregate oprf = g.profiler.getAggregate(sid, qccSourcePath);
		if (oprf == null) {
			paraAttention(writer, "Profiling Not Available");
		} else {
			final ProfileSamplePI program = oprf.program;
			final EsSourceHtml sourceHtml = oprf.sourceHtml;
			sourceHtml.writeListingProfile(writer, program);
		}
	}

	public ShellPageProfile(BerylliumPath path) {
		super(path);
	}
}
