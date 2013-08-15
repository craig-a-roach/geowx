/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import java.util.concurrent.ExecutorService;

import org.eclipse.jetty.server.Server;

import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.Ds;
import com.metservice.argon.management.IArgonService;
import com.metservice.beryllium.BerylliumHttpConnectorType;
import com.metservice.beryllium.BerylliumHttpServerFactory;

/**
 * @author roach
 */
class KmlService implements IArgonService {

	public static KmlService newInstance(KernelCfg kc) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		final int listenPort = kc.id.kmlListenPort();
		final BerylliumHttpServerFactory.ServerConfig sc = BerylliumHttpServerFactory.newServerConfig();
		sc.maxThreads = kc.cfgSpace.getKmlServiceMaxThreads();
		sc.minThreads = kc.cfgSpace.getKmlServiceMinThreads();
		sc.gracefulShutdownMs = CSpace.HttpGracefulShutdown.intMsSigned();
		final BerylliumHttpConnectorType cxType = kc.cfgSpace.getServiceConnectorType();
		final BerylliumHttpServerFactory.ConnectorConfig cc = BerylliumHttpServerFactory.newConnectorConfig(listenPort, cxType);
		final Server server = BerylliumHttpServerFactory.newServer(sc, cc);
		final KmlHandler handler = new KmlHandler(kc);
		server.setHandler(handler);
		return new KmlService(kc, server);
	}

	@Override
	public String name() {
		return "KmlService";
	}

	@Override
	public void serviceEnd() {
		try {
			m_server.stop();
		} catch (final Exception ex) {
			final Ds ds = Ds.triedTo("Stop kml web server", ex, "Potential resource leak");
			ds.a("kmlPort", kc.id.kmlListenPort());
			kc.probe.warnNet(ds);
		}
	}

	@Override
	public void serviceStart(ExecutorService xc)
			throws ArgonPlatformException, InterruptedException {
		try {
			m_server.start();
		} catch (final Exception ex) {
			final int port = kc.id.kmlListenPort();
			final Ds ds = Ds.triedTo("Start kml web server", ex, ArgonPlatformException.class);
			ds.a("kmlPort", port);
			kc.probe.failNet(ds);
			final String m = "Kml web server failed to start on port " + port;
			throw new ArgonPlatformException(m);
		}
	}

	private KmlService(KernelCfg kc, Server srv) {
		assert kc != null;
		assert srv != null;
		this.kc = kc;
		m_server = srv;
	}
	final KernelCfg kc;
	private final Server m_server;
}
