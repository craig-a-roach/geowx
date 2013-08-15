/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.esri;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.Binary;
import com.metservice.argon.file.ArgonDirectoryManagement;

/**
 * @author roach
 */
public class TestFolder {

	public static final String WorkDir = "com.metservice.gallium.ut";

	public static final TestFolder Instance = new TestFolder();

	public Path newFile(String sourceFileName) {
		return newFile(sourceFileName, WorkDir, sourceFileName);
	}

	public Path newFile(String sourceFileName, String targetDir) {
		return newFile(sourceFileName, targetDir, sourceFileName);
	}

	public Path newFile(String sourceFileName, String targetDir, String targetFileName) {
		if (sourceFileName == null || sourceFileName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (targetDir == null || targetDir.length() == 0) throw new IllegalArgumentException("string is null or empty");
		if (targetFileName == null || targetFileName.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		final Binary oBin = Binary.createFromClassPath(getClass(), sourceFileName);
		if (oBin == null) throw new IllegalArgumentException("resource not found>" + sourceFileName + "<");
		final File ocndir = ArgonDirectoryManagement.ocndirUserWriteable(targetDir);
		if (ocndir == null) throw new IllegalStateException("Cannot create working directory");
		final File dst = new File(ocndir, targetFileName);
		try {
			oBin.save(dst, false);
			return dst.toPath();
		} catch (final ArgonPermissionException ex) {
			throw new IllegalStateException(ex.getMessage());
		} catch (final ArgonStreamWriteException ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}

	public void scrub(Path oPath) {
		if (oPath == null) return;
		try {
			Files.delete(oPath);
		} catch (final IOException ex) {
			System.err.println("Failed to clean up " + oPath);
		}
	}

	private TestFolder() {
	}
}
