/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emboron;

import java.nio.charset.Charset;
import java.util.List;

import com.metservice.argon.Elapsed;
import com.metservice.boron.BoronInterpreterId;
import com.metservice.boron.BoronLineTerminator;
import com.metservice.boron.BoronStdioPrompt;
import com.metservice.boron.IBoronScript;
import com.metservice.boron.IBoronScriptResource;
import com.metservice.neon.EsExecutionContext;

/**
 * 
 * @author roach
 */
class ScriptTuple implements IBoronScript {

	@Override
	public int bcBufferStdErr() {
		return m_bcBufferStdErr;
	}

	@Override
	public int bcBufferStdIn() {
		return m_bcBufferStdIn;
	}

	@Override
	public int bcBufferStdOut() {
		return m_bcBufferStdOut;
	}

	@Override
	public Elapsed getExitTimeout() {
		return m_oExitTimeout;
	}

	@Override
	public BoronStdioPrompt getStdioPrompt() {
		return null;
	}

	@Override
	public BoronInterpreterId interpreterId() {
		return m_interpreterId;
	}

	@Override
	public int maxFeedQueueDepth() {
		return m_maxFeedQueueDepth;
	}

	@Override
	public int maxProductQueueDepth() {
		return m_maxProductQueueDepth;
	}

	@Override
	public boolean redirectStdErrToOut() {
		return m_redirectStdErrToOut;
	}

	@Override
	public Charset stdioEncoding() {
		return m_stdioEncoding;
	}

	@Override
	public byte[] stdioLineTerminator() {
		return m_stdioLineTerminator;
	}

	@Override
	public List<String> zlLines() {
		return m_construct.xlzLines;
	}

	@Override
	public List<IBoronScriptResource> zlResources() {
		return m_construct.zlResources;
	}

	public ScriptTuple(EsExecutionContext ecx, ScriptConstruct construct, ScriptEm src) throws InterruptedException {
		if (construct == null) throw new IllegalArgumentException("object is null");
		if (src == null) throw new IllegalArgumentException("object is null");
		m_construct = construct;

		m_interpreterId = BoronInterpreterId.newInstance(src.property_qtwString(ecx, CProp.interpreter));
		m_oExitTimeout = src.property_oElapsed(ecx, CProp.exitTimeout);
		m_redirectStdErrToOut = src.property_boolean(CProp.redirectStdErrToOut);
		m_stdioEncoding = src.property_charset(ecx, CProp.stdioEncoding, CProp.stdioEncoding_default);
		m_stdioLineTerminator = BoronLineTerminator.select(src.property_ozString(ecx, CProp.stdioLineTerminator));
		m_maxProductQueueDepth = src.property_int(ecx, CProp.maxProductQueueDepth);
		m_maxFeedQueueDepth = src.property_int(ecx, CProp.maxFeedQueueDepth);
		m_bcBufferStdOut = src.property_int(ecx, CProp.stdOutBufferSize);
		m_bcBufferStdErr = src.property_int(ecx, CProp.stdErrBufferSize);
		m_bcBufferStdIn = src.property_int(ecx, CProp.stdInBufferSize);
	}

	private final ScriptConstruct m_construct;
	private final BoronInterpreterId m_interpreterId;
	private final Elapsed m_oExitTimeout;
	private final boolean m_redirectStdErrToOut;
	private final Charset m_stdioEncoding;
	private final byte[] m_stdioLineTerminator;
	private final int m_maxProductQueueDepth;
	private final int m_maxFeedQueueDepth;
	private final int m_bcBufferStdOut;
	private final int m_bcBufferStdErr;
	private final int m_bcBufferStdIn;
}
