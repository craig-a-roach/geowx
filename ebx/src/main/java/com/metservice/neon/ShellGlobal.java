/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author roach
 */
class ShellGlobal {

	public boolean isProcess() {
		return kc.cfg.getShellProcess();
	}

	public boolean isShutdownInProgress() {
		return m_shutdownInProgress.get();
	}

	public void notifyShutdown() {
		m_shutdownInProgress.set(true);
		assurance.notifyShutdown();
	}

	public ShellGlobal(KernelCfg kc, NeonSourceLoader sl, NeonConsole con, NeonDebugger dbg, NeonProfiler prf, NeonAssurance asr) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		if (sl == null) throw new IllegalArgumentException("object is null");
		if (con == null) throw new IllegalArgumentException("object is null");
		if (dbg == null) throw new IllegalArgumentException("object is null");
		if (prf == null) throw new IllegalArgumentException("object is null");
		this.kc = kc;
		this.sourceLoader = sl;
		this.console = con;
		this.debugger = dbg;
		this.profiler = prf;
		this.assurance = asr;
	}

	public final KernelCfg kc;
	public final NeonSourceLoader sourceLoader;
	public final NeonConsole console;
	public final NeonDebugger debugger;
	public final NeonProfiler profiler;
	public final NeonAssurance assurance;
	private final AtomicBoolean m_shutdownInProgress = new AtomicBoolean(false);
}
