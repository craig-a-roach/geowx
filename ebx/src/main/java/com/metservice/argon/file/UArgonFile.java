/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import com.metservice.argon.ArgonLockException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
class UArgonFile {

	public static final String SysPropName_User = "user.name";
	public static final String SysPropName_UserDir = "user.dir";
	public static final String SysPropName_UserHome = "user.home";

	private static boolean isPathNodeSeparator(char ch) {
		return ch == '/' || ch == '\\';
	}

	public static void close(IArgonFileProbe probe, File file, FileInputStream ofis) {
		if (ofis == null) return;
		try {
			ofis.close();
		} catch (final IOException exIO) {
			final Ds ds = Ds.triedTo("Close file input stream", exIO, "Potential resource leak");
			probe.warnFile(ds, file);
		}
	}

	public static void close(IArgonFileProbe probe, File file, FileOutputStream ofos) {
		if (ofos == null) return;
		try {
			ofos.close();
		} catch (final IOException exIO) {
			final Ds ds = Ds.triedTo("Close file output stream", exIO, "Potential resource leak");
			probe.warnFile(ds, file);
		}
	}

	public static FileInputStream createFileInputStream(File src) {
		assert src != null;
		try {
			return new FileInputStream(src);
		} catch (final FileNotFoundException ex) {
			return null;
		}
	}

	public static void load(IArgonFileProbe probe, File file, ByteBuffer dest, FileChannel fch)
			throws ArgonStreamReadException {
		assert probe != null;
		assert dest != null;
		assert fch != null;
		try {
			while (dest.hasRemaining()) {
				final int bcRead = fch.read(dest);
				if (bcRead < 0) {
					final Ds ds = Ds.invalidBecause("Reached end-of-file before before buffer filled",
							ArgonStreamReadException.class);
					ds.a("dest", dest);
					probe.failFile(ds, file);
					throw new ArgonStreamReadException("Incomplete read of  '" + file + "'");
				}
			}
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Load file into byte buffer", ex, ArgonStreamReadException.class);
			ds.a("dest", dest);
			probe.failFile(ds, file);
			throw new ArgonStreamReadException("Could not read from '" + file + "'");
		}
	}

	public static FileOutputStream newFileOutputStream(IArgonFileProbe probe, File dest)
			throws ArgonPermissionException {
		assert probe != null;
		assert dest != null;
		try {
			return new FileOutputStream(dest);
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Create file for output", ex, ArgonPermissionException.class);
			probe.failFile(ds, dest);
			throw new ArgonPermissionException("Could not open '" + dest + "' for output");
		}
	}

	public static FileLock newLockExclusive(IArgonFileProbe probe, File file, FileChannel fch)
			throws ArgonLockException {
		assert probe != null;
		assert file != null;
		assert fch != null;
		try {
			return fch.lock();
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Lock file; exclusive", ex, ArgonLockException.class);
			probe.failFile(ds, file);
			throw new ArgonLockException("Could not lock-exclusive '" + file + "'");
		}
	}

	public static String oqtwCleanNode(String ozNode) {
		final String oqtw = ArgonText.oqtw(ozNode);
		if (oqtw == null) return null;
		final boolean frontSep = isPathNodeSeparator(oqtw.charAt(0));
		final String ztwFront = frontSep ? oqtw.substring(1) : oqtw;
		final int frontLen = ztwFront.length();
		if (frontLen == 0) return null;
		final boolean backSep = isPathNodeSeparator(ztwFront.charAt(frontLen - 1));
		final String ztwBack = backSep ? ztwFront.substring(0, frontLen - 1) : ztwFront;
		final int backLen = ztwBack.length();
		return backLen == 0 ? null : ztwBack;
	}

	public static String qUserHome() {
		final String ozUserHome = System.getProperty(SysPropName_UserHome);
		if (ozUserHome == null || ozUserHome.length() == 0)
			throw new UnsupportedOperationException("Cannot determine user home");
		return ozUserHome;
	}

	public static String qUserName() {
		final String ozUserName = System.getProperty(SysPropName_User);
		return ozUserName == null || ozUserName.length() == 0 ? "unknown" : ozUserName;
	}

	public static void save(IArgonFileProbe probe, File file, ByteBuffer src, FileChannel fch)
			throws ArgonStreamWriteException {
		assert probe != null;
		assert src != null;
		assert fch != null;
		try {
			while (src.hasRemaining()) {
				fch.write(src);
			}
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Save byte buffer to file", ex, ArgonStreamWriteException.class);
			ds.a("src", src);
			probe.failFile(ds, file);
			throw new ArgonStreamWriteException("Could not write to '" + file + "'");
		}
	}

	public static void unlock(IArgonFileProbe probe, File file, FileLock ofileLock) {
		assert probe != null;
		assert file != null;
		if (ofileLock == null) return;
		try {
			ofileLock.release();
		} catch (final IOException exIO) {
			final Ds ds = Ds.triedTo("Unlock file", exIO, "Potential contention problems");
			probe.warnFile(ds, file);
		}
	}

	public static void zeroLeft(StringBuilder dst, String zVal, int width) {
		assert dst != null;
		final int pad = width - zVal.length();
		for (int i = 0; i < pad; i++) {
			dst.append('0');
		}
		dst.append(zVal);
	}
}
