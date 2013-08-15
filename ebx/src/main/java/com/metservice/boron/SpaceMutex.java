/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author roach
 */
class SpaceMutex {

	private boolean acquiredLk(KernelCfg kc) {
		if (m_ofos != null || m_oflock != null) return false;
		try {
			m_ofos = new FileOutputStream(m_fileMutex);
		} catch (final FileNotFoundException exFNF) {
		}

		if (m_ofos == null) return false;

		boolean acquired = false;
		try {
			m_oflock = m_ofos.getChannel().tryLock();
			acquired = m_oflock != null;
		} catch (final OverlappingFileLockException exFL) {
		} catch (final IOException exIO) {
		} finally {
			if (!acquired) {
				UBoron.close(kc.probe, m_fileMutex, m_ofos);
			}
		}
		return acquired;
	}

	private void releaseLk(KernelCfg kc) {
		UBoron.unlock(kc.probe, m_fileMutex, m_oflock);
		UBoron.close(kc.probe, m_fileMutex, m_ofos);
	}

	public void acquire(KernelCfg kc)
			throws BoronApiException {
		if (kc == null) throw new IllegalArgumentException("object is null");
		m_slock.lock();
		try {
			if (!acquiredLk(kc)) {
				final String msg = "Space '" + kc.id + "' is already in use on this host";
				throw new BoronApiException(msg);
			}
		} finally {
			m_slock.unlock();
		}
	}

	public void release(KernelCfg kc) {
		if (kc == null) throw new IllegalArgumentException("object is null");
		m_slock.lock();
		try {
			releaseLk(kc);
		} finally {
			m_slock.unlock();
		}
	}

	public SpaceMutex(File cndirHome) {
		if (cndirHome == null) throw new IllegalArgumentException("object is null");
		m_fileMutex = new File(cndirHome, CBoron.FileName_Mutex);
	}

	private final Lock m_slock = new ReentrantLock();
	private final File m_fileMutex;
	private FileOutputStream m_ofos;
	private FileLock m_oflock;
}
