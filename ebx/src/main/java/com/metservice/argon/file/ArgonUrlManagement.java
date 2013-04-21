/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonApiException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonText;
import com.metservice.argon.CArgon;
import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class ArgonUrlManagement {

	private static final Pattern RegExCanon = Pattern.compile("\\w+[:].+");

	private static URL newProtocolUrl(String spec)
			throws ArgonApiException {
		if (spec == null || spec.length() == 0) throw new IllegalArgumentException("string is null or empty");
		try {
			return new URL(spec);
		} catch (final MalformedURLException ex) {
			final String m = "Malformed url '" + spec + "'..." + Ds.message(ex);
			throw new ArgonApiException(m);
		}
	}

	private static URL newSubUrl(URL parent, boolean ensureReadable, String... zptSubComponents)
			throws ArgonApiException {
		if (parent == null) throw new IllegalArgumentException("object is null");
		if (zptSubComponents == null) throw new IllegalArgumentException("object is null");

		final int depth = zptSubComponents.length;
		if (depth == 0) return parent;

		final StringBuilder sbNeo = new StringBuilder();
		sbNeo.append(parent.toExternalForm());
		for (int i = 0; i < depth; i++) {
			final String ozSub = zptSubComponents[i];
			final String oqtwSub = UArgonFile.oqtwCleanNode(ozSub);
			if (oqtwSub != null) {
				sbNeo.append('/').append(oqtwSub);
			}
		}
		return newUrl(sbNeo.toString(), ensureReadable);
	}

	private static URL newUrl(String ozSpec, boolean ensureReadable)
			throws ArgonApiException {
		final String oqtwSpec = ArgonText.oqtw(ozSpec);
		final String qtwSpec = oqtwSpec == null ? CArgon.UserHomeNode : oqtwSpec;
		if (qtwSpec.startsWith(CArgon.UserHomeNode)) {
			final String ztwUser = qtwSpec.substring(CArgon.UserHomeNodeL);
			return newFileUrl(ztwUser, true, ensureReadable);
		}
		if (RegExCanon.matcher(qtwSpec).matches()) return newProtocolUrl(qtwSpec);
		return newFileUrl(qtwSpec, false, ensureReadable);
	}

	public static URL newFileUrl(String zccPath, boolean userRelative, boolean ensureReadable)
			throws ArgonApiException {
		if (zccPath == null) throw new IllegalArgumentException("object is null");
		try {
			final File cndir;
			if (ensureReadable) {
				if (userRelative) {
					cndir = ArgonDirectoryManagement.cndirEnsureUserReadable(zccPath);
				} else {
					cndir = ArgonDirectoryManagement.cndirEnsureReadable(zccPath);
				}
			} else {
				if (userRelative) {
					cndir = ArgonDirectoryManagement.cndirUser(zccPath);
				} else {
					cndir = ArgonDirectoryManagement.cndir(zccPath);
				}
			}
			return newProtocolUrl("file://" + cndir.getPath());
		} catch (final ArgonPermissionException ex) {
			final String pt = userRelative ? "user" : "";
			final String m = "Cannot construct file url for " + pt + " path '" + zccPath;
			throw new ArgonApiException(m + "'..." + ex.getMessage());
		}
	}

	public static URL newReadableSubUrl(URL parent, String... zptSubComponents)
			throws ArgonApiException {
		return newSubUrl(parent, true, zptSubComponents);
	}

	public static URL newReadableUrl(String ozSpec)
			throws ArgonApiException {
		return newUrl(ozSpec, true);
	}

	public static URL newSubUrl(URL parent, String... zptSubComponents)
			throws ArgonApiException {
		return newSubUrl(parent, false, zptSubComponents);
	}

	public static URL newUrl(String ozSpec)
			throws ArgonApiException {
		return newUrl(ozSpec, false);
	}

	private ArgonUrlManagement() {
	}
}
