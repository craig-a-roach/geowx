/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.metservice.argon.Ds;
import com.metservice.argon.management.ArgonSpaceThreadFactory;
import com.metservice.argon.text.ArgonJoiner;
import com.metservice.beryllium.BerylliumPath;
import com.metservice.beryllium.BerylliumSupportId;

/**
 * @author roach
 */
public class NeonAssurance {

	private void addImp(BerylliumSupportId idSupport, String qccSourcePath) {
		if (idSupport == null) throw new IllegalArgumentException("object is null");
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		final NeonSpace ospace = getReadySpace();
		final AssuranceSource source = declareSource(qccSourcePath);
		if (ospace == null) {
			source.setUnavailableState();
		} else {
			final EsRequest request = new EsRequest(idSupport, qccSourcePath);
			applyContext(request);
			final AssuranceRun run = new AssuranceRun(ospace, request);
			final Future<AssuranceRunReport> futureReport = m_xc.submit(run);
			source.setRunningState(futureReport);
		}
	}

	private void addTreeImp(BerylliumSupportId idSupport, BerylliumPath path) {
		if (idSupport == null) throw new IllegalArgumentException("object is null");
		if (path == null) throw new IllegalArgumentException("object is null");
		final List<? extends INeonSourceDescriptor> zlDescriptorsAsc = kc.sourceProvider.zlDescriptorsAsc(path.qtwPath());
		for (final INeonSourceDescriptor sd : zlDescriptorsAsc) {
			final NeonSourceDescriptorType dtype = sd.type();
			switch (dtype) {
				case Container: {
					final BerylliumPath subPath = path.newPath(sd.qccNode());
					addTreeImp(idSupport, subPath);
				}
				break;
				case EcmaScript: {
					final BerylliumPath subPath = path.newPath(sd.qccNode());
					addImp(idSupport, subPath.qtwPath());
				}
				break;
				default:
			}
		}
	}

	private void applyContext(EsRequest request) {
		assert request != null;
		final NeonAssuranceContext oContext = m_context.get();
		if (oContext != null) {
			request.add(oContext.zptInstallers());
		}
	}

	private AssuranceSource declareSource(String qccSourcePath) {
		m_rwlock.writeLock().lock();
		try {
			AssuranceSource vSource = m_map.get(qccSourcePath);
			if (vSource == null) {
				vSource = new AssuranceSource(qccSourcePath);
				m_map.put(qccSourcePath, vSource);
			}
			return vSource;
		} finally {
			m_rwlock.writeLock().unlock();
		}
	}

	private NeonSpace getReadySpace() {
		final NeonSpace ospace = m_space.get();
		if (ospace == null) return null;
		if (ospace.isShutdownInProgress()) return null;
		if (m_xc.isShutdown() || m_xc.isTerminated()) return null;
		return ospace;
	}

	private boolean isAssureLk(BerylliumPath path) {
		assert path != null;
		final String qtwPath = path.qtwPath();
		final INeonSourceDescriptor descriptor = kc.sourceProvider.descriptor(qtwPath);
		if (descriptor.isAssure()) return true;
		final NeonSourceDescriptorType dtype = descriptor.type();
		if (dtype == NeonSourceDescriptorType.Container) {
			final List<? extends INeonSourceDescriptor> zlSubAsc = kc.sourceProvider.zlDescriptorsAsc(qtwPath);
			for (final INeonSourceDescriptor sd : zlSubAsc) {
				final BerylliumPath subPath = path.newPath(sd.qccNode());
				if (isAssureLk(subPath)) return true;
			}
		}
		return false;
	}

	private void makeReportLk(NeonAssuranceReport rpt, BerylliumPath path, INeonSourceDescriptor descriptor) {
		if (!descriptor.isAssure()) return;
		final String qtwPath = path.qtwPath();
		final NeonSourceDescriptorType dtype = descriptor.type();
		switch (dtype) {
			case Container: {
				final List<? extends INeonSourceDescriptor> zlSubAsc = kc.sourceProvider.zlDescriptorsAsc(qtwPath);
				for (final INeonSourceDescriptor sd : zlSubAsc) {
					final BerylliumPath subPath = path.newPath(sd.qccNode());
					makeReportLk(rpt, subPath, sd);
				}
			}
			break;
			case EcmaScript: {
				final NeonSpace ospace = getReadySpace();
				final BerylliumSupportId sid = BerylliumSupportId.Loopback;
				if (ospace != null) {
					final EsRequest request = new EsRequest(sid, qtwPath);
					applyContext(request);
					final AssuranceRun run = new AssuranceRun(ospace, request);
					AssuranceRunReport oRunReport = null;
					try {
						oRunReport = run.call();
					} catch (final Exception ex) {
						oRunReport = AssuranceRunReport.newRuntime(Ds.format(ex));
					}
					if (oRunReport != null) {
						rpt.add(path, descriptor, oRunReport);
					}
				}
			}
			break;
			default:
		}
	}

