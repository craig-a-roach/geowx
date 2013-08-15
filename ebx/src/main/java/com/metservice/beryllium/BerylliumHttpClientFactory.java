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

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;

import com.metservice.argon.CArgon;
import com.metservice.argon.management.ArgonSpaceThreadFactory;
import com.metservice.argon.net.ArgonPlatform;

/**
 * @author roach
 */
public class BerylliumHttpClientFactory {

	public static HttpClient newClient(Config cfg) {
		if (cfg == null) throw new IllegalArgumentException("object is null");
		final HttpClient neo = new HttpClient();
		switch (cfg.connectorType) {
			case BLOCKING:
				neo.setConnectorType(HttpClient.CONNECTOR_SOCKET);
			break;
			case SELECTOR:
				neo.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
			break;
			case PLATFORM:
			default:
				if (ArgonPlatform.isOsWindows()) {
					neo.setConnectorType(HttpClient.CONNECTOR_SOCKET);
				} else {
					neo.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
				}
		}
		neo.setMaxRetries(cfg.maxRetries);
		neo.setMaxRedirects(cfg.maxRedirects);
		neo.setMaxConnectionsPerAddress(cfg.maxConnectionsPerAddress);
		neo.setConnectTimeout(cfg.connectTimeoutMs);
		neo.setTimeout(cfg.timeoutMs);
		neo.setRequestBufferSize(cfg.requestBufferSize);
		neo.setResponseBufferSize(cfg.responseBufferSize);
		final BlockingQueue<Runnable> wq = new LinkedBlockingQueue<Runnable>();
		final ThreadFactory tf = new ArgonSpaceThreadFactory("httpClient", cfg.zccThreadPrefix);
		final int corePool = Math.max(4, cfg.minThreads);
		final int maxPool = Math.min(1024, cfg.maxThreads);
		final ThreadPoolExecutor tpx = new ThreadPoolExecutor(corePool, maxPool, 60, TimeUnit.SECONDS, wq, tf);
		final ExecutorThreadPool xtp = new ExecutorThreadPool(tpx);
		neo.setThreadPool(xtp);
		return neo;
	}

	public static Config newConfig(String zccThreadPrefix) {
		if (zccThreadPrefix == null) throw new IllegalArgumentException("object is null");
		return new Config(zccThreadPrefix, BerylliumHttpConnectorType.PLATFORM);
	}

	public static Config newConfig(String zccThreadPrefix, BerylliumHttpConnectorType connectorType) {
		if (zccThreadPrefix == null) throw new IllegalArgumentException("object is null");
		if (connectorType == null) throw new IllegalArgumentException("object is null");
		return new Config(zccThreadPrefix, connectorType);
	}

	private BerylliumHttpClientFactory() {
	}

	public static class Config {

		Config(String zccThreadPrefix, BerylliumHttpConnectorType connectorType) {
			if (connectorType == null) throw new IllegalArgumentException("object is null");
			this.zccThreadPrefix = zccThreadPrefix;
			this.connectorType = connectorType;
		}
		public final String zccThreadPrefix;
		public final BerylliumHttpConnectorType connectorType;
		public int minThreads = 4;
		public int maxThreads = 64;
		public int maxRetries = 3;
		public int maxRedirects = 5;
		public int maxConnectionsPerAddress = 1024;
		public int connectTimeoutMs = 45 * CArgon.SEC_TO_MS;
		public int timeoutMs = 320 * CArgon.SEC_TO_MS;
		public int requestBufferSize = 8 * CArgon.K;
		public int responseBufferSize = 12 * CArgon.K;
	}
}
