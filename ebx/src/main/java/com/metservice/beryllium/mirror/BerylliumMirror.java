/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.ArgonRunnable;
import com.metservice.argon.ArgonServiceId;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonRunProbe;
import com.metservice.beryllium.BerylliumHttpClientFactory;
import com.metservice.beryllium.BerylliumHttpLogger;
import com.metservice.beryllium.BerylliumHttpPlatformException;
import com.metservice.beryllium.BerylliumHttpServerFactory;
import com.metservice.beryllium.BerylliumPath;

/**
 * @author roach
 */
public class BerylliumMirror {

	private static final String CsqLeak = "Potential resource leak";

	private static HttpClient newClient(MirrorCfg cfg) {
		assert cfg != null;
		final BerylliumHttpClientFactory.Config cc = BerylliumHttpClientFactory.newConfig(cfg.spaceId.format());
		cc.connectTimeoutMs = cfg.connectTimeoutMs;
		cc.timeoutMs = cfg.timeoutMs;
		cc.requestBufferSize = cfg.requestBufferSize;
		cc.responseBufferSize = cfg.responseBufferSize;
		return BerylliumHttpClientFactory.newClient(cc);
	}

	private static Server newServer(MirrorCfg cfg) {
		assert cfg != null;
		final BerylliumHttpServerFactory.ServerConfig sc = BerylliumHttpServerFactory.newServerConfig();
		final BerylliumHttpServerFactory.ConnectorConfig xc = BerylliumHttpServerFactory.newConnectorConfig(cfg.listenPort);
		xc.requestBufferSize = cfg.requestBufferSize;
		xc.responseBufferSize = cfg.responseBufferSize;
		return BerylliumHttpServerFactory.newServer(sc, xc);
	}

	public static Config newConfig(ArgonServiceId sid, String qnctwPeerHost, int listenPort) {
		if (sid == null) throw new IllegalArgumentException("object is null");
		if (qnctwPeerHost == null || qnctwPeerHost.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		return new Config(sid, listenPort, qnctwPeerHost, listenPort);
	}

	public static BerylliumMirror newInstance(IBerylliumMirrorProbe probe, IBerylliumMirrorProvider provider, Config srccfg) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (provider == null) throw new IllegalArgumentException("object is null");
		if (srccfg == null) throw new IllegalArgumentException("object is null");
		final MirrorCfg cfg = new MirrorCfg(srccfg);
		BerylliumHttpLogger.install(cfg.serviceId, cfg.spaceId, srccfg.enableDebug);
		final HttpClient httpClient = newClient(cfg);
		final Server httpServer = newServer(cfg);
		final BerylliumMirror neo = new BerylliumMirror(probe, provider, cfg, httpClient, httpServer);
		return neo;
	}

	private void httpClientEnd() {
		try {
			m_httpClient.stop();
		} catch (final Exception ex) {
			final Ds ds = Ds.triedTo("Stop mirror web client", ex, CsqLeak);
			ds.a("mirrorConfig", cfg);
			probe.warnNet(ds);
		}
	}

	private void httpClientStart()
			throws BerylliumHttpPlatformException {
		try {
			m_httpClient.start();
		} catch (final Exception ex) {
			final Ds ds = Ds.triedTo("Start mirror web client", ex, BerylliumHttpPlatformException.class);
			ds.a("mirrorConfig", cfg);
			probe.failNet(ds);
			throw new BerylliumHttpPlatformException(ds.s());
		}
	}

	private void httpServerEnd() {
		try {
			m_httpServer.stop();
		} catch (final Exception ex) {
			final Ds ds = Ds.triedTo("Stop mirror web server", ex, CsqLeak);
			ds.a("config", cfg);
			probe.warnNet(ds);
		}
	}

	private void httpServerStart()
			throws BerylliumHttpPlatformException {
		try {
			m_httpServer.start();
		} catch (final Exception ex) {
			final Ds ds = Ds.triedTo("Start mirror web server", ex, BerylliumHttpPlatformException.class);
			ds.a("config", cfg);
			probe.failNet(ds);
			throw new BerylliumHttpPlatformException(ds.s());
		}
	}

	void discover() {
		m_engine.discover();
	}

	void handleRequest(BerylliumPath path, Request rq, HttpServletResponse rp)
			throws IOException, ServletException {
		m_engine.handleRequest(path, rq, rp);
	}

