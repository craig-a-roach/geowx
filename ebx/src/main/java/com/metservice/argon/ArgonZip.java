/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author roach
 */
public class ArgonZip {

	public static final int FileBufferBc = 64 * CArgon.K;
	public static final String DirSuffixWip = ".wip";
	private static final String CsqNoCreate = "Cannot create ZIP file";

	private static void cleanupWip(File cndirWip) {
		assert cndirWip != null;
		if (cndirWip.isDirectory()) {
			final File[] ozptMembers = cndirWip.listFiles();
			if (ozptMembers != null) {
				for (int i = 0; i < ozptMembers.length; i++) {
					final File member = ozptMembers[i];
					cleanupWip(member);
				}
			}
		}
		cndirWip.delete();
	}

	private static void closeSilent(InputStream oins) {
		if (oins != null) {
			try {
				oins.close();
			} catch (final IOException ex) {
			}
		}
	}

	private static void closeSilent(OutputStream oos) {
		if (oos != null) {
			try {
				oos.close();
			} catch (final IOException ex) {
			}
		}
	}

	private static void decodeToDirectoryFile(File sourceFile, ZipInputStream zins, File cnTarget, long oTsLastModified,
			byte[] buffer)
			throws ArgonPermissionException, ArgonStreamReadException, ArgonStreamWriteException {
		assert sourceFile != null;
		assert zins != null;
		assert cnTarget != null;
		assert buffer != null;

		final FileOutputStream fos;
		try {
			fos = new FileOutputStream(cnTarget);
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Create target decompressed file", ex, ArgonPermissionException.class);
			ds.a("sourceFile", sourceFile);
			ds.a("cnTarget", cnTarget);
			throw new ArgonPermissionException(ds.s());
		}

		boolean reading = true;
		long bcWritten = 0L;
		String oqCloseFail = null;
		try {
			boolean eof = false;
			while (!eof) {
				final int bc = zins.read(buffer);
				if (bc == -1) {
					eof = true;
					continue;
				}
				reading = false;
				fos.write(buffer, 0, bc);
				bcWritten += bc;
			}
		} catch (final IOException ex) {
			if (reading) {
				final Ds ds = Ds.triedTo("Read compressed stream component", ex, ArgonStreamReadException.class);
				ds.a("sourceFile", sourceFile);
				ds.a("bcWritten", bcWritten);
				throw new ArgonStreamReadException(ds.s());
			}
			final Ds ds = Ds.triedTo("Writing compressed stream component", ex, ArgonStreamWriteException.class);
			ds.a("targetFile", cnTarget);
			ds.a("sourceFile", sourceFile);
			ds.a("bcWritten", bcWritten);
			throw new ArgonStreamWriteException(ds.s());
		} finally {
			try {
				fos.close();
			} catch (final IOException ex) {
				oqCloseFail = ex.getMessage();
			}
		}
		if (oqCloseFail != null) {
			final Ds ds = Ds.invalidBecause("Output file was not closed cleanly", ArgonStreamWriteException.class);
			ds.a("targetFile", cnTarget);
			ds.a("bcWritten", bcWritten);
			throw new ArgonStreamWriteException(ds.s());
		}
		if (oTsLastModified >= 0L) {
			cnTarget.setLastModified(oTsLastModified);
		}
	}

	private static void expandContents(List<File> dest, File container) {
		assert dest != null;
		assert container != null;
		final File[] ozptFiles = container.listFiles();
		if (ozptFiles == null) return;
		for (int i = 0; i < ozptFiles.length; i++) {
			final File n = ozptFiles[i];
			if (n.isDirectory()) {
				expandContents(dest, n);
			} else {
				dest.add(n);
			}
		}
	}

	private static void finish(ZipOutputStream zos)
			throws ArgonStreamWriteException {
		try {
			zos.finish();
		} catch (final IOException ex) {
			throw new ArgonStreamWriteException("Could not finalise the ZIP output stream", ex);
		}
	}

