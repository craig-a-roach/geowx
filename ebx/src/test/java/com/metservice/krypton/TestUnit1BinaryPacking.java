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

	@Test
	public void t50() {

		final int BITDEPTH = 10;
		final int[] Mask = { 0xFF << 24, 0xFF << 16, 0xFF << 8 };
		final int[] data = { 0x3AB, 0x2CD, 0xEF };
		final int bitsReqd = data.length * BITDEPTH;
		final int bytesReqd = bitsToBytes(bitsReqd);
		final Destination dst = new Destination(bytesReqd);
		int writeBuffer = 0x0;
		int avail = 32;
		for (int idata = 0; idata < data.length; idata++) {
			final int datum = data[idata];
			final int shift = avail - BITDEPTH;
			writeBuffer |= (datum << shift);
			System.out.println(Integer.toHexString(writeBuffer));
			avail -= BITDEPTH;
			if (avail < BITDEPTH) {
				for (int m = 0, s = 24; m < 3; m++, s -= 8) {
					final byte b = (byte) ((writeBuffer & Mask[m]) >> s);
					dst.octet(b);
				}
				final int neoAvail = 32 - avail;
				writeBuffer = writeBuffer << neoAvail;
				System.out.println(Integer.toHexString(writeBuffer));
				avail = neoAvail;
			}
		}
		if (avail < 32) {
			final byte b = (byte) (writeBuffer & 0xFF);
			dst.octet(b);
		}

		final IntegerFactory factory = new IntegerFactory(dst);
		final int[] decode = new int[data.length];
		for (int i = 0; i < decode.length; i++) {
			decode[i] = factory.nextInteger(BITDEPTH);
		}
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
