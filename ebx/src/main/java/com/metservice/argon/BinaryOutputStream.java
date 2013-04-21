/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.argon;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author roach
 */
public class BinaryOutputStream extends OutputStream {

	@Override
	public void close()
			throws IOException {
	}

	public Binary newBinary() {
		return Binary.newFromTransient(m_buffer, m_count);
	}

	@Override
	public void write(byte b[], int off, int len) {
		if (b == null) throw new NullPointerException("No buffer supplied by caller");
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
			final String m = "Invalid offset (" + off + "), length (" + len + ") arguments supplied by caller";
			throw new IndexOutOfBoundsException(m);
		}

		if (len == 0) return;

		final int neoCount = m_count + len;
		if (neoCount > m_buffer.length) {
			final byte neoBuf[] = new byte[Math.max(m_buffer.length << 1, neoCount)];
			System.arraycopy(m_buffer, 0, neoBuf, 0, m_count);
			m_buffer = neoBuf;
		}
		System.arraycopy(b, off, m_buffer, m_count, len);
		m_count = neoCount;
	}

	@Override
	public void write(int b) {
		final int neoCount = m_count + 1;
		if (neoCount > m_buffer.length) {
			final byte neoBuf[] = new byte[Math.max(m_buffer.length << 1, neoCount)];
			System.arraycopy(m_buffer, 0, neoBuf, 0, m_count);
			m_buffer = neoBuf;
		}
		m_buffer[m_count] = (byte) b;
		m_count = neoCount;
	}

	public BinaryOutputStream(int initialCapacity) {
		m_buffer = new byte[initialCapacity];
	}

	private byte m_buffer[];
	private int m_count;
}
