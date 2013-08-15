/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.io.IOException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * @author roach
 */
class TestHelpBrowser {

	public HttpExchange sendGET(String url)
			throws IOException {
		final HttpExchange he = new HttpExchange();
		he.setMethod(HttpMethods.GET);
		he.setURL(url);
		httpClient.send(he);
		return he;
	}

	public void shutdown()
			throws Exception {
		httpClient.stop();
		server.stop();
	}

	public TestHelpBrowser(int listenPort, AbstractHandler handler) throws Exception {
		if (handler == null) throw new IllegalArgumentException("object is null");
		this.listenPort = listenPort;
		final BerylliumHttpServerFactory.ServerConfig serverCfg = BerylliumHttpServerFactory.newServerConfig();
		final BerylliumHttpServerFactory.ConnectorConfig cxCfg = BerylliumHttpServerFactory.newConnectorConfig(listenPort);
		server = BerylliumHttpServerFactory.newServer(serverCfg, cxCfg);
		server.setHandler(handler);
		server.start();

		final BerylliumHttpClientFactory.Config clientCfg = BerylliumHttpClientFactory.newConfig("browser");
		httpClient = BerylliumHttpClientFactory.newClient(clientCfg);
		httpClient.start();
	}
	public final int listenPort;
	public final Server server;
	public final HttpClient httpClient;
}
