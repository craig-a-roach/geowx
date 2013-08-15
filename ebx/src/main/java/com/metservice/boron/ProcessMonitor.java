/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.metservice.argon.ArgonClock;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
class ProcessMonitor {

	private ProcessEngine newEngine(IBoronScript script, MainProcessImage mpi) {
		assert script != null;
		assert mpi != null;

		final ProcessBuilder processBuilder = mpi.processBuilder;
		final FeedQueue feedQueue = new FeedQueue(script);
		final ProductQueue productQueue = new ProductQueue(script);
		ProcessThreadGroup optg = null;
		Process oProcess = null;
		boolean launched = false;
		try {
			oProcess = processBuilder.start();
			final long tsStart = ArgonClock.tsNow();
			final RunnableLife runLife = new RunnableLife(kc, oProcess, productQueue);
			final RunnableIn runStdIn = new RunnableIn(kc, oProcess.getOutputStream(), feedQueue);
			final RunnableOut runStdOut = new RunnableOut(kc, OutStreamType.StdOut, oProcess.getInputStream(), productQueue);
			final RunnableOut runStdErr = new RunnableOut(kc, OutStreamType.StdErr, oProcess.getErrorStream(), productQueue);
			optg = new ProcessThreadGroup(script, runLife, runStdIn, runStdOut, runStdErr, tsStart);
			optg.start(m_execService);
			launched = true;
		} catch (final IOException exIO) {
			final BoronInterpreterFailure bif = new BoronInterpreterFailure(mpi, exIO);
			productQueue.putProcessCreateInvalid(bif);
		} catch (final RuntimeException exRT) {
			final Ds ds = Ds.triedTo("Create process", exRT);
			ds.a("interpreterId", script.interpreterId());
			ds.a("mpi", mpi);
			ds.a("self", this);
			kc.probe.failSoftware(ds);
			productQueue.putProcessCreateFailed();
		} catch (final InterruptedException exIR) {
			productQueue.putProcessCreateCancelled();
			Thread.currentThread().interrupt();
		} finally {
			if (!launched) {
				UBoron.destroyProcess(oProcess);
			}
		}
		return new ProcessEngine(mpi.processId, feedQueue, productQueue, optg);
	}

	private IBoronScriptInterpreter selectInterpreter(IBoronScript script)
			throws BoronApiException {
		if (script == null) throw new IllegalArgumentException("object is null");
		final BoronInterpreterId interpreterId = script.interpreterId();
		final IBoronScriptInterpreter oInterpreter = kc.cfg.findInterpreter(interpreterId);
		if (oInterpreter == null) {
			final String msg = "Interpreter '" + interpreterId + "' has not been registered in space configuration";
			throw new BoronApiException(msg);
		}
		return oInterpreter;
	}

	public ProcessEngine findEngine(BoronProcessId processId) {
		if (processId == null) throw new IllegalArgumentException("object is null");
		return m_monitoringTable.find(processId);
	}

	public boolean isBusy() {
		return m_monitoringTable.isBusy();
	}

	public ProcessEngine newProcessEngine(IBoronScript script)
			throws BoronApiException, BoronImpException, InterruptedException {
		if (script == null) throw new IllegalArgumentException("object is null");

		final IBoronScriptInterpreter interpreter = selectInterpreter(script);
		final MainProcessImage mpi = m_diskController.newMainProcessImage(interpreter, script);
		final ProcessEngine engine = newEngine(script, mpi);
		m_monitoringTable.add(engine);
		return engine;
	}

	public ProcessEngine removeEngine(BoronProcessId processId) {
		if (processId == null) throw new IllegalArgumentException("object is null");
		return m_monitoringTable.remove(processId);
	}

	public void serviceEnd() {
		m_monitoringTable.cancel();
		m_runnableHealth.end();
	}

	public void serviceStart()
			throws InterruptedException {
		m_execService.execute(m_runnableHealth);
		m_runnableHealth.awaitStart();
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("monitoringTable", m_monitoringTable);
		return ds.s();
	}

	public ProcessMonitor(KernelCfg kc, ExecutorService exec, DiskController dc) {
		assert kc != null;
		assert dc != null;
		this.kc = kc;
		m_diskController = dc;
		final MonitoringTable monitoringTable = new MonitoringTable(kc, dc);
		final RunnableHealth runnableHealth = new RunnableHealth(kc, monitoringTable);
		m_execService = exec;
		m_monitoringTable = monitoringTable;
		m_runnableHealth = runnableHealth;
	}

	final KernelCfg kc;
	private final DiskController m_diskController;
	private final ExecutorService m_execService;
	private final MonitoringTable m_monitoringTable;
	private final RunnableHealth m_runnableHealth;
}
