/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.nio.charset.Charset;

/**
 * @author roach
 */
public interface IBoronScriptInterpreter {

	public BoronInterpreterId id();

	public ProcessBuilder newProcessBuilder(File cnScriptFile);

	public String qccScriptName(String qccBaseName);

	public Charset scriptEncoding();

	public String zScriptLineTerminator();
}
