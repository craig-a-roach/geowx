/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Server;

/**
 * @author roach
 */
class TestHelpPeer {

	public void start()
			throws Exception {
		server.start();
		httpClient.start();
	}

	public void stop()
			throws Exception {
		httpClient.stop();
		server.stop();
	}

	public TestHelpPeer(String qccRole, int listenPort) {
		this.listenPort = listenPort;
		final BerylliumHttpServerFactory.ServerConfig serverCfg = BerylliumHttpServerFactory.newServerConfig();
		final BerylliumHttpServerFactory.ConnectorConfig cxCfg = BerylliumHttpServerFactory.newConnectorConfig(listenPort);
		server = BerylliumHttpServerFactory.newServer(serverCfg, cxCfg);

		final BerylliumHttpClientFactory.Config clientCfg = BerylliumHttpClientFactory.newConfig(qccRole);
		httpClient = BerylliumHttpClientFactory.newClient(clientCfg);
	}

	public final int listenPort;
	public final Server server;
	public final HttpClient httpClient;
}
