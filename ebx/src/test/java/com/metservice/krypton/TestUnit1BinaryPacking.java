/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1BinaryPacking {

	// final float[] data = { 8.5f, 7.5f, 15.5f, -1.5f, 5.0f, 0.0f };
	private static int bitsToBytes(int bitCount) {
		final int b = bitCount / 8;
		return (bitCount % 8 == 0) ? b : (b + 1);
	}

	private void encoder(int[] data, int bitDepth, Destination dst) {
		final int dataOctetCount = data.length;
		if (dataOctetCount == 0) return;
		final int sampleOctets = bitDepth >> 3;
		final byte[] buffer = new byte[sampleOctets];
		final byte[] r24 = new byte[3];
		UGrib.pack24(r24, data[0]);
		for (int r = 3, w = sampleOctets - 1; w >= 0; r--, w--) {
			buffer[w] = r24[r];
		}

	}

	private void pack(int[] data, int bitDepth) {
		final int bitsReqd = data.length * bitDepth;
		final int bytesReqd = bitsToBytes(bitsReqd);
		final Destination dst = new Destination(bytesReqd);
		final IntegerFactory factory = new IntegerFactory(dst);
		encoder(data, bitDepth, dst);
		final int[] decode = new int[data.length];
		for (int i = 0; i < decode.length; i++) {
			decode[i] = factory.nextInteger(bitDepth);
		}
	}

	@Test
	public void t50() {
		final int[] data = { 0x3AB, 0x2CD, 0xEF, 0x123 };
		pack(data, 10);
	}

	private static class Destination implements IOctetIndexer {

		@Override
		public int firstDataOctetIndex() {
			return 0;
		}

		public void octet(byte value) {
			m_encoded[m_pos] = value;
			m_pos++;
		}

		public int octetCount() {
			return m_pos;
		}

		@Override
		public int octetValue(int octetPos) {
			return m_encoded[octetPos] & 0xFF;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < m_pos; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append("0x");
				sb.append(Integer.toHexString(m_encoded[i] & 0xFF));
			}
			return sb.toString();
		}

		public Destination(int bytesReqd) {
			m_encoded = new byte[bytesReqd];
		}
		private final byte[] m_encoded;
		private int m_pos;
	}
}
