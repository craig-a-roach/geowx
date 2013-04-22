/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class DataSourceBD2Template03 implements IKryptonDataSource {

	private static final int BITSMV1[] = bitsmv1();

	private static int[] bitsmv1() {
		final int bits[] = new int[31];
		for (int i = 0; i < 31; i++) {
			bits[i] = (int) Math.pow(2, i) - 1;
		}
		return bits;
	}

	private void fillMissing(float[] data) {
		final int numberPoints = data.length;
		for (int i = 0; i < numberPoints; i++) {
			data[i] = Float.NaN;
		}
	}

	private boolean unpack(float[] data, boolean[] oMask) {
		final int numberPoints = data.length;
		int ival1 = 0, ival2 = 0, minsd = 0;

		final int os = m_spatial.orderSpatial;
		final int nbitsd = m_spatial.descriptorSpatial * 8;
		final int NG = m_complex.numberOfGroups;
		final int mvm = m_complex.missingValueManagement;

		m_factory.clearBuffer();

		int sign;
		if (nbitsd == 0 || NG == 0) return false;
		sign = m_factory.nextInteger(1);
		ival1 = m_factory.nextInteger(nbitsd - 1);
		if (sign == 1) {
			ival1 = -ival1;
		}
		if (os == 2) {
			sign = m_factory.nextInteger(1);
			ival2 = m_factory.nextInteger(nbitsd - 1);
			if (sign == 1) {
				ival2 = -ival2;
			}
		}
		sign = m_factory.nextInteger(1);
		minsd = m_factory.nextInteger(nbitsd - 1);
		if (sign == 1) {
			minsd = -minsd;
		}

		final int[] X1 = new int[NG];
		if (m_simple.bitDepth != 0) {
			m_factory.clearBuffer();
			for (int i = 0; i < NG; i++) {
				X1[i] = m_factory.nextInteger(m_simple.bitDepth);
			}
		} else {
			for (int i = 0; i < NG; i++) {
				X1[i] = 0;
			}
		}

		final int[] NB = new int[NG];
		if (m_complex.bitsGroupWidths != 0) {
			m_factory.clearBuffer();
			for (int i = 0; i < NG; i++) {
				NB[i] = m_factory.nextInteger(m_complex.bitsGroupWidths);
			}
		} else {
			for (int i = 0; i < NG; i++) {
				NB[i] = 0;
			}
		}

		final int referenceGroupWidths = m_complex.referenceGroupWidths;
		for (int i = 0; i < NG; i++) {
			NB[i] += referenceGroupWidths;
		}

		final int[] L = new int[NG];
		m_factory.clearBuffer();
		if (m_complex.bitsScaledGroupLength != 0) {
			for (int i = 0; i < NG; i++) {
				L[i] = m_factory.nextInteger(m_complex.bitsScaledGroupLength);
			}
		} else {
			for (int i = 0; i < NG; i++) {
				L[i] = 0;
			}
		}

		final int referenceGroupLength = m_complex.referenceGroupLengths;
		final int len_inc = m_complex.lengthIncrement;

		int totalL = 0;
		for (int i = 0; i < NG; i++) {
			L[i] = L[i] * len_inc + referenceGroupLength;
			totalL += L[i];
		}
		totalL -= L[NG - 1];
		totalL += m_complex.lengthLastGroup;
		L[NG - 1] = m_complex.lengthLastGroup;

		if (mvm == 0 && totalL != m_numberOfDataPoints) return false;
		if (mvm != 0 && totalL != numberPoints) return false;

		final int D = m_simple.decimalScale;
		final float DD = (float) java.lang.Math.pow(10, D);
		final float R = m_simple.referenceValue;

		final int E = m_simple.binaryScale;
		final float EE = (float) java.lang.Math.pow(2.0, E);

		m_factory.clearBuffer();
		int count = 0;
		int dataSize = 0;
		boolean[] oDataBitMap = null;
		if (mvm == 0) {
			for (int i = 0; i < NG; i++) {
				if (NB[i] != 0) {
					for (int j = 0; j < L[i]; j++) {
						data[count++] = m_factory.nextInteger(NB[i]) + X1[i];
					}
				} else {
					for (int j = 0; j < L[i]; j++) {
						data[count++] = X1[i];
					}
				}
			}
		} else if (mvm == 1 || mvm == 2) {
			oDataBitMap = new boolean[numberPoints];
			dataSize = 0;
			for (int i = 0; i < NG; i++) {
				if (NB[i] != 0) {
					final int msng1 = BITSMV1[NB[i]];
					final int msng2 = msng1 - 1;
					for (int j = 0; j < L[i]; j++) {
						data[count] = m_factory.nextInteger(NB[i]);
						if (data[count] == msng1) {
							oDataBitMap[count] = false;
						} else if (mvm == 2 && data[count] == msng2) {
							oDataBitMap[count] = false;
						} else {
							oDataBitMap[count] = true;
							data[dataSize++] = data[count] + X1[i];
						}
						count++;
					}
				} else {
					final int msng1 = BITSMV1[m_simple.bitDepth];
					final int msng2 = msng1 - 1;
					if (X1[i] == msng1) {
						for (int j = 0; j < L[i]; j++) {
							oDataBitMap[count++] = false;
						}
					} else if (mvm == 2 && X1[i] == msng2) {
						for (int j = 0; j < L[i]; j++) {
							oDataBitMap[count++] = false;
						}
					} else {
						for (int j = 0; j < L[i]; j++) {
							oDataBitMap[count] = true;
							data[dataSize++] = X1[i];
							count++;
						}
					}
				}
			}
		}

		if (os == 1) {
			data[0] = ival1;
			int itemp;
			if (mvm == 0) {
				itemp = numberPoints;
			} else {
				itemp = dataSize;
			}
			for (int i = 1; i < itemp; i++) {
				data[i] += minsd;
				data[i] = data[i] + data[i - 1];
			}
		} else if (os == 2) {
			data[0] = ival1;
			data[1] = ival2;
			int itemp;
			if (mvm == 0) {
				itemp = numberPoints;
			} else {
				itemp = dataSize;
			}
			for (int i = 2; i < itemp; i++) {
				data[i] += minsd;
				data[i] = data[i] + (2 * data[i - 1]) - data[i - 2];
			}
		}

		if (mvm == 0) {
			for (int i = 0; i < data.length; i++) {
				data[i] = (R + (data[i] * EE)) / DD;
			}
		} else {
			dataSize = 0;
			final float[] tmp = new float[numberPoints];
			for (int i = 0; i < data.length; i++) {
				if (oDataBitMap != null && oDataBitMap[i]) {
					tmp[i] = (R + (data[dataSize++] * EE)) / DD;
				} else {
					tmp[i] = Float.NaN;
				}
			}
			data = tmp;
		}

		if (oMask != null) {
			int idx = 0;
			final float[] tmp = new float[numberPoints];
			for (int i = 0; i < numberPoints; i++) {
				if (oMask[i]) {
					tmp[i] = data[idx++];
				} else {
					tmp[i] = Float.NaN;
				}
			}
			data = tmp;
		}
		return true;
	}

	@Override
	public KryptonArray newArray(KryptonArrayFactory arrayFactory) {
		if (arrayFactory == null) throw new IllegalArgumentException("object is null");
		final float[] data = arrayFactory.newEmptyFloatArray();
		final boolean[] oMask = m_oBitmap == null ? null : m_oBitmap.newBooleanArray(arrayFactory);
		if (!unpack(data, oMask)) {
			fillMissing(data);
		}
		return new KryptonArray(data);
	}

	public DataSourceBD2Template03(int numberOfDataPoints, SimplePackingSpec simple, ComplexPackingSpec complex,
			SpatialPackingSpec spatial, IKryptonBitmapSource oBitmap, IOctetIndexer indexerBD) {
		if (simple == null) throw new IllegalArgumentException("object is null");
		if (complex == null) throw new IllegalArgumentException("object is null");
		if (spatial == null) throw new IllegalArgumentException("object is null");
		m_numberOfDataPoints = numberOfDataPoints;
		m_simple = simple;
		m_complex = complex;
		m_spatial = spatial;
		m_oBitmap = oBitmap;
		m_factory = new IntegerFactory(indexerBD);
	}
	private final int m_numberOfDataPoints;
	private final SimplePackingSpec m_simple;
	private final ComplexPackingSpec m_complex;
	private final SpatialPackingSpec m_spatial;
	private final IKryptonBitmapSource m_oBitmap;
	private final IntegerFactory m_factory;
}
