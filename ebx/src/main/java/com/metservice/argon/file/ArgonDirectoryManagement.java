/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.Ds;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
public class ArgonDirectoryManagement {

	public static final File CnDirRoot = cndirInit(new File("/"));
	public static final File CnDirUserHome = cndirInit(new File(UArgonFile.qUserHome()));

	private static File cndirInit(File dir) {
		assert dir != null;
		try {
			return dir.getCanonicalFile();
		} catch (final IOException ex) {
			final String u = " ...Process user is " + UArgonFile.qUserName();
			final String c = " (" + ex.getMessage() + ")";
			final String m = "Cannot resolve path to directory '" + dir + "' " + c + u;
			throw new UnsupportedOperationException(m);
		}
	}

	private static boolean removeImp(IArgonFileProbe oprobe, File container, boolean retainSelf) {
		if (container == null) throw new IllegalArgumentException("object is null");
		boolean removedAll = true;
		if (container.exists()) {
			if (container.isDirectory()) {
				final File[] ozptMembers = container.listFiles();
				if (ozptMembers != null) {
					for (int i = 0; i < ozptMembers.length; i++) {
						final File member = ozptMembers[i];
						final boolean removed = remove(member, false);
						removedAll = removedAll && removed;
					}
				}
			}
			if (!retainSelf) {
				final boolean deleted = container.delete();
				removedAll = removedAll && deleted;
				if (oprobe != null && !deleted) {
					final Ds ds = Ds.invalidBecause("Delete failed", "Will remain on file system");
					oprobe.warnFile(ds, container);
				}
			}
		}
		return removedAll;
	}

	public static File cndir(File dir)
			throws ArgonPermissionException {
		if (dir == null) throw new IllegalArgumentException("object is null");
		try {
			return dir.getCanonicalFile();
		} catch (final IOException ex) {
			final String u = " ...Process user is " + UArgonFile.qUserName();
			final String c = " (" + ex.getMessage() + ")";
			final String m = "Cannot resolve path to directory '" + dir + "' " + c + u;
			throw new ArgonPermissionException(m);
		}
	}

	public static File cndir(String path)
			throws ArgonPermissionException {
		if (path == null || path.length() == 0) throw new IllegalArgumentException("string is null or empty");
		return cndir(new File(path));
	}

	public static File cndirEnsureReadable(File cndir)
			throws ArgonPermissionException {
		if (cndir == null) throw new IllegalArgumentException("object is null");
		if (cndir.canRead()) return cndir;
		final String u = " ...Process user is " + UArgonFile.qUserName();
		final String msg = "Cannot read directory '" + cndir + "'" + u;
		throw new ArgonPermissionException(msg);
	}

	public static File cndirEnsureReadable(String path)
			throws ArgonPermissionException {
		return cndirEnsureReadable(cndir(path));
	}

	public static File cndirEnsureSubReadable(File parent, String... zptSubComponents)
			throws ArgonPermissionException {
		return cndirEnsureReadable(cndirSub(parent, zptSubComponents));
	}

	public static File cndirEnsureSubWriteable(File parent, String... zptSubComponents)
			throws ArgonPermissionException {
		return cndirEnsureWriteable(cndirSub(parent, zptSubComponents));
	}

	public static File cndirEnsureUserReadable(String... zptSubComponents)
			throws ArgonPermissionException {
		return cndirEnsureSubReadable(CnDirUserHome, zptSubComponents);
	}

	public static File cndirEnsureUserWriteable(String... zptSubComponents)
			throws ArgonPermissionException {
		return cndirEnsureSubWriteable(CnDirUserHome, zptSubComponents);
	}

	public static File cndirEnsureWriteable(File cndir)
			throws ArgonPermissionException {
		if (cndir == null) throw new IllegalArgumentException("object is null");
		if (cndir.canWrite()) return cndir;
		cndir.mkdirs();
		if (cndir.canWrite()) return cndir;
		final String u = " ...Process user is " + UArgonFile.qUserName();
		final String msg = "Cannot create directory '" + cndir + u;
		throw new ArgonPermissionException(msg);
	}

	public static File cndirEnsureWriteable(String path)
			throws ArgonPermissionException {
		return cndirEnsureReadable(cndir(path));
	}

	public static File cndirSub(File parent, String... zptSubComponents)
			throws ArgonPermissionException {
		return cndir(dirSub(parent, zptSubComponents));
	}

	public static File cndirUser(String... zptSubComponents)
			throws ArgonPermissionException {
		return cndirSub(CnDirUserHome, zptSubComponents);
	}

	public static File dirSub(File parent, String... zptSubComponents) {
		if (parent == null) throw new IllegalArgumentException("object is null");
		if (zptSubComponents == null) throw new IllegalArgumentException("object is null");

		File dir = parent;
		final int depth = zptSubComponents.length;
		for (int i = 0; i < depth; i++) {
			final String ozSub = zptSubComponents[i];
			final String oqtwSub = UArgonFile.oqtwCleanNode(ozSub);
			if (oqtwSub != null) {
				dir = new File(dir, oqtwSub);
			}
		}
		return dir;
	}

