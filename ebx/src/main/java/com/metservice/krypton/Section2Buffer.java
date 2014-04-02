/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class Section2Buffer {

	private static final int Min = 16;

	private void ensure(int plus) {
		final int neoOctet = m_octet + plus;
		if (neoOctet <= m_buffer.length) return;
		final int neoCap = Math.max(neoOctet, m_buffer.length * 3 / 2);
		final byte[] save = m_buffer;
		m_buffer = new byte[neoCap];
		System.arraycopy(save, 0, m_buffer, 0, m_octet);
	}

	public void u1(byte value) {
		ensure(1);
	}

	public void u1(short value) {

	}

	public Section2Buffer(int min) {
		m_buffer = new byte[Math.max(Min, min)];
	}

	private byte[] m_buffer;
	private int m_octet;
}