	private static void mkdirs(File cnTarget)
			throws ArgonPermissionException {
		assert cnTarget != null;
		cnTarget.mkdirs();
		if (!cnTarget.canWrite()) {
			final String m = "Could not create sub-directory '" + cnTarget + "'";
			throw new ArgonPermissionException(m);
		}
	}

	private static void unlockSilent(FileLock ofileLock) {
		if (ofileLock != null) {
			try {
				ofileLock.release();
			} catch (final IOException ex) {
			}
		}
	}

	private static String zFilter(String[] ozptPrefixFilter, String zccEntryName) {
		if (ozptPrefixFilter == null) return zccEntryName;
		if (zccEntryName.length() == 0) return zccEntryName;
		final int depth = ozptPrefixFilter.length;
		if (depth == 0) return zccEntryName;
		for (int i = depth - 1; i >= 0; i--) {
			final String filter = ozptPrefixFilter[i];
			if (zccEntryName.startsWith(filter)) return zccEntryName.substring(filter.length());
		}
		return zccEntryName;
	}

	public static void decodeToDirectory(File sourceFile, File dirTarget, String[] ozptPrefixFilter)
			throws ArgonApiException, ArgonPermissionException, ArgonStreamReadException, ArgonStreamWriteException {
		if (sourceFile == null) throw new IllegalArgumentException("object is null");
		if (dirTarget == null) throw new IllegalArgumentException("object is null");

		final File cndirTarget;
		try {
			cndirTarget = dirTarget.getCanonicalFile();
		} catch (final IOException ex) {
			final String m = "Cannot resolve target directory '" + dirTarget + "' (" + ex.getMessage() + ")";
			throw new ArgonPermissionException(m);
		}

		File ocndirWip = null;
		if (cndirTarget.exists()) {
			ocndirWip = new File(cndirTarget.getPath() + DirSuffixWip);
			cleanupWip(ocndirWip);
			final boolean renamed = cndirTarget.renameTo(ocndirWip);
			if (!renamed) {
				final String m = "Could not rename target root directory '" + cndirTarget + "' to work-in-progress name ("
						+ ocndirWip + ")";
				throw new ArgonPermissionException(m);
			}
		}
		cndirTarget.mkdirs();
		if (!cndirTarget.canWrite()) {
			final String m = "Could not create target root directory '" + cndirTarget + "'";
			throw new ArgonPermissionException(m);
		}

		final Map<File, DirTimestamp> zmDir_Timestamp = new HashMap<File, DirTimestamp>(64);

		final String zccTargetName = cndirTarget.getName();
		if (zccTargetName.length() == 0) throw new ArgonApiException("Malformed target path '" + dirTarget + "'");

		final FileInputStream fis;
		try {
			fis = new FileInputStream(sourceFile);
		} catch (final FileNotFoundException ex) {
			return;
		}
		final byte[] buffer = new byte[FileBufferBc];
		final ZipInputStream zins = new ZipInputStream(new BufferedInputStream(fis));
		try {
			boolean moreEntries = true;
			while (moreEntries) {
				final ZipEntry oEntry = zins.getNextEntry();
				if (oEntry == null) {
					moreEntries = false;
					continue;
				}
				final String qccEntryName = oEntry.getName();
				final long oTsLastModified = oEntry.getTime();
				final String zccCanonName = zFilter(ozptPrefixFilter, qccEntryName);
				if (zccCanonName.length() == 0) {
					continue;
				}
				final File cnTarget = new File(cndirTarget, zccCanonName);
				final boolean isDirectory = oEntry.isDirectory();
				final File ocndirTarget;
				if (isDirectory) {
					ocndirTarget = cnTarget;
				} else {
					ocndirTarget = cnTarget.getParentFile();
				}
				if (ocndirTarget != null) {
					DirTimestamp vFileTimestamp = zmDir_Timestamp.get(ocndirTarget);
					if (vFileTimestamp == null) {
						final int ordinal = zmDir_Timestamp.size();
						vFileTimestamp = new DirTimestamp(ocndirTarget, oTsLastModified, ordinal);
						zmDir_Timestamp.put(ocndirTarget, vFileTimestamp);
						mkdirs(ocndirTarget);
					}
				}
				if (!isDirectory) {
					decodeToDirectoryFile(sourceFile, zins, cnTarget, oTsLastModified, buffer);
				}
			}
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Read ZIP stream", ex, ArgonStreamReadException.class);
			ds.a("sourceFile", sourceFile);
			ds.a("dirTarget", dirTarget);
			throw new ArgonStreamReadException(ds.s());
		} finally {
			closeSilent(zins);
		}

		final List<DirTimestamp> zlTimestamps = new ArrayList<DirTimestamp>(zmDir_Timestamp.values());
		Collections.sort(zlTimestamps);
		final int tsCount = zlTimestamps.size();
		for (int i = tsCount - 1; i >= 0; i--) {
			zlTimestamps.get(i).apply();
		}

		cndirTarget.setLastModified(sourceFile.lastModified());

		if (ocndirWip != null) {
			cleanupWip(ocndirWip);
		}
	}