	public static void expandContents(List<File> dest, File container, boolean recursive, boolean includeFiles,
			boolean includeSubdirectories) {
		if (dest == null) throw new IllegalArgumentException("object is null");
		if (container == null) throw new IllegalArgumentException("object is null");
		final File[] ozptFiles = container.listFiles();
		if (ozptFiles == null) return;
		for (int i = 0; i < ozptFiles.length; i++) {
			final File n = ozptFiles[i];
			if (n.isDirectory()) {
				if (recursive) {
					expandContents(dest, n, true, includeFiles, includeSubdirectories);
				}
				if (includeSubdirectories) {
					dest.add(n);
				}
			} else {
				if (includeFiles) {
					dest.add(n);
				}
			}
		}
	}

	public static File ocndir(File dir) {
		if (dir == null) throw new IllegalArgumentException("object is null");
		try {
			return dir.getCanonicalFile();
		} catch (final IOException ex) {
		}
		return null;
	}

	public static File ocndirSub(File parent, String... zptSubComponents) {
		return ocndir(dirSub(parent, zptSubComponents));
	}

	public static File ocndirSubReadable(File parent, String... zptSubComponents) {
		final File ocndir = ocndirSub(parent, zptSubComponents);
		return ocndir != null && ocndir.canRead() ? ocndir : null;
	}

	public static File ocndirSubWriteable(File parent, String... zptSubComponents) {
		final File ocndir = ocndirSub(parent, zptSubComponents);
		if (ocndir == null) return null;
		if (ocndir.canWrite()) return ocndir;
		ocndir.mkdirs();
		if (ocndir.canWrite()) return ocndir;
		return null;
	}

	public static File ocndirUserReadable(String... zptSubComponents) {
		return ocndirSubReadable(CnDirUserHome, zptSubComponents);
	}

	public static File ocndirUserWriteable(String... zptSubComponents) {
		return ocndirSubWriteable(CnDirUserHome, zptSubComponents);
	}

	public static boolean remove(File container, boolean retainSelf) {
		if (container == null) throw new IllegalArgumentException("object is null");
		return removeImp(null, container, retainSelf);
	}

	public static boolean remove(IArgonFileProbe probe, File container, boolean retainSelf) {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (container == null) throw new IllegalArgumentException("object is null");
		return removeImp(probe, container, retainSelf);
	}

	public static boolean removeExceptSelf(File container) {
		return remove(container, true);
	}

	public static boolean removeSelfOnlyIfEmpty(File container) {
		if (container == null) throw new IllegalArgumentException("object is null");
		return container.delete();
	}

	public static boolean removeWithSelf(File container) {
		return remove(container, false);
	}

	public static void renameDirectory(File from, File dest)
			throws ArgonPermissionException {
		if (from == null) throw new IllegalArgumentException("object is null");
		if (dest == null) throw new IllegalArgumentException("object is null");
		String pre = "";
		if (dest.exists()) {
			if (remove(dest, false)) {
				pre = " (pre-delete was successful)";
			} else {
				pre = " (pre-delete failed)";
			}
		}
		final boolean renamed = from.renameTo(dest);
		if (!renamed) {
			final String m = "Could not rename '" + from + "' to '" + dest + "'" + pre;
			throw new ArgonPermissionException(m);
		}
	}

	public static boolean renameDirectory(File dir, Pattern leaf, String zccReplacement)
			throws ArgonPermissionException {
		if (dir == null) throw new IllegalArgumentException("object is null");
		if (leaf == null) throw new IllegalArgumentException("object is null");
		if (zccReplacement == null) throw new IllegalArgumentException("object is null");
		final String zccExLeaf = dir.getName();
		final int exLeafLength = zccExLeaf.length();
		if (exLeafLength == 0) return false;
		final Matcher matcher = leaf.matcher(zccExLeaf);
		if (!matcher.find()) return false;
		final int start;
		final int end;
		if (matcher.groupCount() == 0) {
			start = exLeafLength;
			end = exLeafLength;
		} else {
			start = matcher.start(1);
			end = matcher.end(1);
		}
		if (start < 0 || end < 0) return false;
		final StringBuilder bleaf = new StringBuilder();
		bleaf.append(zccExLeaf.substring(0, start));
		bleaf.append(zccReplacement);
		bleaf.append(zccExLeaf.substring(end));
		final File oParent = dir.getParentFile();
		final File dest;
		if (oParent == null) {
			dest = new File(bleaf.toString());
		} else {
			dest = new File(oParent, bleaf.toString());
		}
		renameDirectory(dir, dest);
		return true;
	}

	public static boolean renameDirectoryOnlyIfUnique(File from, File dest) {
		if (from == null) throw new IllegalArgumentException("object is null");
		if (dest == null) throw new IllegalArgumentException("object is null");
		if (dest.exists()) return false;
		return from.renameTo(dest);
	}

	public static File[] zptListContents(File container, boolean recursive, boolean includeFiles, boolean includeSubdirectories) {
		if (container == null) throw new IllegalArgumentException("object is null");
		final List<File> dest = new ArrayList<File>(64);
		expandContents(dest, container, recursive, includeFiles, includeSubdirectories);
		return dest.toArray(new File[dest.size()]);
	}

	private ArgonDirectoryManagement() {
	}
}
