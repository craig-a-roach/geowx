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
public class TestUnit1PackerG2 {

	private void decode(float[] data, int NX, int NY) {
		final KryptonData2Packer00 p = KryptonData2Packer00.newInstance(data, 1.0f, 1, 24, false);
		final Section2Buffer dst6 = new Section2Buffer(6, 1);
		if (p.requiresBitmap()) {
			p.saveBitmap(dst6);
		}
		final Section2Buffer dst7 = new Section2Buffer(7, 1);
		p.saveData(dst7);

		BitmapSourceBM2Template00 oBM = null;
		if (p.requiresBitmap()) {
			final Source src6 = new Source(dst6);
			oBM = new BitmapSourceBM2Template00(src6);
		}

		final SimplePackingSpec spec = p.packingSpec();
		final Source src7 = new Source(dst7);
		final DataSourceBD2Template00 t = new DataSourceBD2Template00(spec, oBM, src7);
		final KryptonArrayFactory af = new KryptonArrayFactory(NX, NY);
		final KryptonArray array = t.newArray(af);
		final float epsilon = 0.01f;
		for (int i = 0; i < data.length; i++) {
			final float x = data[i];
			final float a = array.value(i);
			if (Float.isNaN(x)) {
				Assert.assertTrue("index " + i + " is NaN", Float.isNaN(a));
			} else {
				Assert.assertEquals("index " + i, x, a, epsilon);
			}
		}
	}

	@Test
	public void t40() {

		final int NX = 2;
		final int NY = 2;
		final float[] data = { 101.4f, 101.4f, 101.4f, 101.4f };
		decode(data, NX, NY);
	}

	@Test
	public void t50() {

		final int NX = 7;
		final int NY = 2;
		final float[] data = { 101.4f, 102.7f, Float.NaN, 101.9f, 101.0f, Float.NaN, 101.6f, 101.2f, Float.NaN, Float.NaN,
				101.1f, 101.8f, 102.9f, Float.NaN };
		decode(data, NX, NY);
	}

	@Test
	public void t60() {

		final int NX = 2;
		final int NY = 2;
		final float[] data = { Float.NaN, 101.4f, 101.4f, Float.NaN };
		decode(data, NX, NY);
	}

	@Test
	public void t70() {

		final int NX = 2;
		final int NY = 2;
		final float[] data = { Float.NaN, Float.NaN, Float.NaN, Float.NaN };
		decode(data, NX, NY);
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
