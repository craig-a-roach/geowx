/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonData2Packer00 {

	private static final double ToLog2 = Math.log(2.0);

	private static int bitDepth(int deltaBits, int maxBits) {
		if (deltaBits >= maxBits) return maxBits;
		if (deltaBits <= 8) return 8;
		if (deltaBits <= 16) return 16;
		return 24;
	}

	public static KryptonData2Packer00 newInstance(float[] xptSparseData, float unitConverter, int decimalScale,
			int maxDepthOctets) {
		if (xptSparseData == null) throw new IllegalArgumentException("object is null");
		final int len = xptSparseData.length;
		if (len == 0) throw new IllegalArgumentException("empty array");
		final int dscale = Math.max(-10, Math.min(10, decimalScale));
		final int maxOctets = Math.max(1, Math.min(3, maxDepthOctets));
		final double dscale10 = Math.pow(10, dscale);
		final int maxBits = maxOctets << 3;
		float vmin = xptSparseData[0];
		float vmax = vmin;
		int validCount = 0;
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
		final int binaryScale = Math.max(0, deltaBits - maxBits);
		final int bitDepth = bitDepth(maxBits, deltaBits);
		final SimplePackingSpec spec = new SimplePackingSpec(referenceValue, binaryScale, decimalScale, bitDepth);
		return new KryptonData2Packer00(xptSparseData, unitConverter, spec, validCount);
	}

	public void saveBitmap(Section2Buffer dst) {

	}

	public void saveData(Section2Buffer dst) {
		final int gridLen = m_xptSparseData.length;
		final double DD = Math.pow(10.0, m_spec.decimalScale);
		final double EE = Math.pow(2.0, m_spec.binaryScale);
		final float R = m_spec.referenceValue;
		for (int i = 0; i < gridLen; i++) {
			final float v = m_xptSparseData[i];
			if (Float.isNaN(v)) {
				continue;
			}
			// in = ((out * 10^D) - R) / 2^E
			final double dev = ((v * m_unitConverter * DD) - R) / EE;
			final int ev = (int) Math.round(dev);

		}

	}

	private KryptonData2Packer00(float[] xptSparseData, float unitConverter, SimplePackingSpec spec, int validCount) {
		assert xptSparseData != null;
		assert spec != null;
		m_xptSparseData = xptSparseData;
		m_unitConverter = unitConverter;
		m_spec = spec;
		m_validCount = validCount;
	}
	private final float[] m_xptSparseData;
	private final float m_unitConverter;
	private final SimplePackingSpec m_spec;
	private final int m_validCount;
}
