/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton.wdt;

import java.io.File;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonArgsException;
import com.metservice.argon.ArgonFormatException;
import com.metservice.argon.ArgonPermissionException;
import com.metservice.argon.Ds;
import com.metservice.argon.file.ArgonFileManifest;

/**
 * @author roach
 */
public class Command {

	private static void main(Props props)
			throws ArgonArgsException, ArgonFormatException, ArgonPermissionException {
		if (props == null) throw new IllegalArgumentException("object is null");
		final Pattern oAcceptPattern = props.oNCPattern();
		final boolean verbose = props.verbose();
		final boolean timing = props.timing();
		final ArgonFileManifest manifest = ArgonFileManifest.newInstance(props.qtwNCPath(), null, oAcceptPattern, null);
		final File[] zptFilesAscPath = manifest.zptFilesAscPath();
		int countGood = 0;
		int countFail = 0;
		for (int i = 0; i < zptFilesAscPath.length; i++) {
			final File inFile = zptFilesAscPath[i];
			final Transcoder t = new Transcoder(props, inFile);
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
				System.err.println(Ds.format(ex));
				System.err.println("Start of trace...");
				System.err.println(t.traceText());
				System.err.println("...End of trace");
				countFail++;
			}
		}
		System.out.println(String.format("Failed %d, Converted %d", countFail, countGood));

	}

	public static void main(String[] args) {
		try {
			final Props props = Props.newInstance(args);
			main(props);
		} catch (final ArgonArgsException | ArgonFormatException | ArgonPermissionException ex) {
			System.err.println(ex.getMessage());
		}
	}

}
