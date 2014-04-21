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
public class TestUnit1BinaryPacking {

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

	private static int[] decode(Source src, int bitDepth, int dataCount) {
		final IntegerFactory factory = new IntegerFactory(src);
		final int[] decode = new int[dataCount];
		for (int i = 0; i < decode.length; i++) {
			decode[i] = factory.nextInteger(bitDepth);
		}
		return decode;
	}

	private static int dout(String tag, int x) {
		System.out.println(tag + ": " + x);
		return x;
	}

	private static int[][] newDataRamp() {
		final int[] data1 = { 0x1, 0x0, 0x1, 0x1, 0x0, 0x1, 0x0, 0x1, 0x1, 0x1 };
		final int[] data2 = { 0x3, 0x2, 0x3, 0x2, 0x3, 0x2, 0x3, 0x2 };
		final int[] data3 = { 0x7, 0x5, 0x6, 0x7, 0x5, 0x7, 0x6, 0x5 };
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
		final int[] data25 = { 0x1FBEFAB, 0x1DCFECD, 0x1EABDEF, 0x1C9BCAC, 0x1BDCFBF, 0x1AEFCDA, 0x1CBDACF, 0x1D8EBDB };
		final int[] data26 = { 0x3FBEFAB, 0x2DCFECD, 0x3EABDEF, 0x2C9BCAC, 0x3BDCFBF, 0x2AEFCDA, 0x3CBDACF, 0x2D8EBDB };
		final int[] data27 = { 0x7FBEFAB, 0x6DCFECD, 0x5EABDEF, 0x4C9BCAC, 0x7BDCFBF, 0x6AEFCDA, 0x5CBDACF, 0x4D8EBDB };
		final int[] data28 = { 0xEFBEFAB, 0xFDCFECD, 0xEEABDEF, 0xFC9BCAC, 0xDBDCFBF, 0xCAEFCDA, 0xACBDACF, 0xBD8EBDB };
		final int[] data29 = { 0x1EFBEFAB, 0x1FDCFECD, 0x1EEABDEF, 0x1FC9BCAC, 0x1DBDCFBF, 0x1CAEFCDA, 0x1ACBDACF, 0x1BD8EBDB };
		final int[][] data = { null, data1, data2, data3, data4, data5, data6, data7, data8, data9, data10, data11, data12,
				data13, data14, data15, data16, data17, data18, data19, data20, data21, data22, data23, data24, data25,
				data26, data27, data28, data29 };
		return data;
	}

	private static int[] newSampleData(int[] depthData, int limit) {
		final int[] sampleData = new int[limit];
		for (int i = 0; i < limit; i++) {
			sampleData[i] = depthData[i];
		}
		return sampleData;
	}

	// |11 1010 1011 |10 1100 1101|00 1110 1111|01 0010 0011|
	// |11 1010 10|11 10 1100|1101 00 11|10 1111 01|0010 0011|

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

