/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Elapsed;

/**
 * @author roach
 */
public class BoronDiagnosticScript implements IBoronScript {

	private static StringBuilder space(StringBuilder sb, Object... zptParts) {
		for (int i = 0; i < zptParts.length; i++) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			sb.append(zptParts[i].toString());
		}
		return sb;
	}

	public static BoronDiagnosticScript javaVersion() {
		final List<String> xl = new ArrayList<String>();
		final BoronInterpreterId ii = UBoron.detectDiagnosticInterpreterId();
		if (ii.equals(BoronInterpreterId.IntrinsicWinCmd)) {
			xl.add("@echo off");
		}
		xl.add("java -version");
		return new BoronDiagnosticScript(ii, 30000L, 25000L, xl);
	}

	public static BoronDiagnosticScript ping(long msInterval, String qtwDest) {
		if (qtwDest == null || qtwDest.length() == 0) throw new IllegalArgumentException("string is null or empty");
		final long msExitTimeout = Math.max(30000L, msInterval * 2);
		final long secInterval = Math.max(1L, msInterval / 1000L);
		final BoronInterpreterId ii = UBoron.detectDiagnosticInterpreterId();
		final StringBuilder sb = new StringBuilder();
		sb.append("ping");
		if (ii.equals(BoronInterpreterId.IntrinsicWinCmd)) {
			space(sb, "-n", secInterval, "-w", msInterval);
		} else {
			space(sb, "-i 1", "-c", secInterval, "-W", secInterval);
		}
		sb.append(" ").append(qtwDest);
		final List<String> xl = new ArrayList<String>();
		xl.add(sb.toString());
		return new BoronDiagnosticScript(ii, msExitTimeout, msExitTimeout, xl);
	}

	public static BoronDiagnosticScript processList() {
		final List<String> xl = new ArrayList<String>();
		final BoronInterpreterId ii = UBoron.detectDiagnosticInterpreterId();
		if (ii.equals(BoronInterpreterId.IntrinsicWinCmd)) {
			xl.add("tasklist");
		} else {
			xl.add("ps -ef");
		}
		return new BoronDiagnosticScript(ii, 30000, 25000, xl);
	}

	@Override
	public int bcBufferStdErr() {
		return 1024;
	}

	@Override
	public int bcBufferStdIn() {
		return 512;
	}

	@Override
	public int bcBufferStdOut() {
		return 1024;
	}

	@Override
	public Elapsed getExitTimeout() {
		return m_exitTimeout;
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
		return 32;
	}

	@Override
	public int maxProductQueueDepth() {
		return 64;
	}

	public Elapsed productTimeout() {
		return m_productTimeout;
	}

	@Override
	public boolean redirectStdErrToOut() {
		return true;
	}

	@Override
	public Charset stdioEncoding() {
		return ArgonText.UTF8;
	}

	@Override
	public byte[] stdioLineTerminator() {
		return UBoron.detectLineTerminator();
	}

	@Override
	public List<String> zlLines() {
		return m_zlLines;
	}

	@Override
	public List<IBoronScriptResource> zlResources() {
		return Collections.emptyList();
	}

	private BoronDiagnosticScript(BoronInterpreterId ii, long msExitTimeout, long msProductTimeout, List<String> zlLines) {
		if (ii == null) throw new IllegalArgumentException("object is null");
		if (zlLines == null) throw new IllegalArgumentException("object is null");
		m_interpreterId = ii;
		m_exitTimeout = Elapsed.newInstance(msExitTimeout);
		m_productTimeout = Elapsed.newInstance(msProductTimeout);
		m_zlLines = zlLines;
	}
	private final BoronInterpreterId m_interpreterId;
	private final Elapsed m_exitTimeout;
	private final Elapsed m_productTimeout;
	private final List<String> m_zlLines;
}
