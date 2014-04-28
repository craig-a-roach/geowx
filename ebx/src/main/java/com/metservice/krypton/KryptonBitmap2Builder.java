/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonBitmap2Builder extends KryptonSection2Builder {

	@Override
	void save(Section2Buffer dst) {
		dst.u1(m_indicator);
		if (m_oEmitter != null) {
			m_oEmitter.saveBitmap(dst);
		}
	}

	@Override
	public int estimatedOctetCount() {
		final int bmc = m_oEmitter == null ? 0 : m_oEmitter.bitmapByteCount();
		return 6 + bmc;
	}

	@Override
	public int sectionNo() {
		return 6;
	}

	public KryptonBitmap2Builder(IBitmap2Emitter oEmitter) {
		if (oEmitter == null || !oEmitter.requiresBitmap()) {
			m_indicator = Table6_0.DoesNotApply;
			m_oEmitter = null;
		} else {
			m_indicator = Table6_0.Supplied;
			m_oEmitter = oEmitter;
		}
	}

	public KryptonBitmap2Builder(int indicator) {
		m_indicator = indicator;
		m_oEmitter = null;
	}

	private final int m_indicator;
	private final IBitmap2Emitter m_oEmitter;

	public static class Table6_0 {

		public static final int Supplied = 0;
		public static final int PreviouslyDefined = 254;
		public static final int DoesNotApply = 255;
	}
}
