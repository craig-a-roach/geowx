/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import java.util.Arrays;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
public class EsBreakpointLines {

	public EsBreakpointLines add(int lineNo) {
		final int lineIndex = Math.max(0, lineNo - 1);
		final int exLen = m_zptLineIndexAsc.length;
		final int[] zptLineIndexAsc = new int[exLen + 1];
		System.arraycopy(m_zptLineIndexAsc, 0, zptLineIndexAsc, 0, exLen);
		zptLineIndexAsc[exLen] = lineIndex;
		Arrays.sort(zptLineIndexAsc);
		return new EsBreakpointLines(zptLineIndexAsc);
	}

	public boolean hit(int lineIndex) {
		return Arrays.binarySearch(m_zptLineIndexAsc, lineIndex) >= 0;
	}

	public EsBreakpointLines remove(int lineNo) {
		final int lineIndex = lineNo - 1;
		final int pos = Arrays.binarySearch(m_zptLineIndexAsc, lineIndex);
		if (pos < 0) return this;
		final int exLen = m_zptLineIndexAsc.length;
		if (exLen == 1) return None;
		final int[] zptLineIndexAsc = new int[exLen - 1];
		for (int i = 0; i < pos; i++) {
			zptLineIndexAsc[i] = m_zptLineIndexAsc[i];
		}
		for (int i = pos + 1; i < exLen; i++) {
			zptLineIndexAsc[i - 1] = m_zptLineIndexAsc[i];
		}
		return new EsBreakpointLines(zptLineIndexAsc);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("zptLineIndexAsc", m_zptLineIndexAsc);
		return ds.s();
	}

	public int[] zptLineIndexAsc() {
		return m_zptLineIndexAsc;
	}

	public static EsBreakpointLines newInstance(int lineSerial) {
		final int lineIndex = Math.max(0, lineSerial - 1);
		final int[] zptLineIndexAsc = new int[1];
		zptLineIndexAsc[0] = lineIndex;
		return new EsBreakpointLines(zptLineIndexAsc);
	}

	private EsBreakpointLines(int[] zptLineIndexAsc) {
		assert zptLineIndexAsc != null;
		m_zptLineIndexAsc = zptLineIndexAsc;
	}

	public static final EsBreakpointLines None = new EsBreakpointLines(new int[0]);

	private final int[] m_zptLineIndexAsc;
}
