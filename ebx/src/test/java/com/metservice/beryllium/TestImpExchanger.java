/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.metservice.argon.CArgon;
import com.metservice.argon.json.JsonObject;

/**
 * @author roach
 */
class TestImpExchanger extends AbstractHandler {

	@Override
	public void handle(String target, Request rq, HttpServletRequest sr, HttpServletResponse rp)
			throws IOException, ServletException {
		rq.setHandled(true);
		final BerylliumPath path = BerylliumPath.newInstance(rq);
		if (path.depth == 0) {
			rp.sendError(HttpServletResponse.SC_NOT_FOUND, "Incomplete uri");
			return;
		}
		final String qtwNode = path.qtwNode(0);
		IBerylliumJsonHttpService oService = null;
		if (qtwNode.equals("json")) {
			oService = new TestImpJsonService(probe);
		}
		if (oService == null) {
			rp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unsupported uri node '" + qtwNode + "'");
		} else {
			m_jsonDispatcher.handle(oService, path, rq, rp);
		}
	}

	public TestImpJsonTracker send(TestImpHostPort hp, BerylliumPath uri, TestImpJsonContent content)
			throws InterruptedException {
		final BerylliumJsonHttpExchange hex = BerylliumJsonHttpExchange.newInstance(hp, uri, content);
		final TestImpJsonTracker ht = new TestImpJsonTracker(probe, hp, uri, content.toString());
		hex.sendFrom(peer.httpClient, ht, probe);
		return ht;
	}

	public TestImpJsonTracker sendPulse(TestImpHostPort hp, BerylliumPath uri, int id)
			throws InterruptedException {
		final BerylliumJsonHttpExchange hex = BerylliumJsonHttpExchange.newInstance(hp, uri, JsonObject.Empty);
		final TestImpJsonTracker ht = new TestImpJsonTracker(probe, hp, uri, "Pulse" + id);
		hex.sendFrom(peer.httpClient, ht, probe);
		return ht;
	}

	public TestImpExchanger(TestHelpPeer peer, boolean sysout, boolean showLive) {
		assert peer != null;
		this.peer = peer;
		peer.server.setHandler(this);
		probe = new TestImpNetProbe(sysout, showLive);
		m_jsonDispatcher = new BerylliumJsonHttpDispatcher(probe, 1024 * CArgon.M, false);
	}
	public final TestHelpPeer peer;
	public final TestImpNetProbe probe;
	private final BerylliumJsonHttpDispatcher m_jsonDispatcher;
}
