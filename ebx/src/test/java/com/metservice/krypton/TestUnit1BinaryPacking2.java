/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1BinaryPacking2 {

	// final float[] data = { 8.5f, 7.5f, 15.5f, -1.5f, 5.0f, 0.0f };

	private static String bin(int x) {
		final String s = Integer.toBinaryString(x);
		final int sl = s.length();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, j = sl; i < sl; i++, j--) {
			if (i > 0 && (j % 4) == 0) {
				sb.append(' ');
			}
			sb.append(s.charAt(i));
		}
		return sb.toString();
	}

	private static int bitsToBytes(int bitCount) {
		final int f = bitCount >> 3;
		final int r = f - (f << 3);
		return r == 0 ? f : (f + 1);
	}

	private static int[] decode(Source src, int bitDepth, int dataCount) {
		final IntegerFactory factory = new IntegerFactory(src);
		final int[] decode = new int[dataCount];
		for (int i = 0; i < decode.length; i++) {
			decode[i] = factory.nextInteger(bitDepth);
		}
		return decode;
	}

	// |11 1010 1011 |10 1100 1101|00 1110 1111|01 0010 0011|
	// |11 1010 10|11 10 1100|1101 00 11|10 1111 01|0010 0011|

	private static int dout(String tag, int x) {
		System.out.println(tag + ": " + x);
		return x;
	}

	private static int[][] newDataRamp() {
		final int[] data1 = { 0x1, 0x0, 0x1, 0x1, 0x0, 0x1, 0x0, 0x1, 0x1, 0x1 };
		final int[] data2 = { 0x3, 0x2, 0x3, 0x2, 0x3, 0x2, 0x3, 0x2 };
		final int[] data3 = { 0x6, 0x7, 0x5, 0x7, 0x5, 0x7, 0x6, 0x5 };
		final int[] data4 = { 0xB, 0xD, 0xF, 0xC, 0xF, 0xA, 0xF, 0xB };
		final int[] data5 = { 0x1B, 0x1D, 0x1F, 0x1C, 0x1F, 0x1A, 0x1F, 0x1B };
		final int[] data6 = { 0x3B, 0x2D, 0x3F, 0x2C, 0x3F, 0x2A, 0x3F, 0x2B };
		final int[] data7 = { 0x7B, 0x6D, 0x5F, 0x7C, 0x6F, 0x5A, 0x7F, 0x6B };
		final int[] data8 = { 0xAB, 0xBD, 0xCF, 0xDC, 0xEF, 0xFA, 0xAF, 0xCB };
		final int[] data9 = { 0x1AB, 0x1BD, 0x1CF, 0x1DC, 0x1EF, 0x1FA, 0x1AF, 0x1CB };
		final int[] data10 = { 0x3AB, 0x2CD, 0x3EF, 0x2AC, 0x3BF, 0x2DA, 0x3CF, 0x2DB };
		final int[] data11 = { 0x7AB, 0x6CD, 0x5EF, 0x7AC, 0x6BF, 0x5DA, 0x5CF, 0x7DB };
		final int[] data12 = { 0xFAB, 0xECD, 0xDEF, 0xCAC, 0xFBF, 0xCDA, 0xACF, 0xBDB };
		final int[] data13 = { 0x1FAB, 0x1ECD, 0x1DEF, 0x1CAC, 0x1FBF, 0x1CDA, 0x1ACF, 0x1BDB };
		final int[] data14 = { 0x3FAB, 0x2ECD, 0x3DEF, 0x2CAC, 0x3FBF, 0x2CDA, 0x3ACF, 0x2BDB };
		final int[] data15 = { 0x7FAB, 0x6ECD, 0x5DEF, 0x4CAC, 0x7FBF, 0x6CDA, 0x5ACF, 0x4BDB };
		final int[] data16 = { 0xEFAB, 0xFECD, 0xBDEF, 0xBCAC, 0xCFBF, 0xFCDA, 0xDACF, 0xEBDB };
		final int[] data17 = { 0x1EFAB, 0x1FECD, 0x1BDEF, 0x1BCAC, 0x1CFBF, 0x1FCDA, 0x1DACF, 0x1EBDB };
		final int[] data18 = { 0x3EFAB, 0x2FECD, 0x3BDEF, 0x2BCAC, 0x3CFBF, 0x2FCDA, 0x3DACF, 0x2EBDB };
		final int[] data19 = { 0x7EFAB, 0x6FECD, 0x5BDEF, 0x4BCAC, 0x7CFBF, 0x5FCDA, 0x4DACF, 0x7EBDB };
		final int[] data20 = { 0xBEFAB, 0xCFECD, 0xABDEF, 0x9BCAC, 0xDCFBF, 0xEFCDA, 0xBDACF, 0x8EBDB };
		final int[] data21 = { 0x1BEFAB, 0x1CFECD, 0x1ABDEF, 0x19BCAC, 0x1DCFBF, 0x1EFCDA, 0x1BDACF, 0x18EBDB };
		final int[] data22 = { 0x3BEFAB, 0x2CFECD, 0x3ABDEF, 0x29BCAC, 0x3DCFBF, 0x2EFCDA, 0x3BDACF, 0x28EBDB };
		final int[] data23 = { 0x7BEFAB, 0x6CFECD, 0x5ABDEF, 0x49BCAC, 0x7DCFBF, 0x6EFCDA, 0x5BDACF, 0x48EBDB };
		final int[] data24 = { 0xFBEFAB, 0xDCFECD, 0xEABDEF, 0xC9BCAC, 0xBDCFBF, 0xAEFCDA, 0xCBDACF, 0xD8EBDB };
		final int[][] data = { null, data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12,
				data13, data14, data15, data16, data17, data18, data19, data20, data21, data22, data23, data24 };
		return data;
	}

	private static int[] newSampleData(int[] depthData, int limit) {
		final int[] sampleData = new int[limit];
		for (int i = 0; i < limit; i++) {
			sampleData[i] = depthData[i];
		}
		return sampleData;
	}

	private static byte out(String tag, byte b) {
		final int x = 0xFF & b;
		System.out.println(tag + ": " + bin(x) + " [" + Integer.toHexString(x) + "] " + x);
		return b;
	}

	private static byte[] out(String tag, byte[] ba) {
		for (int i = 0; i < ba.length; i++) {
			out(tag + "[" + i + "]", ba[i]);
		}
		return ba;
	}

	private static int out(String tag, int x) {
		System.out.println(tag + ": " + bin(x) + " [" + Integer.toHexString(x) + "] " + x);
		return x;
	}

	private void encode(int[] data, int bitDepth, Section2Buffer dst) {
		if (data.length == 0) return;
		if (bitDepth % 8 == 0) {
			encodeAligned(data, bitDepth, dst);
		} else {
			encodeUnaligned(data, bitDepth, dst);
		}
	}

	private void encodeAligned(int[] xptData, int bitDepth, Section2Buffer dst) {
		final int dataOctetCount = xptData.length;
		final int sampleOctets = bitDepth >> 3;
		final byte[] packBuffer = new byte[sampleOctets];
		for (int i = 0; i < dataOctetCount; i++) {
			final int datum = xptData[i];
			UGrib.pack(packBuffer, datum);
			dst.octets(packBuffer);
		}
	}

	private void encodeUnaligned(int[] xptData, int bitDepth, Section2Buffer dst) {
		final int datumCount = xptData.length;
		int bufferR = 0;
		int bufferBitCount = 0;
		for (int datumIndex = 0; datumIndex < datumCount; datumIndex++) {
			final int datum = xptData[datumIndex];
			bufferR = (bufferR << bitDepth) | datum;
			bufferBitCount += bitDepth;
			while (bufferBitCount >= 8) {
				final int shift = bufferBitCount - 8;
				final int mask = 0xFF << shift;
				final int dm = (bufferR & mask) >>> shift;
				dst.octet(dm);
				bufferR = bufferR & ~mask;
				bufferBitCount -= 8;
			}
		}
		if (bufferBitCount > 0) {
			final int bufferL = bufferR << (8 - bufferBitCount);
			dst.octet(bufferL);
		}
	}

	private void encodeUnalignedT(int[] xptData, int bitDepth, Section2Buffer dst) {
		final int datumCount = xptData.length;
		int bufferR = 0;
		int bufferBitCount = 0;
		for (int datumIndex = 0; datumIndex < datumCount; datumIndex++) {
			final int datum = out("datum@" + datumIndex, xptData[datumIndex]);
			bufferR = out("bufferR", (bufferR << bitDepth) | datum);
			bufferBitCount = dout("bufferBitCount", bufferBitCount + bitDepth);
			while (bufferBitCount >= 8) {
				final int shift = dout("shift", bufferBitCount - 8);
				final int mask = out("mask", 0xFF << shift);
				final int dm = out("dm", (bufferR & mask) >>> shift);
				dst.octet(dm);
				bufferR = out("bufferR", bufferR & ~mask);
				bufferBitCount = dout("bufferBitCount", bufferBitCount - 8);
			}
		}
		if (bufferBitCount > 0) {
			final int bufferL = out("bufferL", bufferR << (8 - bufferBitCount));
			dst.octet(bufferL);
		}
	}

	@Test
	public void a00() {
		final float[] m_xptSparseData = { 101.4f, 102.7f, Float.NaN, 101.9f, 100.3f, Float.NaN, 101.6f, 101.2f, Float.NaN,
				Float.NaN, 101.0f, 100.8f, Float.NaN, Float.NaN, Float.NaN, 101.5f, Float.NaN };
		final Section2Buffer dst = new Section2Buffer(6, 1);
		final int gridLen = m_xptSparseData.length;
		final int reqdBytes = bitsToBytes(gridLen);
		dst.increaseCapacityBy(reqdBytes);
		int buffer = 0;
		int bufferBitCount = 0;
		for (int i = 0; i < gridLen; i++) {
			final boolean isMissing = Float.isNaN(m_xptSparseData[i]);
			buffer <<= 1;
			if (!isMissing) {
				buffer |= 1;
			}
			out("buffer", buffer);
			bufferBitCount++;
			if (bufferBitCount == 8) {
				dst.octet(buffer);
				bufferBitCount = 0;
				buffer = 0;
			}
		}
		if (bufferBitCount > 0) {
			final int bufferL = buffer << (8 - bufferBitCount);
			out("buffer", bufferL);
			dst.octet(bufferL);
		}
		final Source src = new Source(dst);
		System.out.println(src);
	}

	@Test
	public void a10() {
		final int[][] data = newDataRamp();
		final int bitDepth = 3;
		final int limit = 5;
		final int[] sampleData = newSampleData(data[bitDepth], limit);
		final Section2Buffer dst = new Section2Buffer(7, 1);
		encode(sampleData, bitDepth, dst);
		final Source src = new Source(dst);
		final int[] decode = decode(src, bitDepth, limit);
		Assert.assertArrayEquals(sampleData, decode);
	}

	@Test
	public void t50() {
		final int[][] data = newDataRamp();
		for (int bitDepth = 1; bitDepth < data.length; bitDepth++) {
			System.out.println("BITDEPTH: " + bitDepth);
			final int[] depthData = data[bitDepth];
			for (int limit = 1; limit < depthData.length; limit++) {
				try {
					final int[] sampleData = newSampleData(depthData, limit);
					final Section2Buffer dst = new Section2Buffer(7, 1);
					encode(sampleData, bitDepth, dst);
					final Source src = new Source(dst);
					final int[] decode = decode(src, bitDepth, limit);
					Assert.assertArrayEquals("bitDepth:" + bitDepth + "/limit:" + limit, sampleData, decode);
				} catch (final RuntimeException ex) {
					Assert.fail("Runtime at bitDepth " + bitDepth + " limit " + limit);
				}
			}
		}
	}

	private static class Source implements IOctetIndexer {

		@Override
		public int firstDataOctetIndex() {
			return m_firstDataOctetIndex;
		}

		@Override
		public int octetValue(int octetPos) {
			return m_encoded[octetPos] & 0xFF;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			for (int i = m_firstDataOctetIndex; i < m_encoded.length; i++) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append("0x");
				sb.append(Integer.toHexString(m_encoded[i] & 0xFF));
			}
			return sb.toString();
		}

		public Source(Section2Buffer buffer) {
			if (buffer == null) throw new IllegalArgumentException("object is null");
			m_encoded = buffer.emit();
			m_firstDataOctetIndex = Section2Buffer.FirstDataOctetIndex;
		}

		private final byte[] m_encoded;
		private final int m_firstDataOctetIndex;
	}
}
