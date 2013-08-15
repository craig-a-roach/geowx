/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium.mirror;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Request;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.argon.Elapsed;
import com.metservice.beryllium.BerylliumBinaryHttpPayload;
import com.metservice.beryllium.BerylliumCommandHttpDispatcher;
import com.metservice.beryllium.BerylliumCommandHttpExchange;
import com.metservice.beryllium.BerylliumDownloadHttpDispatcher;
import com.metservice.beryllium.BerylliumDownloadHttpExchange;
import com.metservice.beryllium.BerylliumJsonHttpDispatcher;
import com.metservice.beryllium.BerylliumJsonHttpExchange;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.BerylliumPathQuery;
import com.metservice.beryllium.BerylliumQuery;
import com.metservice.beryllium.BerylliumUploadHttpDispatcher;
import com.metservice.beryllium.BerylliumUploadHttpExchange;
import com.metservice.beryllium.IBerylliumHttpTracker;

/**
 * @author roach
 */
class Engine {

	private static final Elapsed PollInterval = Elapsed.newInstance(500L);
	private static final Elapsed OutcomeAwaitInterval = Elapsed.newInstance(5000L);
	private static final String DefaultHiexPath = "!";
	private static final String CsqDiscardSaveTask = "Discard save task";
	private static final String CsqDiscardCommitTask = "Discard commit task";

	private CommitK createCommitTracker(IBerylliumMirrorCommitTask task)
			throws InterruptedException {
		if (task == null) throw new IllegalArgumentException("object is null");
		final String oqcctwPath = ArgonText.oqtw(task.qccPath());
		if (oqcctwPath == null) {
			final Ds ds = Ds.invalidBecause("Missing path for commit task", CsqDiscardCommitTask);
			ds.a("task", task);
			probe.warnMirror(ds.s());
			return null;
		}
		final BerylliumQuery q = BerylliumQuery.newConstant(CProp.path, oqcctwPath);
		final BerylliumPathQuery uri = new BerylliumPathQuery(CUri.Commit, q);
		final CommitK tracker = new CommitK(probe, task);
		final PeerAddress a = cfg.peerAddress;
		final BerylliumCommandHttpExchange hex = BerylliumCommandHttpExchange.newInstance(a, uri);
		hex.sendFrom(m_httpClient, tracker, probe);
		if (probe.isLiveMirror()) {
			probe.liveMirror("SEND", uri, "TO", a);
		}
		return tracker;
	}

	private IBerylliumHttpTracker createDemandTracker(DemandTask task)
			throws InterruptedException {
		assert task != null;
		final BerylliumQuery q = BerylliumQuery.newConstant(CProp.path, task.qccPath);
		final BerylliumPathQuery uri = new BerylliumPathQuery(CUri.Demand, q);
		final DemandK tracker = new DemandK(probe, task);
		final PeerAddress a = cfg.peerAddress;
		final BerylliumDownloadHttpExchange hex = BerylliumDownloadHttpExchange.newInstance(a, uri);
		hex.sendFrom(m_httpClient, tracker, probe);
		if (probe.isLiveMirror()) {
			probe.liveMirror("SEND", uri, "TO", a);
		}
		return tracker;
	}

	private DiscoverK createDiscoverTracker()
			throws InterruptedException {
		String oqtwHiexPath = ArgonText.oqtw(provider.discoverHiexPath());
		if (oqtwHiexPath == null) {
			oqtwHiexPath = DefaultHiexPath;
		}

		final List<String> ozlWipPathsAsc = provider.discoverWipPathsAsc();
		List<String> zlWipPathsAsc = Collections.emptyList();
		if (ozlWipPathsAsc != null) {
			zlWipPathsAsc = ozlWipPathsAsc;
		}
		final DiscoverQ query = new DiscoverQ(oqtwHiexPath, zlWipPathsAsc);
		final DiscoverK tracker = new DiscoverK(probe, query);
		final PeerAddress a = cfg.peerAddress;
		final BerylliumJsonHttpExchange hex = BerylliumJsonHttpExchange.newInstance(a, CUri.Discover, query);
		hex.sendFrom(m_httpClient, tracker, probe);
		if (probe.isLiveMirror()) {
			probe.liveMirror("SEND", query, "TO", a);
		}
		return tracker;
	}

