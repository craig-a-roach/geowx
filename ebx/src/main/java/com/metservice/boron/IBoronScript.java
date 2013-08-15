/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.nio.charset.Charset;
import java.util.List;

import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public interface IBoronScript {

	public int bcBufferStdErr();

	public int bcBufferStdIn();

	public int bcBufferStdOut();

	public Elapsed getExitTimeout();

	public BoronStdioPrompt getStdioPrompt();

	public BoronInterpreterId interpreterId();

	public int maxFeedQueueDepth();

	public int maxProductQueueDepth();

	public boolean redirectStdErrToOut();

	public Charset stdioEncoding();

	public byte[] stdioLineTerminator();

	public List<String> zlLines();

	public List<IBoronScriptResource> zlResources();
}
