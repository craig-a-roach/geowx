/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
class BitMesh {

	private final static int ADDRESS_BITS_PER_WORD = 6;

	private static int wordIndex(int bitIndex) {
		return bitIndex >> ADDRESS_BITS_PER_WORD;
	}

	private boolean outbounds(int x, int y) {
		return x < 0 || x >= m_w || y < 0 || y >= m_h;
	}

	public boolean clear(int x, int y) {
		if (outbounds(x, y)) return false;
		final int index = (y * m_w) + x;
		final int iw = wordIndex(index);
		final long mask = 1L << index;
		final long current = m_words[iw];
		final boolean exValue = (current & mask) != 0;
		final long neo = current & (~mask);
		m_words[iw] = neo;
		return exValue;
	}

	public int height() {
		return m_h;
	}

	public void set(int x, int y, boolean value) {
		if (outbounds(x, y)) {
			final String arg = "x=" + x + ",y=" + y + ", w=" + m_w + ", h=" + m_h;
			throw new IllegalArgumentException("Out of bounds (" + arg + ")");
		}
		final int index = (y * m_w) + x;
		final int iw = wordIndex(index);
		final long mask = 1L << index;
		final long current = m_words[iw];
		final long neo = value ? (current | mask) : (current & (~mask));
		m_words[iw] = neo;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int y = m_h - 1; y >= 0; y--) {
			for (int x = 0; x < m_w; x++) {
				final char ch = value(x, y) ? '*' : '.';
				sb.append(ch);
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	public boolean value(int x, int y) {
		if (outbounds(x, y)) return false;
		final int index = (y * m_w) + x;
		final int iw = wordIndex(index);
		final long mask = 1L << index;
		final long current = m_words[iw];
		return (current & mask) != 0;
	}

	public int width() {
		return m_w;
	}

	public BitMesh(int width, int height) {
		if (width < 1) throw new IllegalArgumentException("non-positive width " + width);
		if (height < 1) throw new IllegalArgumentException("non-positive height " + height);
		m_w = width;
		m_h = height;
		final int len = height * width;
		final int wc = wordIndex(len - 1) + 1;
		m_words = new long[wc];
	}
	private final int m_w;
	private final int m_h;
	private final long[] m_words;
}
