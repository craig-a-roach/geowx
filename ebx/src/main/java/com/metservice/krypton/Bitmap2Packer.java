/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class Bitmap2Packer {

	private void flush() {
		m_octetArray[m_octetIndex] = m_buffer;
		m_octetIndex++;
		m_bitIndex = 0;
		m_buffer = 0x0;
	}

	public void add(boolean hasValue) {
		if (hasValue) {
			final int ex = m_buffer & 0xFF;
			m_buffer = (byte) (ex | 1 << m_bitIndex);
		}
		m_bitIndex++;
		if (m_bitIndex == 8) {
			flush();
		}
	}

	public void save(Section2Buffer dst) {
		if (m_bitIndex > 0) {
			flush();
		}
		dst.octets(m_octetArray);
	}

	public Bitmap2Packer(int valueCount) {
		int bc = valueCount >> 3;
		if (valueCount % 8 != 0) {
			bc++;
		}
		m_octetArray = new byte[bc];
	}
	private final byte[] m_octetArray;
	private byte m_buffer;
	private int m_octetIndex;
	private int m_bitIndex;
}
