/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;

import com.metservice.argon.CArgon;
import com.metservice.argon.management.ArgonSpaceThreadFactory;
import com.metservice.argon.net.ArgonPlatform;
import com.metservice.argon.text.ArgonNumber;

/**
 * @author roach
 */
public class BerylliumHttpServerFactory {

	public static ConnectorConfig newConnectorConfig(int listenPort) {
		return newConnectorConfig(listenPort, BerylliumHttpConnectorType.PLATFORM);
	}

	public static ConnectorConfig newConnectorConfig(int listenPort, BerylliumHttpConnectorType connectorType) {
		if (connectorType == null) throw new IllegalArgumentException("object is null");
		return new ConnectorConfig(listenPort, connectorType);
	}

	public static Server newServer(ServerConfig serverCfg, ConnectorConfig... zptConnectorCfg) {
		if (serverCfg == null) throw new IllegalArgumentException("object is null");
		if (zptConnectorCfg == null) throw new IllegalArgumentException("object is null");

		final Server neo = new Server();
		neo.setStopAtShutdown(true);
		neo.setGracefulShutdown(serverCfg.gracefulShutdownMs);

		String zccThreadPrefix = "";
		if (zptConnectorCfg.length == 1) {
			zccThreadPrefix = ArgonNumber.intToDec5(zptConnectorCfg[0].listenPort);
		}

		final BlockingQueue<Runnable> wq = new LinkedBlockingQueue<Runnable>();
		final ThreadFactory tf = new ArgonSpaceThreadFactory("httpServer", zccThreadPrefix);
		final int corePool = Math.max(4, serverCfg.minThreads);
		final int maxPool = Math.max(corePool, Math.min(1024, serverCfg.maxThreads));
		final ThreadPoolExecutor tpx = new ThreadPoolExecutor(corePool, maxPool, 60, TimeUnit.SECONDS, wq, tf);
		final ExecutorThreadPool xtp = new ExecutorThreadPool(tpx);
		neo.setThreadPool(xtp);
		for (int i = 0; i < zptConnectorCfg.length; i++) {
			final ConnectorConfig cc = zptConnectorCfg[i];
			final AbstractConnector cx;
			switch (cc.connectorType) {
				case BLOCKING:
					cx = new SocketConnector();
				break;
				case SELECTOR:
					cx = new SelectChannelConnector();
				break;
				case PLATFORM:
				default:
					if (ArgonPlatform.isOsWindows()) {
						cx = new SocketConnector();
					} else {
						cx = new SelectChannelConnector();
					}
			}
			cx.setPort(cc.listenPort);
			cx.setReuseAddress(cc.reuseAddress);
			cx.setMaxIdleTime(cc.maxIdleTimeMs);
			cx.setRequestBufferSize(cc.requestBufferSize);
			cx.setResponseBufferSize(cc.responseBufferSize);
			neo.addConnector(cx);
		}
		return neo;
	}

	public static ServerConfig newServerConfig() {
		return new ServerConfig();
	}

	private BerylliumHttpServerFactory() {
	}

	public static class ConnectorConfig {

		ConnectorConfig(int listenPort, BerylliumHttpConnectorType connectorType) {
			if (connectorType == null) throw new IllegalArgumentException("object is null");
			this.listenPort = listenPort;
			this.connectorType = connectorType;
		}
		public final int listenPort;
		public final BerylliumHttpConnectorType connectorType;
		public boolean reuseAddress = true;
		public int maxIdleTimeMs = 200 * CArgon.SEC_TO_MS;
		public int requestBufferSize = 8 * CArgon.K;
		public int responseBufferSize = 12 * CArgon.K;
	}

	public static class ServerConfig {

		ServerConfig() {
		}
		public int minThreads = 4;
		public int maxThreads = 64;
		public int gracefulShutdownMs = 7 * CArgon.SEC_TO_MS;
	}
}
