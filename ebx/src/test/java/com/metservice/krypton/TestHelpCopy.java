/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.io.File;
import java.io.InputStream;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.Binary;
import com.metservice.argon.CArgon;
import com.metservice.argon.file.ArgonDirectoryManagement;

/**
 * @author roach
 */
public class TestHelpCopy {

	public static File newFile(Object tester, String resourceSubDir, String resourceName) {
		if (tester == null) throw new IllegalArgumentException("object is null");
		if (resourceSubDir == null || resourceSubDir.length() == 0)
			throw new IllegalArgumentException("string is null or empty");
		if (resourceName == null || resourceName.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final String resourcePath = resourceSubDir + "/" + resourceName;
		final InputStream ins = tester.getClass().getResourceAsStream(resourcePath);
		try {
			final Binary resource = Binary.newFromInputStream(ins, 1 * CArgon.M);
			final File cndir = ArgonDirectoryManagement.cndirEnsureUserWriteable("unitest.krypton", resourceSubDir);
			final File saveFile = new File(cndir, resourceName);
			resource.save(saveFile, false);
			return saveFile;
		} catch (final ArgonQuotaException ex) {
			throw new IllegalStateException("Cannot read " + resourcePath, ex);
		} catch (final ArgonStreamReadException ex) {
			throw new IllegalStateException("Cannot read " + resourcePath, ex);
		} catch (final ArgonPermissionException ex) {
			throw new IllegalStateException("Cannot create " + resourceSubDir, ex);
		} catch (final ArgonStreamWriteException ex) {
			throw new IllegalStateException("Cannot write " + resourcePath, ex);
		}
	}
}