	void handleTasks()
			throws InterruptedException {
		m_engine.handleTasks();
	}

	public void push(IBerylliumMirrorTask task)
			throws InterruptedException {
		m_engine.push(task);
	}

	public void shutdown() {
		m_worker.requestEndInterrupt();
		m_worker.awaitEnd();
		httpClientEnd();
		httpServerEnd();
	}

	public void start(ExecutorService xc)
			throws BerylliumHttpPlatformException, InterruptedException {
		httpServerStart();
		httpClientStart();
		xc.execute(m_worker);
		m_worker.awaitStartup();
	}

	private BerylliumMirror(IBerylliumMirrorProbe probe, IBerylliumMirrorProvider provider, MirrorCfg cfg, HttpClient httpClient,
			Server httpServer) {
		assert probe != null;
		assert provider != null;
		assert cfg != null;
		assert httpClient != null;
		assert httpServer != null;
		this.probe = probe;
		this.provider = provider;
		this.cfg = cfg;
		m_httpClient = httpClient;
		m_httpServer = httpServer;
		m_handler = new MirrorHandler();
		httpServer.setHandler(m_handler);
		m_worker = new Worker(probe, cfg.discoverIntervalMs);
		m_engine = new Engine(probe, provider, cfg, httpClient);
	}
	final IBerylliumMirrorProbe probe;
	final IBerylliumMirrorProvider provider;
	final MirrorCfg cfg;
	private final HttpClient m_httpClient;
	private final Server m_httpServer;
	private final MirrorHandler m_handler;
	private final Worker m_worker;
	private final Engine m_engine;

	private class MirrorHandler extends AbstractHandler {

		@Override
		public void handle(String target, Request rq, HttpServletRequest sr, HttpServletResponse rp)
				throws IOException, ServletException {
			rq.setHandled(true);
			final BerylliumPath path = BerylliumPath.newInstance(rq);
			handleRequest(path, rq, rp);
		}

		public MirrorHandler() {
		}
	}

	private class Worker extends ArgonRunnable {

		private void checkDiscovery() {
			final long tsNextDiscoverDue = m_tsNextDiscoverDue.get();
			if (tsNextDiscoverDue <= ArgonClock.tsNow()) {
				discover();
				m_tsNextDiscoverDue.set(ArgonClock.tsNow() + m_msDiscoverInterval);
			}
		}

		@Override
		protected IArgonRunProbe getRunProbe() {
			return m_probe;
		}

		@Override
		public void runImp()
				throws InterruptedException {
			while (keepRunning()) {
				handleTasks();
				checkDiscovery();
				yield();
			}
		}

		Worker(IBerylliumMirrorProbe probe, long msDiscoverInterval) {
			if (probe == null) throw new IllegalArgumentException("object is null");
			m_probe = probe;
			m_msDiscoverInterval = msDiscoverInterval;
			m_tsNextDiscoverDue = new AtomicLong(ArgonClock.tsNow());
		}
		private final IBerylliumMirrorProbe m_probe;
		private final long m_msDiscoverInterval;
		private final AtomicLong m_tsNextDiscoverDue;
	}

	public static class Config {

		Config(ArgonServiceId sid, int listenPort, String qnctwPeerHost, int peerPort) {
			if (sid == null) throw new IllegalArgumentException("object is null");
			if (qnctwPeerHost == null || qnctwPeerHost.length() == 0)
				throw new IllegalArgumentException("string is null or empty");
			this.serviceId = sid;
			this.listenPort = listenPort;
			this.qnctwPeerHost = qnctwPeerHost;
			this.peerPort = peerPort;
		}
		public final ArgonServiceId serviceId;
		public final int listenPort;
		public String qnctwPeerHost;
		public final int peerPort;
		public int mirrorQueryTimeoutMs = 10 * CArgon.MIN_TO_MS;
		public int discoverIntervalMs = 15 * CArgon.MIN_TO_MS;
		public int connectTimeoutMs = 180 * CArgon.SEC_TO_MS;
		public int timeoutMs = 620 * CArgon.SEC_TO_MS;
		public int minRetryIntervalMs = 15 * CArgon.SEC_TO_MS;
		public int requestBufferSize = 16 * CArgon.K;
		public int responseBufferSize = 8 * CArgon.K;
		public boolean enableDebug = false;
		public int payloadQuotaBc = 1024 * CArgon.M;
	}
}
