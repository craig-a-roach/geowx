/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.metservice.argon.ArgonText;

/**
 * @author roach
 */
public class BoronInterpreterWinCmdDefault implements IBoronScriptInterpreter {

	public static final String BinPath = "cmd";
	public static final String BinOpt1 = "/c";
	public static final String Suffix = ".cmd";

	@Override
	public BoronInterpreterId id() {
		return BoronInterpreterId.IntrinsicWinCmd;
	}

	@Override
	public ProcessBuilder newProcessBuilder(File cnScriptFile) {
		final List<String> zlArgs = new ArrayList<String>(4);
		zlArgs.add(BinPath);
		zlArgs.add(BinOpt1);
		zlArgs.add(cnScriptFile.getPath());
		return new ProcessBuilder(zlArgs);
	}

	@Override
	public String qccScriptName(String qccBaseName) {
		return qccBaseName + Suffix;
	}

	@Override
	public Charset scriptEncoding() {
		return ArgonText.ISO8859_1;
	}

	@Override
	public String zScriptLineTerminator() {
		return "\r\n";
	}

	public BoronInterpreterWinCmdDefault() {
	}
}