	private void makeStateLk(Set<String> stateSet, BerylliumPath path) {
		assert stateSet != null;
		assert path != null;
		final String qtwPath = path.qtwPath();
		final INeonSourceDescriptor descriptor = kc.sourceProvider.descriptor(qtwPath);
		if (!descriptor.isAssure()) return;
		final NeonSourceDescriptorType dtype = descriptor.type();
		switch (dtype) {
			case Container: {
				final List<? extends INeonSourceDescriptor> zlSubAsc = kc.sourceProvider.zlDescriptorsAsc(qtwPath);
				for (final INeonSourceDescriptor sd : zlSubAsc) {
					final BerylliumPath subPath = path.newPath(sd.qccNode());
					makeStateLk(stateSet, subPath);
				}
			}
			break;
			case EcmaScript: {
				final AssuranceSource oSource = findSourceLk(qtwPath);
				if (oSource == null) {
					stateSet.add("Untested");
				} else {
					stateSet.add(oSource.qState());
				}
			}
			break;
			default:
		}
	}

	private NeonAssuranceReport newReportLk() {
		final NeonAssuranceReport rpt = new NeonAssuranceReport();
		final List<? extends INeonSourceDescriptor> zlSubAsc = kc.sourceProvider.zlDescriptorsAsc("");
		for (final INeonSourceDescriptor sd : zlSubAsc) {
			final BerylliumPath subPath = BerylliumPath.newInstance(sd.qccNode());
			makeReportLk(rpt, subPath, sd);
		}
		return rpt;
	}

	public void add(BerylliumSupportId idSupport, BerylliumPath path) {
		if (path == null) throw new IllegalArgumentException("object is null");
		addImp(idSupport, path.qtwPath());
	}

	public void add(BerylliumSupportId idSupport, String qccSourcePath) {
		addImp(idSupport, qccSourcePath);
	}

	public void addTree(BerylliumSupportId idSupport, BerylliumPath path) {
		addTreeImp(idSupport, path);
	}

	public void addTree(BerylliumSupportId idSupport, String qccSourcePath) {
		addTreeImp(idSupport, BerylliumPath.newInstance(qccSourcePath));
	}

	public void end() {
		m_space.set(null);
		m_xc.shutdownNow();
	}

	public AssuranceSource findSource(String qccSourcePath) {
		if (qccSourcePath == null || qccSourcePath.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		m_rwlock.readLock().lock();
		try {
			return findSourceLk(qccSourcePath);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public AssuranceSource findSourceLk(String qccSourcePath) {
		assert qccSourcePath != null && qccSourcePath.length() > 0;
		return m_map.get(qccSourcePath);
	}

	public boolean isAssure(BerylliumPath path) {
		m_rwlock.readLock().lock();
		try {
			return isAssureLk(path);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public NeonAssuranceReport newReport() {
		m_rwlock.readLock().lock();
		try {
			return newReportLk();
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public void notifyShutdown() {
		m_xc.shutdown();
	}

	public void register(NeonAssuranceContext oContext) {
		m_context.set(oContext);
	}

	public void register(NeonSpace space) {
		if (space == null) throw new IllegalArgumentException("object is null");
		m_space.set(space);
	}

	public String zStateTree(BerylliumPath path) {
		m_rwlock.readLock().lock();
		try {
			final Set<String> stateSet = new HashSet<String>();
			makeStateLk(stateSet, path);
			final List<String> zl = new ArrayList<String>(stateSet);
			Collections.sort(zl);
			return ArgonJoiner.zComma(zl);
		} finally {
			m_rwlock.readLock().unlock();
		}
	}

	public NeonAssurance(KernelCfg kc) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		this.kc = kc;
		m_xc = new ArgonSpaceThreadFactory(CNeon.ThreadPrefix, kc.id).newCachedThreadPool();
	}
	final KernelCfg kc;
	private final AtomicReference<NeonSpace> m_space = new AtomicReference<NeonSpace>();
	private final AtomicReference<NeonAssuranceContext> m_context = new AtomicReference<NeonAssuranceContext>();
	private final ReadWriteLock m_rwlock = new ReentrantReadWriteLock();
	private final ExecutorService m_xc;
	private final Map<String, AssuranceSource> m_map = new HashMap<String, AssuranceSource>(8);
}
