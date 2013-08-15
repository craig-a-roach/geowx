/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;
import com.metservice.argon.Elapsed;
import com.metservice.argon.management.ArgonSpaceThreadFactory;

/**
 * @author roach
 */
class Kernel {

	public static Kernel newInstance(KernelCfg kc)
			throws BoronPlatformException, BoronApiException, BoronImpException {
		if (kc == null) throw new IllegalArgumentException("object is null");
		final ArgonSpaceThreadFactory stf = new ArgonSpaceThreadFactory(CBoron.ThreadPrefix, kc.id);
		final ExecutorService execService = stf.newCachedThreadPool();
		final DiskController dc = DiskController.newInstance(kc, execService);
		final ProcessMonitor pm = new ProcessMonitor(kc, execService, dc);
		return new Kernel(kc, execService, dc, pm);
	}

	public File cndirProcess(BoronProcessId processId)
			throws BoronApiException {
		return m_diskController.cndirProcess(processId);
	}

	public ProcessEngine findEngine(BoronProcessId processId) {
		return m_processMonitor.findEngine(processId);
	}

	public boolean isServiceBusy() {
		return m_processMonitor.isBusy();
	}

	public ProcessEngine newProcessEngine(IBoronScript script)
			throws BoronApiException, BoronImpException, InterruptedException {
		return m_processMonitor.newProcessEngine(script);
	}

	public List<BoronFeedUnit> processFeedsPut(BoronProcessId processId, List<BoronFeedUnit> zlFeeds, Elapsed oTimeout)
			throws InterruptedException {
		if (processId == null) throw new IllegalArgumentException("object is null");
		if (zlFeeds == null) throw new IllegalArgumentException("object is null");
		final ProcessEngine oEngine = findEngine(processId);
		if (oEngine == null) return zlFeeds;

		final FeedQueue feedQueue = oEngine.feedQueue();
		return feedQueue.put(kc, zlFeeds, oTimeout);
	}

	public IBoronProduct processProductTake(BoronProcessId processId, Elapsed oTimeout)
			throws InterruptedException {
		if (processId == null) throw new IllegalArgumentException("object is null");
		IBoronProduct product = BoronProductManagementFailure.Instance;
		boolean removeEngine = true;
		try {
			final ProcessEngine oEngine = findEngine(processId);
			if (oEngine == null) {
				product = BoronProductCancellation.Instance;
				removeEngine = false;
			} else {
				final ProductQueue productQueue = oEngine.productQueue();
				if (productQueue.isDrained()) {
					product = BoronProductCancellation.Instance;
					removeEngine = false;
				} else {
					product = productQueue.takeProduct(oTimeout);
					removeEngine = productQueue.isDrained();
				}
			}
		} catch (final RuntimeException ex) {
			final Ds ds = Ds.triedTo("Take process product off queue", ex, "Break product iteration");
			ds.a("processId", processId);
			ds.a("self", this);
			kc.probe.failSoftware(ds);
		} finally {
			if (removeEngine) {
				removeEngine(processId);
			}
		}
		return product;
	}

	public ProcessEngine removeEngine(BoronProcessId processId) {
		return m_processMonitor.removeEngine(processId);
	}

	public boolean serviceCooldown()
			throws InterruptedException {
		return waitNotBusy(kc.cfg.getCooldownSecs() * CArgon.SEC_TO_MS);
	}

	public void serviceEnd() {
		m_execService.shutdown();
		m_processMonitor.serviceEnd();
		m_diskController.serviceEnd();
	}

	public Kernel serviceRestart(ISpaceProbe neoProbe, BoronSpaceCfg neoCfg)
			throws BoronPlatformException, BoronApiException, BoronImpException, InterruptedException {
		if (neoProbe == null) throw new IllegalArgumentException("object is null");
		if (neoCfg == null) throw new IllegalArgumentException("object is null");
		serviceEnd();
		final KernelCfg neoKernelCfg = new KernelCfg(neoProbe, kc.id, neoCfg);
		final Kernel neo = newInstance(neoKernelCfg);
		neo.serviceStart();
		return neo;
	}

	public void serviceStart()
			throws InterruptedException {
		m_diskController.serviceStart();
		m_processMonitor.serviceStart();
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("diskController", m_diskController);
		ds.a("processMonitor", m_processMonitor);
		return ds.s();
	}

	public boolean waitNotBusy(long msMaxWait)
			throws InterruptedException {
		final long tsEnd = System.currentTimeMillis() + msMaxWait;
		boolean busy = true;
		boolean wait = true;
		while (busy && wait) {
			busy = isServiceBusy();
			if (busy) {
				Thread.sleep(1000L);
				wait = System.currentTimeMillis() < tsEnd;
			}
		}
		return !busy;
	}

	private Kernel(KernelCfg kc, ExecutorService exec, DiskController dc, ProcessMonitor pm) {
		assert kc != null;
		assert exec != null;
		assert pm != null;
		this.kc = kc;
		m_execService = exec;
		m_diskController = dc;
		m_processMonitor = pm;
	}

	final KernelCfg kc;
	private final ExecutorService m_execService;
	private final DiskController m_diskController;
	private final ProcessMonitor m_processMonitor;
}
