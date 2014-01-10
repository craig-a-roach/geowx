/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import com.metservice.argon.ArgonLockException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
class FOut {

	public static FOut newInstance(IArgonFileProbe probe, File destFile, boolean lock)
			throws ArgonPermissionException, ArgonLockException {
		final FileOutputStream fos = UArgonFile.newFileOutputStream(probe, destFile);
		final FileChannel fch = fos.getChannel();
		boolean lockFailed = true;
		try {
			final FileLock oflock = lock ? UArgonFile.newLockExclusive(probe, destFile, fch) : null;
			lockFailed = false;
			return new FOut(destFile, fos, fch, oflock);
		} finally {
			if (lockFailed) {
				UArgonFile.close(probe, destFile, fos);
			}
		}
	}

	public void close(IArgonFileProbe probe) {
		assert probe != null;
		UArgonFile.unlock(probe, destFile, m_oflock);
		m_oflock = null;
		UArgonFile.close(probe, destFile, m_ofos);
		m_ofos = null;
		m_ofch = null;
	}

	public FileChannel fileChannel() {
		if (m_ofch == null) throw new IllegalStateException("File '" + destFile + "' not open");
		return m_ofch;
	}

	public FileOutputStream fileOutputStream() {
		if (m_ofos == null) throw new IllegalStateException("File '" + destFile + "' not open");
		return m_ofos;
	}

	public void save(IArgonFileProbe probe, ByteBuffer src)
			throws ArgonStreamWriteException {
		assert probe != null;
		assert src != null;
		if (m_ofch == null) throw new IllegalStateException("File '" + destFile + "' not open");
		boolean failed = true;
		try {
			UArgonFile.save(probe, destFile, src, m_ofch);
			failed = false;
		} finally {
			if (failed) {
				close(probe);
			}
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(destFile);
		sb.append(m_ofos == null ? " closed" : " open");
		if (m_oflock != null) {
			sb.append("locked");
		}
		return sb.toString();
	}

	private FOut(File destFile, FileOutputStream fos, FileChannel fch, FileLock oflock) {
		assert destFile != null;
		this.destFile = destFile;
		m_ofos = fos;
		m_ofch = fch;
		m_oflock = oflock;
	}

	public final File destFile;
	private FileOutputStream m_ofos;
	private FileChannel m_ofch;
	private FileLock m_oflock;
}
