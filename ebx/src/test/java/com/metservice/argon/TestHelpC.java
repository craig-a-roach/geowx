/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.File;

import com.metservice.argon.file.ArgonDirectoryManagement;

/**
 * @author roach
 */
public class TestHelpC {

	public static final String Vendor = "unittest.metservice";
	public static final ArgonServiceId SID = new ArgonServiceId(Vendor, "argon");

	public static File cndirScratch(String key) {
		try {
			final File cndir = ArgonDirectoryManagement.cndirEnsureUserWriteable(Vendor, "argon-" + key);
			ArgonDirectoryManagement.removeExceptSelf(cndir);
			return cndir;
		} catch (final ArgonPermissionException ex) {
			throw new IllegalStateException("Cannot create argon scratch " + key + "..." + ex.getMessage());
		}
	}
}