	public static void encodeToFile(File sourceDirectory, File destFile, File oBaseDir, String[] ozptPrefix, boolean lockSource,
			char pathSeparator)
			throws ArgonApiException, ArgonPermissionException, ArgonStreamWriteException, ArgonStreamReadException {
		if (sourceDirectory == null) throw new IllegalArgumentException("object is null");
		final List<File> zlSourceFiles = new ArrayList<File>(128);
		expandContents(zlSourceFiles, sourceDirectory);
		final File[] zptSourceFiles = zlSourceFiles.toArray(new File[zlSourceFiles.size()]);
		encodeToFile(zptSourceFiles, destFile, oBaseDir, ozptPrefix, lockSource, pathSeparator);
	}

	public static void encodeToFile(File[] sourceFiles, File destFile, File oBaseDir, String[] ozptPrefix, boolean lockSource,
			char pathSeparator)
			throws ArgonApiException, ArgonPermissionException, ArgonStreamWriteException, ArgonStreamReadException {
		if (sourceFiles == null) throw new IllegalArgumentException("object is null");
		if (destFile == null) throw new IllegalArgumentException("object is null");

		final FileOutputStream ofos;
		try {
			ofos = new FileOutputStream(destFile);
		} catch (final FileNotFoundException ex) {
			final String m = "Could not create destination file '" + destFile + "'";
			throw new ArgonPermissionException(m);
		}
		final int fileCount = sourceFiles.length;
		final long[] zptLengths = new long[fileCount];
		long bcMax = 0L;
		for (int i = 0; i < sourceFiles.length; i++) {
			final long bc = sourceFiles[i].length();
			zptLengths[i] = bc;
			bcMax = Math.max(bcMax, bc);
		}
		final int bcBuffer = (int) Math.min(FileBufferBc, bcMax);
		final byte[] srcBuffer = new byte[bcBuffer];
		final ZipOutputStream zos = new ZipOutputStream(ofos);
		boolean zosFail = false;
		try {
			final PathComponents baseComponents;
			if (oBaseDir == null) {
				final PathComponents destComponents = new PathComponents(destFile);
				baseComponents = destComponents.newParent();
			} else {
				baseComponents = new PathComponents(oBaseDir);
			}

			for (int i = 0; i < sourceFiles.length; i++) {
				final File sourceFile = sourceFiles[i];
				final long tsLastModified = sourceFile.lastModified();
				final long bcSourceFile = zptLengths[i];
				final PathComponents sourceComponents = new PathComponents(sourceFile);
				final String oqccRelative = baseComponents.oqccRelativePath(sourceComponents, ozptPrefix, pathSeparator);
				final String qccSourcePath = oqccRelative == null ? sourceFile.getAbsolutePath() : oqccRelative;
				final ZipEntry ze = new ZipEntry(qccSourcePath);
				ze.setMethod(ZipOutputStream.DEFLATED);
				ze.setTime(tsLastModified);
				ze.setSize(bcSourceFile);
				try {
					zos.putNextEntry(ze);
				} catch (final IOException ex) {
					zosFail = true;
					final Ds ds = Ds.triedTo("Start new ZIP output entry", ex, CsqNoCreate);
					ds.afileinfo("sourceFile", sourceFile);
					ds.a("sourcePath", qccSourcePath);
					throw new ArgonStreamWriteException(ds.s());
				} finally {
					if (zosFail) {
						closeSilent(zos);
					}
				}
				FileInputStream ofis = null;
				try {
					ofis = new FileInputStream(sourceFile);
				} catch (final FileNotFoundException ex) {
				}
				if (ofis == null) {
					continue;
				}
				final FileChannel fch = ofis.getChannel();
				FileLock olock = null;
				try {
					try {
						if (lockSource) {
							olock = fch.lock(0, bcSourceFile, true);
						}
					} catch (final IOException ex) {
						zosFail = true;
						final Ds ds = Ds.triedTo("Obtain shared lock on source file", ex, CsqNoCreate);
						ds.afileinfo("sourceFile", sourceFile);
						ds.a("sourcePath", qccSourcePath);
						throw new ArgonApiException(ds.s());
					} finally {
						if (zosFail) {
							closeSilent(zos);
						}
					}
					boolean moreSource = true;
					while (moreSource) {
						final int bcRead;
						try {
							bcRead = ofis.read(srcBuffer);
						} catch (final IOException ex) {
							zosFail = true;
							final Ds ds = Ds.triedTo("Read source file into buffer", ex, CsqNoCreate);
							ds.afileinfo("sourceFile", sourceFile);
							ds.a("sourcePath", qccSourcePath);
							throw new ArgonStreamReadException(ds.s());
						} finally {
							if (zosFail) {
								closeSilent(zos);
							}
						}
						if (bcRead < 0) {
							moreSource = false;
							continue;
						}
						try {
							zos.write(srcBuffer, 0, bcRead);
						} catch (final IOException ex) {
							zosFail = true;
							final Ds ds = Ds.triedTo("Write source buffer to ZIP output stream", ex, CsqNoCreate);
							ds.afileinfo("sourceFile", sourceFile);
							ds.a("sourcePath", qccSourcePath);
							ds.a("bcRead", bcRead);
							throw new ArgonStreamReadException(ds.s());
						} finally {
							if (zosFail) {
								closeSilent(zos);
							}
						}
					}
				} finally {
					unlockSilent(olock);
					closeSilent(ofis);
				}
				try {
					zos.closeEntry();
				} catch (final IOException ex) {
					zosFail = true;
					final Ds ds = Ds.triedTo("End new ZIP output entry", ex, CsqNoCreate);
					ds.afileinfo("sourceFile", sourceFile);
					ds.a("sourcePath", qccSourcePath);
					throw new ArgonStreamWriteException(ds.s());
				} finally {
					if (zosFail) {
						closeSilent(zos);
					}
				}
			}
			finish(zos);
		} finally {
			closeSilent(zos);
		}
	}

	private static class DirTimestamp implements Comparable<DirTimestamp> {

		public void apply() {
			if (oTsLastModified >= 0L) {
				file.setLastModified(oTsLastModified);
			}
		}

		@Override
		public int compareTo(DirTimestamp rhs) {
			if (ordinal < rhs.ordinal) return -1;
			if (ordinal > rhs.ordinal) return +1;
			return 0;
		}

		@Override
		public String toString() {
			return file.getPath() + " #" + ordinal + (oTsLastModified >= 0L ? (" @" + oTsLastModified) : "");
		}

		public DirTimestamp(File file, long oTsLastModified, int ordinal) {
			this.file = file;
			this.oTsLastModified = oTsLastModified;
			this.ordinal = ordinal;
		}
		final File file;
		final long oTsLastModified;
		final int ordinal;
	}
}
