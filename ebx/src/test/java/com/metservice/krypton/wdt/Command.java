/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.wdt;

import java.io.File;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonArgs;
import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.ArgonProperties;
import com.metservice.argon.file.ArgonFileManifest;

/**
 * @author roach
 */
public class Command {

	private static final String ARG_PFILE = "pfile:p";
	private static final String ARG_IN = "in:i";
	private static final String PNAME_NCPATH = "ncpath";
	private static final String PNAME_NCPATTERN = "ncpattern";
	private static final String PDEFAULT_NCPATTERN = ".+[.]nc";

	private static void main(ArgonProperties ap)
			throws ArgonArgsException, ArgonFormatException, ArgonPermissionException {
		if (ap == null) throw new IllegalArgumentException("object is null");
		final Pattern oAcceptPattern = ap.select(PNAME_NCPATTERN).opattern();
		final ArgonFileManifest manifest = ArgonFileManifest.newInstance(ap.select(PNAME_NCPATH).qtwValue, null,
				oAcceptPattern, null);
		final File[] zptFilesAscPath = manifest.zptFilesAscPath();
		for (int i = 0; i < zptFilesAscPath.length; i++) {
			final Transcoder transcoder = new Transcoder(ap, zptFilesAscPath[i]);
			transcoder.newResult();
		}
	}

	public static void main(String[] args) {
		try {
			final ArgonArgs aa = new ArgonArgs(args);
			final ArgonProperties.BuilderFromArgs b = ArgonProperties.newBuilder(aa);
			b.putProperty(PNAME_NCPATTERN, PDEFAULT_NCPATTERN);
			b.putFiles(ARG_PFILE);
			b.putAssignments();
			b.putMappedArg(ARG_IN, PNAME_NCPATH);
			b.printlnUnsupportedMessage();
			main(b.newProperties());
		} catch (final ArgonArgsException | ArgonFormatException | ArgonPermissionException ex) {
			System.err.println(ex.getMessage());
		}
	}

}
