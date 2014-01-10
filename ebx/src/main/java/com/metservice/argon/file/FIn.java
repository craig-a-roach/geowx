/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import com.metservice.argon.ArgonLockException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
class FIn {

	public static Binary loadBinary(IArgonFileProbe probe, File srcFile, boolean lock, int bcQuota)
			throws ArgonLockException, ArgonQuotaException, ArgonStreamReadException {
		final FIn fin = FIn.newInstance(probe, srcFile, lock);
		return fin.load(probe, bcQuota);
	}

	public static FIn newInstance(IArgonFileProbe probe, File srcFile, boolean lock)
			throws ArgonLockException {
		final FileInputStream ofis = UArgonFile.createFileInputStream(srcFile);
		final FileChannel ofch = (ofis == null) ? null : ofis.getChannel();
		boolean lockFailed = true;
		try {
			final FileLock oflock = (lock && ofch != null) ? UArgonFile.newLockExclusive(probe, srcFile, ofch) : null;
			lockFailed = false;
			return new FIn(srcFile, ofis, ofch, oflock);
		} finally {
			if (lockFailed) {
				UArgonFile.close(probe, srcFile, ofis);
			}
		}
	}

	private Binary load(IArgonFileProbe probe, int bcQuota)
			throws ArgonQuotaException, ArgonStreamReadException {
		assert probe != null;
		if (!canRead) return Binary.Empty;
		if (m_ofch == null) throw new IllegalStateException("File '" + srcFile + "' not open");
		final long bcLength = srcFile.length();
		if (bcLength > bcQuota) {
			final Ds ds = Ds.invalidBecause("File length exceeds read quota", "Will not attempt to load file");
			ds.a("length", bcLength);
			ds.a("quota", bcQuota);
			probe.failFile(ds, srcFile);
			final String m = "File '" + srcFile + "' is " + bcLength + " bytes; this exceeds quota of " + bcQuota + " bytes";
			throw new ArgonQuotaException(m);
		}
		final int bciLength = (int) bcLength;
		final byte[] zptDest = new byte[bciLength];
		final ByteBuffer dest = ByteBuffer.wrap(zptDest);
		try {
			dest.clear();
			UArgonFile.load(probe, srcFile, dest, m_ofch);
			return Binary.newFromTransient(zptDest);
		} finally {
			close(probe);
		}
	}

	public void close(IArgonFileProbe probe) {
		assert probe != null;
		UArgonFile.unlock(probe, srcFile, m_oflock);
		m_oflock = null;
		UArgonFile.close(probe, srcFile, m_ofis);
		m_ofis = null;
		m_ofch = null;
	}

	public FileChannel fileChannel() {
		if (m_ofch == null) throw new IllegalStateException("File '" + srcFile + "' not open");
		return m_ofch;
	}

	public FileInputStream fileInputStream() {
		if (m_ofis == null) throw new IllegalStateException("File '" + srcFile + "' not open");
		return m_ofis;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(srcFile);
		sb.append(canRead ? " r+" : " r-");
		sb.append(m_ofis == null ? " closed" : " open");
		if (m_oflock != null) {
			sb.append("locked");
		}
		return sb.toString();
	}

	private FIn(File srcFile, FileInputStream ofis, FileChannel ofch, FileLock oflock) {
		assert srcFile != null;
		this.srcFile = srcFile;
		this.canRead = ofis != null && ofch != null;
		m_ofis = ofis;
		m_ofch = ofch;
		m_oflock = oflock;
	}

	public final File srcFile;
	public final boolean canRead;
	private FileInputStream m_ofis;
	private FileChannel m_ofch;
	private FileLock m_oflock;
}