	private SaveK createSaveTracker(IBerylliumMirrorSaveTask task)
			throws InterruptedException {
		assert task != null;
		final String oqcctwPath = ArgonText.oqtw(task.qccPath());
		if (oqcctwPath == null) {
			final Ds ds = Ds.invalidBecause("Missing path for save task", CsqDiscardSaveTask);
			ds.a("task", task);
			probe.warnMirror(ds.s());
			return null;
		}
		final BerylliumBinaryHttpPayload oPayload = task.createPayload(probe);
		if (oPayload == null) {
			final Ds ds = Ds.invalidBecause("Could not create payload for save task", CsqDiscardSaveTask);
			ds.a("path", oqcctwPath);
			ds.a("task", task);
			probe.warnMirror(ds.s());
			return null;
		}
		final BerylliumQuery q = BerylliumQuery.newConstant(CProp.path, oqcctwPath);
		final BerylliumPathQuery uri = new BerylliumPathQuery(CUri.Save, q);
		final SaveK tracker = new SaveK(probe, task);
		final PeerAddress a = cfg.peerAddress;
		final BerylliumUploadHttpExchange hex = BerylliumUploadHttpExchange.newInstance(a, uri, oPayload);
		hex.sendFrom(m_httpClient, tracker, probe);
		if (probe.isLiveMirror()) {
			probe.liveMirror("SEND", uri, "TO", a);
		}
		return tracker;
	}

	private IBerylliumHttpTracker createTracker(IBerylliumMirrorTask task)
			throws InterruptedException {
		assert task != null;
		if (task instanceof IBerylliumMirrorSaveTask) return createSaveTracker((IBerylliumMirrorSaveTask) task);
		if (task instanceof IBerylliumMirrorCommitTask) return createCommitTracker((IBerylliumMirrorCommitTask) task);
		if (task instanceof DemandTask) return createDemandTracker((DemandTask) task);
		if (task instanceof DiscoverTask) return createDiscoverTracker();
		probe.warnMirror("Unsupported task class: " + task.getClass().getName());
		return null;
	}

	private void handleCommitK(CommitK tracker) {
		final boolean isComplete = tracker.isComplete();
		if (isComplete) {
			provider.onCommitComplete(tracker.task);
		} else {
			m_taskRetryBox.set(tracker.task);
			provider.onHttpRetry();
		}
	}

	private void handleDemandK(DemandK tracker) {
		assert tracker != null;
		final BerylliumBinaryHttpPayload oResponse = tracker.getResponse();
		if (oResponse == null) {
			m_taskRetryBox.set(tracker.task);
			provider.onHttpRetry();
		} else {
			handleDemandR(tracker.task, oResponse);
		}
	}

	private void handleDemandR(DemandTask task, BerylliumBinaryHttpPayload payload) {
		assert task != null;
		assert payload != null;
		final Binary content = payload.content();
		final long tsLastModified = payload.tsLastModified();
		if (probe.isLiveMirror()) {
			probe.liveMirror("SAVE", task.qccPath, "bytes", content.byteCount());
		}
		provider.save(task.qccPath, content, tsLastModified);
	}

	private void handleDiscoverK(DiscoverK tracker) {
		final DiscoverR oResponse = tracker.getResponse();
		if (oResponse == null) {
			m_taskRetryBox.set(DiscoverTask.Instance);
			provider.onHttpRetry();
		} else {
			handleDiscoverR(oResponse);
		}
	}

	private void handleDiscoverR(DiscoverR response) {
		assert response != null;
		final List<String> zlCommitPathsAsc = response.zlCommitPathsAsc();
		for (final String qccWipPath : zlCommitPathsAsc) {
			if (probe.isLiveMirror()) {
				probe.liveMirror("COMMIT", qccWipPath);
			}
			provider.commit(qccWipPath);
		}
		final List<String> zlDemandPathsAsc = response.zlDemandPathsAsc();
		for (final String qccPath : zlDemandPathsAsc) {
			m_taskQueue.add(new DemandTask(qccPath));
		}
		m_discoveryGate.set(true);
	}

	private void handleSaveK(SaveK tracker) {
		final boolean isComplete = tracker.isComplete();
		if (isComplete) {
			provider.onSaveComplete(tracker.task);
		} else {
			m_taskRetryBox.set(tracker.task);
			provider.onHttpRetry();
		}
	}

	private void handleTracker(IBerylliumHttpTracker tracker) {
		if (tracker instanceof SaveK) {
			handleSaveK((SaveK) tracker);
			return;
		}
		if (tracker instanceof CommitK) {
			handleCommitK((CommitK) tracker);
			return;
		}
		if (tracker instanceof DiscoverK) {
			handleDiscoverK((DiscoverK) tracker);
			return;
		}
		if (tracker instanceof DemandK) {
			handleDemandK((DemandK) tracker);
			return;
		}
		probe.warnMirror("Unsupported tracker class: " + tracker.getClass().getName());
	}

	void discover() {
		try {
			if (m_discoveryGate.get()) {
				m_taskQueue.put(DiscoverTask.Instance);
				m_discoveryGate.set(false);
			}
		} catch (final InterruptedException ex) {
			probe.warnMirror("Peer discovery cancelled");
		}
	}

