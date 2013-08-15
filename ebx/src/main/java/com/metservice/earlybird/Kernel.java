/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.earlybird;

import com.metservice.argon.ArgonPlatformException;
import com.metservice.argon.management.ArgonServiceSuite;
import com.metservice.argon.management.ArgonSpaceThreadFactory;
import com.metservice.beryllium.BerylliumHttpLogger;

/**
 * @author roach
 */
class Kernel {

	public static Kernel newInstance(KernelCfg kc)
			throws ArgonPlatformException {
		BerylliumHttpLogger.install(CSpace.ServiceId, kc.id, false);
		final ArgonSpaceThreadFactory stf = new ArgonSpaceThreadFactory(CSpace.ThreadPrefix, kc.id);
		final ArgonServiceSuite ss = ArgonServiceSuite.newCachedThreadPool(kc.probe, stf);
		final SpaceShell shell = ss.register(SpaceShell.newInstance(kc));
		ss.register(PathSensor.newInstance(kc));
		ss.register(KmlService.newInstance(kc));
		return new Kernel(kc, ss, shell);
	}

	public boolean awaitShutdown(long msAwait)
			throws InterruptedException {
		return m_shell.awaitShutdown(msAwait);
	}

	public void serviceEnd()
			throws InterruptedException {
		m_serviceSuite.endServices(CSpace.ExecutorShutdownAwait);
	}

	public Kernel serviceRestart(ISpaceProbe neoProbe, SpaceCfg neoCfgS, PathSensorCfg neoCfgPs)
			throws ArgonPlatformException, InterruptedException {
		if (neoProbe == null) throw new IllegalArgumentException("object is null");
		if (neoCfgS == null) throw new IllegalArgumentException("object is null");
		if (neoCfgPs == null) throw new IllegalArgumentException("object is null");
		serviceEnd();
		final KernelCfg neoKernelCfg = new KernelCfg(neoProbe, m_kc.id, neoCfgS, neoCfgPs);
		final Kernel neo = newInstance(neoKernelCfg);
		neo.serviceStart();
		return neo;
	}

	public void serviceStart()
			throws ArgonPlatformException, InterruptedException {
		m_serviceSuite.startServices();
	}

	private Kernel(KernelCfg kc, ArgonServiceSuite ss, SpaceShell shell) {
		assert kc != null;
		assert ss != null;
		assert shell != null;
		m_kc = kc;
		m_serviceSuite = ss;
		m_shell = shell;
	}

	private final KernelCfg m_kc;
	private final ArgonServiceSuite m_serviceSuite;
	private final SpaceShell m_shell;
}
