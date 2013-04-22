/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class IntegerFactory {

	private int read() {
		final int sample = m_src.octetValue(m_octetPos);
		m_octetPos++;
		return sample;
	}

	public void clearBuffer() {
		m_bitPos = 0;
		m_bitBuf = 0;
	}

	public int nextInteger(int bitCount) {
		if (m_bitPos == 0) {
			m_bitBuf = read();
			m_bitPos = 8;
		}

		int result = 0;
		boolean more = true;
		int bitsLeft = bitCount;
		while (more) {
			final int shift = bitsLeft - m_bitPos;
			if (shift > 0) {
				result |= m_bitBuf << shift;
				bitsLeft -= m_bitPos;
				m_bitBuf = read();
				m_bitPos = 8;
			} else {
				result |= m_bitBuf >> -shift;
				m_bitPos -= bitsLeft;
				m_bitBuf &= 0xFF >> (8 - m_bitPos);
				more = false;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "@" + m_octetPos + " bit" + m_bitPos;
	}

	public IntegerFactory(IOctetIndexer src) {
		if (src == null) throw new IllegalArgumentException("object is null");
		m_src = src;
		m_octetPos = src.firstDataOctetIndex();
	}
	private final IOctetIndexer m_src;
	private int m_octetPos;
	private int m_bitBuf;
	private int m_bitPos;
}
