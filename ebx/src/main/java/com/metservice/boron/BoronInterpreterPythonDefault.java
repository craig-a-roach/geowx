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
public class BoronInterpreterPythonDefault implements IBoronScriptInterpreter {

	public static final String BinPath = "/usr/bin/python";
	public static final String Suffix = ".py";

	@Override
	public BoronInterpreterId id() {
		return BoronInterpreterId.IntrinsicPython;
	}

	@Override
	public ProcessBuilder newProcessBuilder(File cnScriptFile) {
		final List<String> zlArgs = new ArrayList<String>(4);
		zlArgs.add(BinPath);
		zlArgs.add(cnScriptFile.getPath());
		return new ProcessBuilder(zlArgs);
	}

	@Override
	public String qccScriptName(String qccBaseName) {
		return qccBaseName + Suffix;
	}

	@Override
	public Charset scriptEncoding() {
		return ArgonText.UTF8;
	}

	@Override
	public String zScriptLineTerminator() {
		return "\n";
	}

	public BoronInterpreterPythonDefault() {
	}
}
