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
	private static final String PNAME_VERBOSE = "verbose:v";
	private static final String PNAME_TIMING = "timing:t";
	private static final String PNAME_NCPATH = "ncpath:n";
	private static final String PNAME_NCPATTERN = "ncpattern";
	private static final String PDEFAULT_NCPATTERN = ".+[.]nc";

	private static void main(ArgonProperties ap)
			throws ArgonArgsException, ArgonFormatException, ArgonPermissionException {
		if (ap == null) throw new IllegalArgumentException("object is null");
		final Pattern oAcceptPattern = ap.select(PNAME_NCPATTERN).opattern();
		final boolean verbose = ap.select(PNAME_VERBOSE).flag();
		final boolean timing = ap.select(PNAME_TIMING).flag();
		final ArgonFileManifest manifest = ArgonFileManifest.newInstance(ap.select(PNAME_NCPATH).qtwValue, null,
				oAcceptPattern, null);
		final File[] zptFilesAscPath = manifest.zptFilesAscPath();
		int countGood = 0;
		int countFail = 0;
		for (int i = 0; i < zptFilesAscPath.length; i++) {
			final File inFile = zptFilesAscPath[i];
			final Transcoder t = new Transcoder(ap, inFile);
			try {
				if (verbose || timing) {
					System.out.print("Converting " + inFile + "...");
				}
				t.transcode();
				countGood++;
				if (verbose || timing) {
					System.out.println(" done in " + t.msElapsed() + " ms");
				}
				if (verbose) {
					System.out.println("Information...");
					System.out.println(t.traceText());
					System.out.println("---");
				}
			} catch (final TranscodeException ex) {
				System.err.println(ex.getMessage());
				System.err.println("Start of trace...");
				System.err.println(t.toString());
				System.err.println("...End of trace");
				countFail++;
			}
		}
		System.out.println(String.format("Failed %d, Converted %d", countFail, countGood));

	}

	public static void main(String[] args) {
		try {
			final ArgonArgs aa = new ArgonArgs(args);
			final ArgonProperties.BuilderFromArgs b = ArgonProperties.newBuilder(aa);
			b.putProperty(PNAME_NCPATTERN, PDEFAULT_NCPATTERN);
			b.putProperty(PNAME_TIMING, false);
			b.putProperty(PNAME_VERBOSE, false);
			b.putFiles(ARG_PFILE);
			b.putAssignments();
			b.putMappedArg(PNAME_NCPATH);
			b.putMappedFlag(PNAME_TIMING);
			b.putMappedFlag(PNAME_VERBOSE);
			b.printlnUnsupportedMessage();
			main(b.newProperties());
		} catch (final ArgonArgsException | ArgonFormatException | ArgonPermissionException ex) {
			System.err.println(ex.getMessage());
		}
	}

}
