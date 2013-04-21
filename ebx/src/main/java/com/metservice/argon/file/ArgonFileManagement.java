/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
public class ArgonFileManagement {

	private static String deleteFileImp(File ex) {
		if (ex.delete()) return null;
		return "delete '" + ex + "'";
	}

	private static String renameFileImp(File from, File dest) {
		String pre = "";
		if (dest.exists()) {
			if (dest.delete()) {
				pre = " (pre-delete was successful)";
			} else {
				pre = " (pre-delete failed)";
			}
		}
		final boolean renamed = from.renameTo(dest);
		if (renamed) return null;
		return "rename '" + from + "' to '" + dest + "'" + pre;
	}

	public static File cnfile(IArgonFileProbe probe, File file) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (file == null) throw new IllegalArgumentException("object is null");
		try {
			return file.getCanonicalFile();
		} catch (final IOException exIO) {
			final Ds ds = Ds.triedTo("Canonize file", exIO, "Use non-canonical form");
			ds.a("file", file);
			probe.warnFile(ds, file);
			return file;
		}
	}

	public static void deleteFile(File ex)
			throws ArgonPermissionException {
		if (ex == null) throw new IllegalArgumentException("object is null");
		final String oqm = deleteFileImp(ex);
		if (oqm != null) throw new ArgonPermissionException("Could not " + oqm);
	}

	public static boolean deleteFile(IArgonFileProbe probe, File ex) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (ex == null) throw new IllegalArgumentException("object is null");
		final String oqm = deleteFileImp(ex);
		if (oqm == null) return true;
		final Ds ds = Ds.invalidBecause("Could not delete file", "Will remain on file system");
		probe.warnFile(ds, ex);
		return false;
	}

	public static FileOutputStream newFileOutputStream(File destFile)
			throws ArgonPermissionException {
		if (destFile == null) throw new IllegalArgumentException("object is null");
		try {
			return new FileOutputStream(destFile);
		} catch (final FileNotFoundException ex) {
			throw new ArgonPermissionException("Cannot open '" + destFile + "' for output");
		}
	}

	public static File newSubstitutedFile(File ref, String qccNeoName) {
		if (ref == null) throw new IllegalArgumentException("object is null");
		if (qccNeoName == null || qccNeoName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final File oRefParent = ref.getParentFile();
		final File dest;
		if (oRefParent == null) {
			dest = new File(qccNeoName);
		} else {
			dest = new File(oRefParent, qccNeoName);
		}
		return dest;
	}

	public static File ocnfile(File file) {
		if (file == null) throw new IllegalArgumentException("object is null");
		try {
			return file.getCanonicalFile();
		} catch (final IOException ex) {
		}
		return null;
	}

	public static void renameFile(File from, File dest)
			throws ArgonPermissionException {
		if (from == null) throw new IllegalArgumentException("object is null");
		if (dest == null) throw new IllegalArgumentException("object is null");
		final String oqm = renameFileImp(from, dest);
		if (oqm != null) throw new ArgonPermissionException("Could not " + oqm);
	}

	public static boolean renameFile(IArgonFileProbe probe, File from, File dest) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (from == null) throw new IllegalArgumentException("object is null");
		if (dest == null) throw new IllegalArgumentException("object is null");
		final String oqm = renameFileImp(from, dest);
		if (oqm == null) return true;
		final Ds ds = Ds.invalidBecause("Could not rename file", "Will remain on file system under old name");
		ds.a("operation", oqm);
		probe.failFile(ds, from);
		return false;
	}

	private ArgonFileManagement() {
	}

}
