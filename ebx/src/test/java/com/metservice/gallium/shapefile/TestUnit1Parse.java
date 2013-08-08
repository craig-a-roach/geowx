/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

import java.io.File;

import org.junit.Test;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonStreamWriteException;
import com.metservice.argon.Binary;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.metservice.argon.file.ArgonFileManagement;

/**
 * @author roach
 */
public class TestUnit1Parse {

	private File copyGHHS(String res, int level) {
		return newFile("GSHHS_" + res + "_L" + level + ".shp");
	}

	private File newFile(String fn) {
		final Binary oBin = Binary.createFromClassPath(getClass(), fn);
		if (oBin == null) throw new IllegalArgumentException("resource not found>" + fn + "<");
		final File ocndir = ArgonDirectoryManagement.ocndirUserWriteable("com.metservice.gallium.ut");
		if (ocndir == null) throw new IllegalStateException("Cannot create working directory");
		final File dst = new File(ocndir, fn);
		try {
			oBin.save(dst, false);
			return dst;
		} catch (final ArgonPermissionException ex) {
			throw new IllegalStateException(ex.getMessage());
		} catch (final ArgonStreamWriteException ex) {
			throw new IllegalStateException(ex.getMessage());
		}
	}

	private void scrub(File f) {
		try {
			ArgonFileManagement.deleteFile(f);
		} catch (final ArgonPermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void a10_gshhs() {
		File odst = null;
		try {
			odst = copyGHHS("c", 2);
		} finally {
			scrub(odts);
		}
	}

}
