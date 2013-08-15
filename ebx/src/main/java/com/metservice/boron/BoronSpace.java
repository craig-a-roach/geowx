/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.management.NotificationBroadcasterSupport;

import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public class BoronSpace extends NotificationBroadcasterSupport implements BoronSpaceMBean {

	List<BoronFeedUnit> putProcessFeeds(BoronProcessId processId, List<BoronFeedUnit> zlFeeds, Elapsed oTimeout)
			throws BoronApiException, InterruptedException {
		m_rw.readLock().lock();
		try {
			m_ka.verifyUp("put feeds");
			return m_kernel.processFeedsPut(processId, zlFeeds, oTimeout);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	void removeProcess(BoronProcessId processId) {
		m_rw.readLock().lock();
		try {
			m_kernel.removeEngine(processId);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	IBoronProduct takeProcessProduct(BoronProcessId processId, Elapsed oTimeout)
			throws BoronApiException, InterruptedException {
		m_rw.readLock().lock();
		try {
			m_ka.verifyUp("take product");
			return m_kernel.processProductTake(processId, oTimeout);
		} finally {
			m_rw.readLock().unlock();
		}
	}

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

	public File cndirProcess(BoronProcessId processId)
			throws BoronApiException {
		m_rw.readLock().lock();
		try {
			return m_kernel.cndirProcess(processId);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public String emit(BoronDiagnosticScript script)
			throws InterruptedException {
		if (script == null) throw new IllegalArgumentException("object is null");
		try {
			final BoronProductIterator biterator = newProcessProductIterator(script);
			return UBoron.consume(biterator, script.productTimeout());
		} catch (final BoronApiException ex) {
			return UBoron.diagnostic(ex);
		} catch (final BoronImpException ex) {
			return UBoron.diagnostic(ex);
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

	public boolean isWinOS() {
		m_rw.readLock().lock();
		try {
			return m_cfg.isWinOS();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public BoronProductIterator newProcessProductIterator(IBoronScript script)
			throws BoronApiException, BoronImpException, InterruptedException {
		if (script == null) throw new IllegalArgumentException("object is null");
		m_rw.readLock().lock();
		try {
			m_ka.verifyUpNewTran("new process product iterator");
			final ProcessEngine processEngine = m_kernel.newProcessEngine(script);
			final BoronProcessId processId = processEngine.processId();
			return new BoronProductIterator(this, processId);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String requestRestart() {
		m_rw.writeLock().lock();
		try {
			m_kernel.serviceCooldown();
			m_kernel = m_kernel.serviceRestart(m_spaceProbe, m_cfg);
			return "restarted";
		} catch (final BoronException ex) {
			return ex.getMessage();
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
			return "Interrupted";
		} finally {
			m_rw.writeLock().unlock();
		}
	}

	public boolean restart(boolean cooldown)
			throws BoronPlatformException, BoronApiException, BoronImpException {
		boolean done = false;
		m_rw.writeLock().lock();
		try {
			if (cooldown) {
				m_kernel.serviceCooldown();
			}
			m_kernel = m_kernel.serviceRestart(m_spaceProbe, m_cfg);
			done = true;
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} finally {
			m_rw.writeLock().unlock();
		}
		return done;
	}

	public boolean restart(BoronSpaceCfg neoConfig)
			throws BoronPlatformException, BoronApiException, BoronImpException {
		boolean done = false;
		m_rw.writeLock().lock();
		try {
			m_kernel.serviceCooldown();
			m_cfg = neoConfig;
			m_spaceProbe = new SpaceProbe(this, m_id, neoConfig, m_probeControl);
			m_kernel = m_kernel.serviceRestart(m_spaceProbe, neoConfig);
			done = true;
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
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

	public void shutdown() {
		m_rw.readLock().lock();
		try {
			m_ka.onCooling();
			m_kernel.serviceCooldown();
			m_ka.onEnding();
			m_kernel.serviceEnd();
			m_ka.onDown();
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} finally {
			m_rw.readLock().unlock();
		}
		m_probeControl.jmxUnregister(m_id);
	}

	public void start()
			throws BoronApiException {
		m_ka.onStarting();
		m_rw.readLock().lock();
		try {
			m_kernel.serviceStart();
			m_ka.onUp();
		} catch (final InterruptedException ex) {
			Thread.currentThread().interrupt();
		} finally {
			m_rw.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		return m_ka.toString();
	}

	public boolean waitNotBusy(long msMaxWait)
			throws InterruptedException {
		m_rw.readLock().lock();
		try {
			return m_kernel.waitNotBusy(msMaxWait);
		} finally {
			m_rw.readLock().unlock();
		}
	}

	public BoronSpace(BoronSpaceId id) throws BoronPlatformException, BoronApiException, BoronImpException {
		this(id, new BoronSpaceCfg(), new BoronDefaultProbeControl());
	}

	public BoronSpace(BoronSpaceId id, BoronSpaceCfg cfg) throws BoronPlatformException, BoronApiException, BoronImpException {
		this(id, cfg, new BoronDefaultProbeControl());
	}

	public BoronSpace(BoronSpaceId id, BoronSpaceCfg cfg, IBoronProbeControl probeControl) throws BoronPlatformException,
			BoronApiException, BoronImpException {
		if (id == null) throw new IllegalArgumentException("object is null");
		if (cfg == null) throw new IllegalArgumentException("object is null");
		if (probeControl == null) throw new IllegalArgumentException("object is null");
		m_id = id;
		m_cfg = cfg;
		m_probeControl = probeControl;
		m_ka = new KernelAccessor(id);
		m_spaceProbe = new SpaceProbe(this, id, cfg, probeControl);
		probeControl.jmxRegister(this, id);
		final KernelCfg kc = new KernelCfg(m_spaceProbe, id, cfg);
		m_kernel = Kernel.newInstance(kc);
	}

	private final BoronSpaceId m_id;
	private final ReadWriteLock m_rw = new ReentrantReadWriteLock();
	private final IBoronProbeControl m_probeControl;
	private final KernelAccessor m_ka;

	private BoronSpaceCfg m_cfg;
	private SpaceProbe m_spaceProbe;
	private Kernel m_kernel;
}
