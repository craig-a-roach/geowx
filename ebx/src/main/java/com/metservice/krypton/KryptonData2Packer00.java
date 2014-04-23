/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonData2Packer00 implements IBitmap2Emitter, IData2Emitter {

	private static final double ToLog2 = Math.log(2.0);

	private static int alignBitDepth(int value) {
		final int f = value >> 3;
		final int r = value - (f << 3);
		final int fa = (r == 0) ? f : (f + 1);
		return fa << 3;
	}

	private static int bitDepth(int calculated, int maxBitDepth, boolean aligned) {
		final int climit = Math.max(1, Math.min(calculated, maxBitDepth));
		return aligned ? alignBitDepth(climit) : climit;
	}

	private static int bitsToBytes(int bitCount) {
		final int f = bitCount >> 3;
		final int r = bitCount - (f << 3);
		return r == 0 ? f : (f + 1);
	}

	private static int maxBitDepth(int bitDepthLimit, boolean aligned) {
		final int climit = Math.max(1, Math.min(bitDepthLimit, 24));
		return aligned ? alignBitDepth(climit) : climit;
	}

	public static KryptonData2Packer00 newInstance(float[] xptSparseData, float unitConverter, int decimalScale,
			int bitDepthLimit, boolean aligned) {
		if (xptSparseData == null) throw new IllegalArgumentException("object is null");
		final int len = xptSparseData.length;
		if (len == 0) throw new IllegalArgumentException("empty array");
		final int dscale = Math.max(-10, Math.min(10, decimalScale));
		final int maxBitDepth = maxBitDepth(bitDepthLimit, aligned);
		final double dscale10 = Math.pow(10, dscale);
		final float v0 = xptSparseData[0];
		float vmin = v0;
		float vmax = v0;
		int validCount = Float.isNaN(v0) ? 0 : 1;
		for (int i = 1; i < len; i++) {
			final float v = xptSparseData[i];
			if (Float.isNaN(v)) {
				continue;
			}
			if (v < vmin) {
				vmin = v;
			}
			if (v > vmax) {
				vmax = v;
			}
			validCount++;
		}
		final float nmin = unitConverter * vmin;
		final float nmax = unitConverter * vmax;
		final float referenceValue = (float) (nmin * dscale10);
		final float delta = nmax - nmin;
		final double delta10 = Math.ceil(delta * dscale10);
		final int deltaBits = (int) Math.ceil((Math.log(delta10) / ToLog2));
		final int binaryScale = Math.max(0, deltaBits - maxBitDepth);
		final int bitDepth = bitDepth(deltaBits, maxBitDepth, aligned);
		final SimplePackingSpec spec = new SimplePackingSpec(referenceValue, binaryScale, decimalScale, bitDepth);
		return new KryptonData2Packer00(xptSparseData, unitConverter, spec, validCount);
	}

	SimplePackingSpec packingSpec() {
		return m_spec;
	}

	@Override
	public int bitmapByteCount() {
		return m_bitmapByteCount;
	}

	@Override
	public int dataByteCount() {
		return m_dataByteCount;
	}

	public int gridPointCount() {
		return m_gridCount;
	}

	@Override
	public boolean requiresBitmap() {
		return m_validCount < m_gridCount;
	}

	@Override
	public void saveBitmap(Section2Buffer dst) {
		dst.increaseCapacityBy(m_bitmapByteCount);
		int buffer = 0;
		int bufferBitCount = 0;
		for (int i = 0; i < m_gridCount; i++) {
			final boolean isMissing = Float.isNaN(m_xptSparseData[i]);
			buffer <<= 1;
			if (!isMissing) {
				buffer |= 1;
			}
			bufferBitCount++;
			if (bufferBitCount == 8) {
				dst.octet(buffer);
				bufferBitCount = 0;
				buffer = 0;
			}
		}
		if (bufferBitCount > 0) {
			final int bufferL = buffer << (8 - bufferBitCount);
			dst.octet(bufferL);
		}
	}

	@Override
	public void saveData(Section2Buffer dst) {
		final double DD = Math.pow(10.0, m_spec.decimalScale);
		final double EE = Math.pow(2.0, m_spec.binaryScale);
		final float R = m_spec.referenceValue;
		final int bitDepth = m_spec.bitDepth;
		final Encoder encoder = Encoder.newEncoder(dst, bitDepth);
		dst.increaseCapacityBy(m_dataByteCount);
		for (int i = 0; i < m_gridCount; i++) {
			final float v = m_xptSparseData[i];
			if (Float.isNaN(v)) {
				continue;
			}
			final double dev = ((v * m_unitConverter * DD) - R) / EE;
			final int ev = (int) Math.round(dev);
			encoder.encode(ev);
		}
		encoder.flush();
	}

	public int validPointCount() {
		return m_validCount;
	}

	private KryptonData2Packer00(float[] xptSparseData, float unitConverter, SimplePackingSpec spec, int validCount) {
		assert xptSparseData != null;
		assert spec != null;
		m_xptSparseData = xptSparseData;
		m_unitConverter = unitConverter;
		m_spec = spec;
		m_gridCount = xptSparseData.length;
		m_validCount = validCount;
		m_bitmapByteCount = bitsToBytes(xptSparseData.length);
		m_dataByteCount = bitsToBytes(validCount * spec.bitDepth);
	}

	private final float[] m_xptSparseData;
	private final float m_unitConverter;
	private final SimplePackingSpec m_spec;
	private final int m_gridCount;
	private final int m_validCount;
	private final int m_bitmapByteCount;
	private final int m_dataByteCount;

	private static abstract class Encoder {

		public static Encoder newEncoder(Section2Buffer dst, int bitDepth) {
			if (bitDepth % 8 == 0) return new EncoderAligned(dst, bitDepth);
			return new EncoderUnaligned(dst, bitDepth);
		}

		public abstract void encode(int datum);

		public abstract void flush();

		protected Encoder(Section2Buffer dst, int bitDepth) {
			this.dst = dst;
			this.bitDepth = bitDepth;
		}
		protected final Section2Buffer dst;
		protected final int bitDepth;
	}

	private static class EncoderAligned extends Encoder {

		@Override
		public void encode(int datum) {
			UGrib.pack(m_packBuffer, datum);
			dst.octets(m_packBuffer);
		}

		@Override
		public void flush() {
		}

		protected EncoderAligned(Section2Buffer dst, int bitDepth) {
			super(dst, bitDepth);
			final int octetDepth = bitDepth >> 3;
			m_packBuffer = new byte[octetDepth];
		}
		private final byte[] m_packBuffer;
	}

	private static class EncoderUnaligned extends Encoder {

		@Override
		public void encode(int datum) {
			m_bufferR = (m_bufferR << bitDepth) | datum;
			m_bufferBitCount += bitDepth;
			while (m_bufferBitCount >= 8) {
				final int shift = m_bufferBitCount - 8;
				final int mask = 0xFF << shift;
				final int dm = (m_bufferR & mask) >>> shift;
				dst.octet(dm);
				m_bufferR &= ~mask;
				m_bufferBitCount -= 8;
			}
		}

		@Override
		public void flush() {
			if (m_bufferBitCount > 0) {
				final int bufferL = m_bufferR << (8 - m_bufferBitCount);
				dst.octet(bufferL);
			}
		}

		protected EncoderUnaligned(Section2Buffer dst, int bitDepth) {
			super(dst, bitDepth);
		}
		private int m_bufferR;
		private int m_bufferBitCount;
	}
}