	void pullTask() {
		try {
			final boolean isTracking = m_trackerBox.get() != null;
			boolean moreTasks = !isTracking;
			while (moreTasks) {
				final IBerylliumMirrorTask oRetryTask = m_taskRetryBox.getAndSet(null);
				IBerylliumMirrorTask oTask = null;
				if (oRetryTask == null) {
					oTask = m_taskQueue.poll(PollInterval.sms, TimeUnit.MILLISECONDS);
				} else {
					Thread.sleep(cfg.minRetryIntervalMs);
					oTask = oRetryTask;
				}
				if (oTask == null) {
					if (m_syncPointGate.getAndSet(false)) {
						provider.onSynchronizationPoint();
						if (probe.isLiveMirror()) {
							probe.liveMirror("SYNC");
						}
					}
					moreTasks = false;
				} else {
					m_syncPointGate.set(true);
					final IBerylliumHttpTracker oTracker = createTracker(oTask);
					if (oTracker != null) {
						m_trackerBox.set(oTracker);
						moreTasks = false;
					}
				}
			}
		} catch (final InterruptedException ex) {
			probe.warnMirror("Task pull cancelled");
		}
	}

	public void handleRequest(BerylliumPath path, Request rq, HttpServletResponse rp)
			throws IOException, ServletException {
		if (path == null) throw new IllegalArgumentException("object is null");
		if (path.equals(CUri.Save)) {
			final SaveService service = new SaveService(provider);
			m_uploadDispatcher.handle(service, path, rq, rp);
			return;
		}
		if (path.equals(CUri.Commit)) {
			final CommitService service = new CommitService(provider);
			m_commandDispatcher.handle(service, path, rq, rp);
			return;
		}
		if (path.equals(CUri.Discover)) {
			final DiscoverService service = new DiscoverService(provider);
			m_jsonDispatcher.handle(service, path, rq, rp);
			return;
		}
		if (path.equals(CUri.Demand)) {
			final DemandService service = new DemandService(provider);
			m_downloadDispatcher.handle(service, path, rq, rp);
			return;
		}
		probe.warnMirror("Unsupported request " + path);
		rp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	public void handleTasks()
			throws InterruptedException {
		final IBerylliumHttpTracker oTracker = m_trackerBox.get();
		if (oTracker == null) {
			pullTask();
		} else {
			if (oTracker.tryOutcome(OutcomeAwaitInterval)) {
				handleTracker(oTracker);
				m_trackerBox.set(null);
			}
		}
	}

	public void push(IBerylliumMirrorTask task)
			throws InterruptedException {
		if (task == null) throw new IllegalArgumentException("object is null");
		m_taskQueue.put(task);
	}

	public Engine(IBerylliumMirrorProbe probe, IBerylliumMirrorProvider provider, MirrorCfg cfg, HttpClient httpClient) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (provider == null) throw new IllegalArgumentException("object is null");
		if (cfg == null) throw new IllegalArgumentException("object is null");
		if (httpClient == null) throw new IllegalArgumentException("object is null");
		this.probe = probe;
		this.provider = provider;
		this.cfg = cfg;
		m_httpClient = httpClient;
		m_taskRetryBox = new AtomicReference<IBerylliumMirrorTask>();
		m_taskQueue = new LinkedBlockingDeque<IBerylliumMirrorTask>();
		m_trackerBox = new AtomicReference<IBerylliumHttpTracker>();
		m_discoveryGate = new AtomicBoolean(true);
		m_syncPointGate = new AtomicBoolean(false);
		m_jsonDispatcher = new BerylliumJsonHttpDispatcher(probe, cfg.payloadQuotaBc, false);
		m_downloadDispatcher = new BerylliumDownloadHttpDispatcher(probe);
		m_uploadDispatcher = new BerylliumUploadHttpDispatcher(probe, cfg.payloadQuotaBc);
		m_commandDispatcher = new BerylliumCommandHttpDispatcher(probe);
	}
	final IBerylliumMirrorProbe probe;
	final IBerylliumMirrorProvider provider;
	final MirrorCfg cfg;
	private final HttpClient m_httpClient;
	private final AtomicReference<IBerylliumMirrorTask> m_taskRetryBox;
	private final BlockingQueue<IBerylliumMirrorTask> m_taskQueue;
	private final AtomicReference<IBerylliumHttpTracker> m_trackerBox;
	private final AtomicBoolean m_discoveryGate;
	private final AtomicBoolean m_syncPointGate;
	private final BerylliumJsonHttpDispatcher m_jsonDispatcher;
	private final BerylliumDownloadHttpDispatcher m_downloadDispatcher;
	private final BerylliumUploadHttpDispatcher m_uploadDispatcher;
	private final BerylliumCommandHttpDispatcher m_commandDispatcher;
}
