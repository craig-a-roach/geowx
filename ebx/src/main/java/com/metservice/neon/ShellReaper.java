/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.Timer;
import java.util.TimerTask;

import com.metservice.argon.CArgon;

/**
 * @author roach
 */
class ShellReaper {

	public void end() {
		m_timer.cancel();
	}

	public void start() {
		final int msMaxIdle = kc.cfg.getShellSessionMaxIdleSec() * CArgon.SEC_TO_MS;
		final ReapTask task = new ReapTask(m_shellHandler, msMaxIdle);
		m_timer.scheduleAtFixedRate(task, msMaxIdle, CNeonShell.ReapingIntervalMs);
	}

	public ShellReaper(KernelCfg kc, NeonShellHandler shellHandler) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		if (shellHandler == null) throw new IllegalArgumentException("object is null");
		this.kc = kc;
		m_shellHandler = shellHandler;
		final String qReaperThread = "neon-reaper-" + kc.id.listenPort();
		m_timer = new Timer(qReaperThread, true);
	}

	final KernelCfg kc;
	private final NeonShellHandler m_shellHandler;
	private final Timer m_timer;

	private static class ReapTask extends TimerTask {

		@Override
		public void run() {
			m_shellHandler.reapIdleSessions(m_msMaxIdle);
		}

		public ReapTask(NeonShellHandler shellHandler, long msMaxIdle) {
			m_shellHandler = shellHandler;
			m_msMaxIdle = msMaxIdle;
		}

		private final NeonShellHandler m_shellHandler;
		private final long m_msMaxIdle;
	}
}
