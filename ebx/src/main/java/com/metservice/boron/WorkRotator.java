/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.metservice.argon.ArgonText;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.file.ArgonFileManagement;

/**
 * @author roach
 */
class WorkRotator {

	private static final File[] ZFILES = new File[0];

	public static WorkRotator newInstance(KernelCfg kc, File cndirWork) {
		if (cndirWork == null) throw new IllegalArgumentException("object is null");
		final File[] ozptWorkDirs = cndirWork.listFiles();
		final File[] zptWorkDirsAsc = ozptWorkDirs == null ? ZFILES : ozptWorkDirs;
		Arrays.sort(zptWorkDirsAsc, UBoron.FileByLastModified);

		final Queue<BoronProcessId> queue = new LinkedList<BoronProcessId>();
		for (int i = 0; i < zptWorkDirsAsc.length; i++) {
			final File f = zptWorkDirsAsc[i];
			if (f.isDirectory()) {
				final String qDirName = f.getName();
				final long nDirName = ArgonText.parseLongB36(qDirName, -1L).longValue();
				if (nDirName >= BoronProcessId.Init) {
					queue.add(new BoronProcessId(nDirName));
				} else {
					ArgonDirectoryManagement.remove(kc.probe, f, false);
				}
			} else {
				ArgonFileManagement.deleteFile(kc.probe, f);
			}
		}

		final WorkRotator neo = new WorkRotator(kc, cndirWork, queue);
		neo.trim();
		return neo;
	}

	public void add(BoronProcessId bpid) {
		if (bpid == null) throw new IllegalArgumentException("object is null");
		m_lock.lock();
		try {
			m_queue.add(bpid);
		} finally {
			m_lock.unlock();
		}
	}

	public BoronProcessId nextProcessId() {
		m_lock.lock();
		try {
			BoronProcessId oMax = null;
			for (final BoronProcessId bpid : m_queue) {
				if (oMax == null || bpid.id > oMax.id) {
					oMax = bpid;
				}
			}
			final long nextProcessId = oMax == null ? BoronProcessId.Init : oMax.id + 1L;
			return new BoronProcessId(nextProcessId);
		} finally {
			m_lock.unlock();
		}
	}

	public void trim() {
		m_lock.lock();
		try {
			final int depth = m_queue.size();
			final int reclaim = depth - m_maxDepth;
			for (int i = 0; i < reclaim; i++) {
				final BoronProcessId oPurgeId = m_queue.poll();
				if (oPurgeId != null) {
					final String qPSubName = BoronProcessId.qId(oPurgeId.id);
					final File cndirProcess = new File(m_cndirWork, qPSubName);
					ArgonDirectoryManagement.remove(kc.probe, cndirProcess, false);
				}
			}

		} finally {
			m_lock.unlock();
		}
	}

	private WorkRotator(KernelCfg kc, File cndirWork, Queue<BoronProcessId> initQueue) {
		assert cndirWork != null;
		assert initQueue != null;
		this.kc = kc;
		m_cndirWork = cndirWork;
		m_maxDepth = kc.cfg.getWorkHistoryDepth();
		m_queue = initQueue;
	}

	final KernelCfg kc;
	private final File m_cndirWork;
	private final int m_maxDepth;
	private final Lock m_lock = new ReentrantLock();
	private final Queue<BoronProcessId> m_queue;
}
