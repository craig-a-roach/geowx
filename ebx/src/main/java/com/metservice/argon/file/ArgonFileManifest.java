/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonSplitter;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class ArgonFileManifest {

	private static void addDirectory(Set<String> zscanonPath, File cndir, Pattern oAcceptName, Pattern oRejectName) {
		final File[] ozptFiles = cndir.listFiles();
		if (ozptFiles == null) return;
		for (int i = 0; i < ozptFiles.length; i++) {
			try {
				final File cnf = ozptFiles[i].getCanonicalFile();
				addPath(zscanonPath, cnf, oAcceptName, oRejectName);
			} catch (final IOException ex) {
			}
		}
	}

	private static void addFile(Set<String> zscanonPath, File cnfile, Pattern oAcceptName, Pattern oRejectName) {
		if (!cnfile.canRead()) return;
		final String qccName = cnfile.getName();
		if (oAcceptName != null && !oAcceptName.matcher(qccName).matches()) return;
		if (oRejectName != null && oRejectName.matcher(qccName).matches()) return;
		zscanonPath.add(cnfile.getAbsolutePath());
	}

	private static void addPath(Set<String> zscanonPath, File cn, Pattern oAcceptName, Pattern oRejectName) {
		if (cn.isFile()) {
			addFile(zscanonPath, cn, oAcceptName, oRejectName);
		} else if (cn.isDirectory()) {
			addDirectory(zscanonPath, cn, oAcceptName, oRejectName);
		}
	}

	private static String msgCannotRead(String qtwPath) {
		final String u = " ...Process user is " + UArgonFile.qUserName();
		return "Cannot read '" + qtwPath + "' " + u;
	}

	private static String msgCannotResolve(String qtwPath, IOException ex) {
		final String u = " ...Process user is " + UArgonFile.qUserName();
		final String c = " (" + ex.getMessage() + ")";
		return "Cannot resolve path '" + qtwPath + "' " + c + u;
	}

	public static ArgonFileManifest newInstance(String zSpec, Pattern oDelimiter, Pattern oAcceptName, Pattern oRejectName)
			throws ArgonPermissionException {
		if (zSpec == null) throw new IllegalArgumentException("object is null");
		final String ztwSpec = zSpec.trim().replace("~", UArgonFile.qUserHome());
		String[] zptqtwPaths;
		if (oDelimiter == null) {
			zptqtwPaths = new String[1];
			zptqtwPaths[0] = ztwSpec;
		} else {
			zptqtwPaths = ArgonSplitter.zptqtwSplit(ztwSpec, oDelimiter);
		}
		return newInstance(zptqtwPaths, oAcceptName, oRejectName);
	}

	public static ArgonFileManifest newInstance(String[] zptqtwPaths, Pattern oAcceptName, Pattern oRejectName)
			throws ArgonPermissionException {
		if (zptqtwPaths == null) throw new IllegalArgumentException("object is null");
		final int pathCount = zptqtwPaths.length;
		final Set<String> zscanonPath = new HashSet<String>(pathCount);
		for (int i = 0; i < pathCount; i++) {
			final String qtwPath = zptqtwPaths[i];
			if (qtwPath == null || qtwPath.length() == 0) {
				final String m = "path at index " + i + " is null or empty";
				throw new IllegalArgumentException(m);
			}
			try {
				final File f = new File(qtwPath);
				final File cnf = f.getCanonicalFile();
				if (!cnf.canRead()) throw new ArgonPermissionException(msgCannotRead(qtwPath));
				addPath(zscanonPath, cnf, oAcceptName, oRejectName);
			} catch (final IOException ex) {
				throw new ArgonPermissionException(msgCannotResolve(qtwPath, ex));
			}
		}
		final List<String> zlCanonPathsAsc = new ArrayList<String>(zscanonPath);
		Collections.sort(zlCanonPathsAsc);
		final int count = zlCanonPathsAsc.size();
		final File[] zptFilesAscPath = new File[count];
		for (int i = 0; i < count; i++) {
			zptFilesAscPath[i] = new File(zlCanonPathsAsc.get(i));
		}
		return new ArgonFileManifest(zptFilesAscPath);
	}

	public Map<String, File> newFileNameMap() {
		if (m_zptFilesAscPath.length == 0) return Collections.emptyMap();
		final Map<String, File> xm = new HashMap<String, File>(m_zptFilesAscPath.length);
		for (int i = 0; i < m_zptFilesAscPath.length; i++) {
			final File f = m_zptFilesAscPath[i];
			xm.put(f.getName(), f);
		}
		return xm;
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("files", m_zptFilesAscPath);
		return ds.s();
	}

	public File[] zptFilesAscPath() {
		return m_zptFilesAscPath;
	}

	private ArgonFileManifest(File[] zptFilesAscPath) {
		assert zptFilesAscPath != null;
		m_zptFilesAscPath = zptFilesAscPath;
	}

	private final File[] m_zptFilesAscPath;
}
