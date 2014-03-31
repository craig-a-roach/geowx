/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.radon;

import java.io.File;

import org.junit.Test;

import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.Binary;
import com.metservice.argon.file.ArgonDirectoryManagement;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * @author roach
 */
public class TestUnit1Exp {

	@Test
	public void t50()
			throws ArgonPermissionException {
		final EnvironmentConfig envCfg = new EnvironmentConfig();
		envCfg.setAllowCreate(true);
		final File cndir = ArgonDirectoryManagement.cndirEnsureUserWriteable("bdb", "exp");
		Environment oEnv = null;
		Database oDb = null;
		try {
			oEnv = new Environment(cndir, envCfg);
			final DatabaseConfig dbCfg = new DatabaseConfig();
			dbCfg.setAllowCreate(true);
			oDb = oEnv.openDatabase(null, "tseries", dbCfg);
			final Binary kb = Binary.newFromStringASCII("atom|93439");
			@SuppressWarnings("unused")
			final DatabaseEntry key = new DatabaseEntry(kb.zptReadOnly);
		} finally {
			if (oDb != null) {
				try {
					oDb.close();
				} catch (final DatabaseException ex) {
					ex.printStackTrace();
				}
			}
			if (oEnv != null) {
				try {
					oEnv.close();
				} catch (final DatabaseException ex) {
					ex.printStackTrace();
				}
			}
		}

	}

}