	private static byte[] pack10(int[] data) {
		final int BD = 10;
		final int FB = BD >> 3;
		final int zbits = (data.length * BD);
		final int zbc = (zbits / 8) + ((zbits % 8) == 0 ? 0 : 1);
		final byte[] zout = new byte[zbc];
		final int R = 10 - (FB << 3); // 2
		final byte[] bf = new byte[FB + 1];

		int residual = 0;
		int cycle = dout("cycle", 0);
		int s = dout("s", 8 - cycle - R);
		int rs = dout("rs", BD + s);
		int d = 0;
		d = out("d0", data[0]);
		UGrib.intu2(bf, 0, out("r<<rs|d<<s", (residual << rs) | (d << s)));
		out("bf", bf);
		zout[0] = out("z0", bf[0]);

		System.out.println("--1110 1010");
		residual = out("residual", (bf[1] & 0xFF) >> s);
		System.out.println("--11");
		cycle = dout("cycle", (cycle + BD) % 8);
		s = dout("s", 8 - cycle - R);
		rs = dout("rs", BD + s);

		d = out("d1", data[1]);
		UGrib.intu2(bf, 0, out("residual<<rs|d<<s", (residual << rs) | (d << s)));
		out("bf", bf);
		zout[1] = out("z1", bf[0]);
		System.out.println("--1110 1100");
		residual = out("residual", (bf[1] & 0xFF) >> s);
		System.out.println("--1101");
		cycle = dout("cycle", (cycle + BD) % 8);
		s = dout("s", 8 - cycle - R);
		rs = dout("rs", BD + s);

		d = out("d2", data[2]);
		UGrib.intu2(bf, 0, out("residual<<rs|d<<s", (residual << rs) | (d << s)));
		out("bf", bf);
		zout[2] = out("z2", bf[0]);
		System.out.println("--1101 0011");
		residual = out("residual", (bf[1] & 0xFF) >> s);
		System.out.println("--10 1111");
		cycle = dout("cycle", (cycle + BD) % 8);
		s = dout("s", 8 - cycle - R);
		rs = dout("rs", BD + s);

		d = out("d3", data[3]);
		UGrib.intu2(bf, 0, out("residual<<rs|d<<s", (residual << rs) | (d << s)));
		out("bf", bf);
		zout[3] = out("z3", bf[0]);
		System.out.println("--1011 1101");

		zout[4] = out("z4", bf[1]);
		System.out.println("--0010 0011");

		return zout;

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
		int buffer8 = 0;
		final int bufferBitCount = 0;
		final int datumCount = xptData.length;
		for (int datumIndex = 0; datumIndex < datumCount; datumIndex++) {
			final int datum = out("datum@" + datumIndex, xptData[datumIndex]);
			int bufferBitRem = 8 - bufferBitCount;
			if (bufferBitRem < bitDepth) {
				final int shift = dout("shift", bitDepth - bufferBitRem);
				final int mask = out("mask", (1 << bufferBitRem) - 1);
				final int smask = out("smask", mask << shift);
				buffer8 = buffer8 | ((datum & smask) >>> shift);
				dst.octet(buffer8);
				buffer8 = (datum & ~smask) << bufferBitRem;
				bufferBitRem = shift;
			} else {

			}
		}
	}

	private void encodeUnaligned2(int[] xptData, int bitDepth, Section2Buffer dst) {
		final int datumCount = xptData.length;
		final int fullBytesPerSample = bitDepth >> 3;
		final int residualBitsPerSample = bitDepth - (fullBytesPerSample << 3);
		final byte[] packingBuffer = new byte[fullBytesPerSample + 1];

		int residual = dout("residual", 0);
		int carryBits = dout("carry", 0);

		for (int datumIndex = 0; datumIndex < datumCount; datumIndex++) {
			final int datumShift = dout("datumShift", 8 - carryBits - residualBitsPerSample);
			final int residualShift = dout("residualShift", bitDepth + datumShift);
			final int datum = out("datum@" + datumIndex, xptData[datumIndex]);
			UGrib.pack(packingBuffer,
					out("residual<<residualShift|datum<<datumShift", (residual << residualShift) | (datum << datumShift)));
			out("packingBuffer", packingBuffer);
			for (int byteIndex = 0; byteIndex < fullBytesPerSample; byteIndex++) {
				dst.octet(out("add", packingBuffer[byteIndex]));
			}
			if (datumShift == 0) {
				dst.octet(out("add", packingBuffer[fullBytesPerSample]));
				residual = 0;
				carryBits = 0;
			} else {
				residual = out("residual", (packingBuffer[fullBytesPerSample] & 0xFF) >> datumShift);
				carryBits = dout("carryBits", carryBits + residualBitsPerSample);
			}
		}
		if (carryBits > 0) {
			dst.octet(out("add", packingBuffer[fullBytesPerSample]));
		}
	}

	@Test
	public void a00() {
		final int datum = out("datum", 0xABCDB7F);
		final int avail = 10;
		final int reqd = 3;
		final int mask = out("mask", (1 << reqd) - 1);
		final int x = out("x", (datum >> (avail - reqd)) & mask);

	}

	// @Test
	public void a10() {
		final int[][] data = newDataRamp();
		final int bitDepth = 3;
		final int limit = 3;
		final int[] sampleData = newSampleData(data[bitDepth], limit);
		final Section2Buffer dst = new Section2Buffer(7, 1);
		encode(sampleData, bitDepth, dst);
		final Source src = new Source(dst);
		final int[] decode = decode(src, bitDepth, limit);
		Assert.assertArrayEquals(sampleData, decode);
	}

	// @Test
	public void a20() {
		final int[][] data = newDataRamp();
		final int bitDepth = 10;
		final int limit = 4;
		final int[] sampleData = newSampleData(data[bitDepth], limit);
		final Section2Buffer dst = new Section2Buffer(7, 1);
		encode(sampleData, bitDepth, dst);
		final Source src = new Source(dst);
		final int[] decode = decode(src, bitDepth, limit);
		Assert.assertArrayEquals(sampleData, decode);
	}

	// @Test
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
