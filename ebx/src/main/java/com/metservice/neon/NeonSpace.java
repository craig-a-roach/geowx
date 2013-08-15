/*
 * Copyright 2009 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.management.InstanceAlreadyExistsException;
import javax.management.NotificationBroadcasterSupport;

import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
public class NeonSpace extends NotificationBroadcasterSupport implements NeonSpaceMBean {

	@Override
	public String alterFilterConsole(String filterPattern) {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.alterFilterConsole(filterPattern);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String alterFilterJmx(String filterPattern) {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.alterFilterJmx(filterPattern);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String alterFilterLog(String filterPattern) {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.alterFilterLog(filterPattern);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public void awaitShutdownRequest()
			throws InterruptedException {
		boolean wait = true;
		while (wait) {
			wait = !isShutdownInProgress();
			if (wait) {
				Thread.sleep(1000L);
			}
		}
	}

	@Override
	public String getConfigInfo() {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.getConfigInfo();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String getFilterPatternConsole() {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.getFilterPatternConsole();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String getFilterPatternJmx() {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.getFilterPatternJmx();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String getFilterPatternLog() {
		m_rw.readLock().lock();
		try {
			return m_spaceProbe.getFilterPatternLog();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String getIdInfo() {
		return m_id.toString();
	}

	public NeonSpaceId id() {
		return m_id;
	}

	public boolean isShutdownInProgress() {
		m_rw.readLock().lock();
		try {
			return m_kernel.isShutdownInProgress();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public NeonAssuranceReport newAssuranceReport() {
		m_rw.readLock().lock();
		try {
			return m_kernel.newAssuranceReport();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public File ocndirSourceProviderHome() {
		return m_sourceProvider.ocndirHome();
	}

	public void register(NeonAssuranceContext oContext) {
		m_rw.readLock().lock();
		try {
			m_kernel.register(oContext);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String requestRestart() {
		m_rw.writeLock().lock();
		try {
			m_kernel = m_kernel.serviceRestart(m_spaceProbe, m_sourceProvider, m_cfg);
			return "restarted";
		} catch (final NeonPlatformException ex) {
			return ex.getMessage();
		} finally {
			m_rw.writeLock().unlock();
		}
	}

	public boolean restart()
			throws NeonPlatformException {
		boolean done = false;
		m_rw.writeLock().lock();
		try {
			m_kernel = m_kernel.serviceRestart(m_spaceProbe, m_sourceProvider, m_cfg);
			done = true;
		} finally {
			m_rw.writeLock().unlock();
		}
		return done;
	}

	public boolean restart(INeonSourceProvider neoSourceProvider, NeonSpaceCfg neoConfig)
			throws NeonPlatformException {
		if (neoConfig == null) throw new IllegalArgumentException("object is null");
		boolean done = false;
		m_rw.writeLock().lock();
		try {
			m_cfg = neoConfig;
			m_spaceProbe = new SpaceProbe(this, m_id, neoConfig, m_probeControl);
			m_kernel = m_kernel.serviceRestart(m_spaceProbe, neoSourceProvider, neoConfig);
			done = true;
		} finally {
			m_rw.writeLock().unlock();
		}
		return done;
	}

	@Override
	public void restoreFilterConsole() {
		m_rw.readLock().lock();
		try {
			m_spaceProbe.restoreFilterConsole();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public void restoreFilterJmx() {
		m_rw.readLock().lock();
		try {
			m_spaceProbe.restoreFilterJmx();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public void restoreFilterLog() {
		m_rw.readLock().lock();
		try {
			m_spaceProbe.restoreFilterLog();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public void run(EsRequest request, EsResponse response)
			throws NeonScriptException, NeonImpException, InterruptedException {
		m_rw.readLock().lock();
		try {
			m_kernel.run(request, response);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public void shutdown() {
		m_rw.readLock().lock();
		try {
			m_kernel.serviceEnd();
		} finally {
			m_rw.readLock().unlock();
		}
		m_probeControl.jmxUnregister(m_id);
	}

	public void start()
			throws NeonPlatformException {
		m_rw.readLock().lock();
		try {
			m_kernel.serviceStart();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		return m_id.toString();
	}

	public List<BerylliumSupportId> zlDebugSupportIdsAsc() {
		m_rw.readLock().lock();
		try {
			return m_kernel.zlDebugSupportIdsAsc();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public List<BerylliumSupportId> zlProfileSupportIdsAsc() {
		m_rw.readLock().lock();
		try {
			return m_kernel.zlProfileSupportIdsAsc();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public NeonSpace(NeonSpaceId id, INeonSourceProvider sourceProvider, NeonSpaceCfg cfg) throws InstanceAlreadyExistsException {
		this(id, sourceProvider, cfg, new NeonDefaultProbeControl());
	}

	public NeonSpace(NeonSpaceId id, INeonSourceProvider sourceProvider, NeonSpaceCfg cfg, INeonProbeControl probeControl)
			throws InstanceAlreadyExistsException {
		if (id == null) throw new IllegalArgumentException("object is null");
		if (sourceProvider == null) throw new IllegalArgumentException("object is null");
		if (cfg == null) throw new IllegalArgumentException("object is null");
		if (probeControl == null) throw new IllegalArgumentException("object is null");
		m_id = id;
		m_sourceProvider = sourceProvider;
		m_cfg = cfg;
		m_probeControl = probeControl;
		m_spaceProbe = new SpaceProbe(this, id, cfg, probeControl);
		sourceProvider.registerProbe(m_spaceProbe);
		probeControl.jmxRegister(this, id);
		final KernelCfg kc = new KernelCfg(m_spaceProbe, id, sourceProvider, cfg);
		m_kernel = Kernel.newInstance(kc);
		m_kernel.selfRegister(this);
	}
	private final NeonSpaceId m_id;
	private final ReadWriteLock m_rw = new ReentrantReadWriteLock();
	private final INeonProbeControl m_probeControl;
	private final INeonSourceProvider m_sourceProvider;
	private NeonSpaceCfg m_cfg;
	private SpaceProbe m_spaceProbe;
	private Kernel m_kernel;
}
