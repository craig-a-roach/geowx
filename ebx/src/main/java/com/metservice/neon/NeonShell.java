/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import org.eclipse.jetty.server.Server;

import com.metservice.argon.Ds;
import com.metservice.beryllium.BerylliumHttpConnectorType;
import com.metservice.beryllium.BerylliumHttpLogger;
import com.metservice.beryllium.BerylliumHttpServerFactory;
import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
public class NeonShell {

	static NeonShell newInstance(KernelCfg kc, NeonSourceLoader sl) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		if (sl == null) throw new IllegalArgumentException("object is null");
		final int listenPort = kc.id.listenPort();
		final boolean isShellProcess = kc.cfg.getShellProcess();
		if (isShellProcess) {
			BerylliumHttpLogger.install(CNeon.ServiceId, kc.id, false);
		}
		final BerylliumHttpServerFactory.ServerConfig sc = BerylliumHttpServerFactory.newServerConfig();
		sc.maxThreads = CNeonShell.MaxThreads;
		sc.minThreads = CNeonShell.MinThreads;
		sc.gracefulShutdownMs = CNeonShell.GracefulShutdownMs;
		final BerylliumHttpConnectorType cxType = kc.cfg.getShellSessionConnectorType();
		final BerylliumHttpServerFactory.ConnectorConfig cc = BerylliumHttpServerFactory.newConnectorConfig(listenPort, cxType);
		final Server server = BerylliumHttpServerFactory.newServer(sc, cc);
		final NeonConsole console = new NeonConsole(kc);
		final NeonDebugger debugger = new NeonDebugger(kc);
		final NeonProfiler profiler = new NeonProfiler();
		final NeonAssurance assurance = new NeonAssurance(kc);
		final NeonShellHandler handler = new NeonShellHandler(kc, sl, console, debugger, profiler, assurance);
		server.setHandler(handler);
		final ShellReaper reaper = new ShellReaper(kc, handler);
		return new NeonShell(kc, sl, server, console, debugger, profiler, assurance, handler, reaper);
	}

	public NeonAssurance assurance() {
		return m_assurance;
	}

	public NeonConsole console() {
		return m_console;
	}

	public NeonDebugger debugger() {
		return m_debugger;
	}

	public void emit(String qccSourcePath, EmitType type, String message) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (type == null) throw new IllegalArgumentException("object is null");
		if (message == null) throw new IllegalArgumentException("object is null");
		switch (type) {
			case Trace:
				kc.probe.liveScriptEmit(qccSourcePath, message);
			break;
			case Fail:
				kc.probe.failScriptEmit(qccSourcePath, message);
			break;
		}
		m_console.scriptEmit(qccSourcePath, type, message);
	}

	public boolean isShutdownInProgress() {
		return m_handler.isShutdownInProgress();
	}

	public ShellHook newHook(EsRequest request, EsSource source) {
		if (request == null) throw new IllegalArgumentException("object is null");
		if (source == null) throw new IllegalArgumentException("object is null");
		final BerylliumSupportId sid = request.idSupport();
		final String qccSourcePath = source.qccPath();
		m_debugger.attach(sid, qccSourcePath);
		final boolean enabledDebugging = m_debugger.isEnabled(sid, qccSourcePath);
		final boolean enabledProfiling = m_profiler.isEnabled(sid, qccSourcePath);
		final boolean requireMarkup = enabledDebugging || enabledProfiling;
		final EsSourceHtml oHtml = requireMarkup ? source.createSourceHtml() : null;
		final ProfileSample oPrf = enabledProfiling ? ProfileSample.newInstance(source, oHtml) : null;
		return new ShellHook(this, request, source, enabledDebugging, oPrf, oHtml);
	}

	public NeonProfiler profiler() {
		return m_profiler;
	}

	public void serviceEnd() {
		try {
			m_assurance.end();
			m_reaper.end();
			m_server.stop();
		} catch (final Exception ex) {
			final Ds ds = Ds.triedTo("Stop shell web server", ex, "Potential resource leak");
			ds.a("listenPort", kc.id.listenPort());
			kc.probe.warnNet(ds);
		}
	}

	public void serviceStart()
			throws NeonPlatformException {
		try {
			m_server.start();
			m_reaper.start();
		} catch (final Exception ex) {
			final Ds ds = Ds.triedTo("Start shell web server", ex, "Debugging not available");
			ds.a("listenPort", kc.id.listenPort());
			kc.probe.failNet(ds);
			throw new NeonPlatformException(ds);
		}
	}

	public NeonSourceLoader sourceLoader() {
		return m_sourceLoader;
	}

	private NeonShell(KernelCfg kc, NeonSourceLoader sl, Server srv, NeonConsole con, NeonDebugger dbg, NeonProfiler prf,
			NeonAssurance asr, NeonShellHandler sh, ShellReaper sr) {
		assert kc != null;
		assert sl != null;
		assert srv != null;
		assert con != null;
		assert dbg != null;
		assert prf != null;
		assert asr != null;
		assert sh != null;
		assert sr != null;
		this.kc = kc;
		m_sourceLoader = sl;
		m_server = srv;
		m_console = con;
		m_debugger = dbg;
		m_profiler = prf;
		m_assurance = asr;
		m_handler = sh;
		m_reaper = sr;
	}
	final KernelCfg kc;
	private final NeonSourceLoader m_sourceLoader;
	private final Server m_server;
	private final NeonConsole m_console;
	private final NeonDebugger m_debugger;
	private final NeonProfiler m_profiler;
	private final NeonAssurance m_assurance;
	private final NeonShellHandler m_handler;
	private final ShellReaper m_reaper;

	public static enum EmitType {
		Trace, Fail
	}
}
