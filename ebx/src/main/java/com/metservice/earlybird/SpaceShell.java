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
class SpaceShell implements IArgonService {

	public static SpaceShell newInstance(KernelCfg kc) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		final int listenPort = kc.id.shellListenPort();
		final BerylliumHttpServerFactory.ServerConfig sc = BerylliumHttpServerFactory.newServerConfig();
		sc.maxThreads = CShell.MaxThreads;
		sc.minThreads = CShell.MinThreads;
		sc.gracefulShutdownMs = CSpace.HttpGracefulShutdown.intMsSigned();
		final BerylliumHttpConnectorType cxType = kc.cfgSpace.getServiceConnectorType();
		final BerylliumHttpServerFactory.ConnectorConfig cc = BerylliumHttpServerFactory.newConnectorConfig(listenPort, cxType);
		final Server server = BerylliumHttpServerFactory.newServer(sc, cc);
		final ShellGlobal global = new ShellGlobal(kc);
		final ShellHandler handler = new ShellHandler(kc, global);
		server.setHandler(handler);
		return new SpaceShell(kc, server, global);
	}

	public boolean awaitShutdown(long msAwait)
			throws InterruptedException {
		return m_global.awaitShutdown(msAwait);
	}

	@Override
	public String name() {
		return "SpaceShell";
	}

	@Override
	public void serviceEnd() {
		try {
			m_server.stop();
		} catch (final Exception ex) {
			final Ds ds = Ds.triedTo("Stop shell web server", ex, "Potential resource leak");
			ds.a("shellPort", kc.id.shellListenPort());
			kc.probe.warnNet(ds);
		}
	}

	@Override
	public void serviceStart(ExecutorService xc)
			throws ArgonPlatformException, InterruptedException {
		try {
			m_server.start();
		} catch (final Exception ex) {
			final int port = kc.id.shellListenPort();
			final Ds ds = Ds.triedTo("Start shell web server", ex, ArgonPlatformException.class);
			ds.a("shellPort", port);
			kc.probe.failNet(ds);
			final String m = "Shell web server failed to start on port " + port;
			throw new ArgonPlatformException(m);
		}
	}

	private SpaceShell(KernelCfg kc, Server server, ShellGlobal global) {
		assert kc != null;
		assert server != null;
		this.kc = kc;
		m_server = server;
		m_global = global;
	}
	final KernelCfg kc;
	private final Server m_server;

	private final ShellGlobal m_global;
}
