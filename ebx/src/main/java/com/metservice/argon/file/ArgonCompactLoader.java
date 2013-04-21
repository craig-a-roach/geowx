/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonQuotaException;
import com.metservice.argon.ArgonStreamReadException;
import com.metservice.argon.Binary;
import com.metservice.argon.IArgonFileProbe;

/**
 * @author roach
 */
public class ArgonCompactLoader {

	public static Properties findProperties(File path)
			throws ArgonStreamReadException {
		if (path == null) throw new IllegalArgumentException("object is null");
		FileInputStream ofis = null;
		try {
			ofis = new FileInputStream(path);
			final Properties p = new Properties();
			p.load(ofis);
			return p;
		} catch (final FileNotFoundException ex) {
			return null;
		} catch (final IOException ex) {
			throw new ArgonStreamReadException("Cannot read properties file '" + path + "'", ex);
		} finally {
			if (ofis != null) {
				try {
					ofis.close();
				} catch (final IOException ex) {
				}
			}
		}
	}

	public static Binary load(IArgonFileProbe probe, File path, boolean lock, int bcQuota)
			throws ArgonApiException, ArgonQuotaException, ArgonStreamReadException {
		return FIn.loadBinary(probe, path, lock, bcQuota);
	}

	private ArgonCompactLoader() {
	}
}
