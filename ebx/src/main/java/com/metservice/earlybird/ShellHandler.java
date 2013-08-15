/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.metservice.argon.Ds;
import com.metservice.argon.text.ArgonTransformer;
import com.metservice.beryllium.BerylliumApiException;
import com.metservice.beryllium.BerylliumAssetLoader;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.CBeryllium;

/**
 * @author roach
 */
class ShellHandler extends AbstractHandler {

	private ShellPage createUIPage(BerylliumPath path) {

		if (path.depth < 2) return null;

		final String qtwLastNode = path.qtwNode(-1);
		if (qtwLastNode.equals(CShell.Node_index)) return new ShellPageIndex();
		if (qtwLastNode.equals(CShell.Node_shutdown)) return new ShellPageShutdown();
		return null;
	}

	private void footer(PrintWriter writer) {
		writer.println("<div id=\"footer\">");
		final String oqtwMessage = m_global.oqtwStatusBar();
		final String qStatus = oqtwMessage == null ? "OK" : oqtwMessage;
		final String qheStatus = ArgonTransformer.zHtmlEncodePCDATA(qStatus);
		writer.println("<p>" + qheStatus + "</p>");
		writer.println("</div>");
		writer.println("</body>");
		writer.println("</html>");
	}

	private void handleUI(BerylliumPath path, Request rq, HttpServletResponse rp)
			throws IOException, ServletException {
		final ShellPage oPage = createUIPage(path);
		if (oPage == null) {
			rp.sendError(HttpServletResponse.SC_NOT_FOUND, rq.getPathInfo());
			return;
		} else {
			renderPage(path, rq, rp, oPage);
		}
	}

	private void header(PrintWriter writer, ShellPage page) {
		writer.println("<body>");
		writer.println("<div id=\"header\">");
		writer.println("<h1>" + CShell.TitlePrefix + " Shell</h1>");
		final String qheTitle = ArgonTransformer.zHtmlEncodePCDATA(page.qTitle());
		writer.println("<h2>" + qheTitle + "</h2>");
		writer.println("</div>");
	}

	private void meta(PrintWriter writer, ShellPage page) {
		writer.println("<head>");
		final String qheTitle = ArgonTransformer.zHtmlEncodePCDATA(page.qTitle());
		writer.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\">");
		writer.println("<title>" + CShell.TitlePrefix + " " + qheTitle + "</title>");
		writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + CShell.CSS_ui + "\"/>");
		page.scripts(writer);
		page.styles(writer);
		writer.println("</head>");
	}

	private void prologue(PrintWriter writer) {
		writer.println(CBeryllium.Html5Prologue);
		writer.println("<html lang=\"en\">");
	}

	private void renderPage(BerylliumPath path, Request rq, HttpServletResponse rp, ShellPage page)
			throws IOException, ServletException {
		rp.setStatus(HttpServletResponse.SC_OK);
		rp.setContentType("text/html;charset=utf-8");
		final PrintWriter writer = rp.getWriter();
		prologue(writer);
		meta(writer, page);
		header(writer, page);
		boolean rendered = false;
		try {
			rendered = page.render(m_global, rq, rp, writer);
		} catch (final BerylliumApiException ex) {
			final Ds ds = Ds.triedTo("Handle request", ex);
			ds.a("page", page);
			ds.a("request", rq);
			kc.probe.failSoftware(ds);
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo("Handle request", ex);
			ds.a("page", page);
			ds.a("request", rq);
			kc.probe.failSoftware(ds);
		}
		if (rendered) {
			footer(writer);
		}
	}

	@Override
	public void handle(String target, Request rq, HttpServletRequest sr, HttpServletResponse rp)
			throws IOException, ServletException {
		rq.setHandled(true);
		final BerylliumPath path = BerylliumPath.newInstance(rq);

		if (path.match(0, CShell.Node_ui)) {
			handleUI(path, rq, rp);
			return;
		}
		if (path.match(0, CShell.Node_asset)) {
			m_assetLoader.handle(path, rq, rp);
			return;
		}

		if (path.isFavouriteIcon()) {
			m_assetLoader.handle(CShell.Favicon, rq, rp);
			return;
		}

		rp.sendRedirect(CShell.Redirect);
	}

	public ShellHandler(KernelCfg kc, ShellGlobal global) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		if (global == null) throw new IllegalArgumentException("object is null");
		this.kc = kc;
		m_global = global;
		m_assetLoader = new BerylliumAssetLoader(null, getClass());
	}
	final KernelCfg kc;
	private final ShellGlobal m_global;
	private final BerylliumAssetLoader m_assetLoader;
}
