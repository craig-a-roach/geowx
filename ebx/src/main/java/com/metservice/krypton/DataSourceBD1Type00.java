/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class DataSourceBD1Type00 implements IKryptonDataSource {

	private void fillConstant(float[] dst, float ref) {
		assert dst != null;
		final int len = dst.length;
		for (int i = 0; i < len; i++) {
			dst[i] = ref;
		}
	}

	private void fillVariable(float[] dst, float ref, float scale) {
		assert dst != null;
		final int bitDepth = m_spec.bitDepth;
		final int len = dst.length;
		for (int i = 0; i < len; i++) {
			final int sample = m_factory.nextInteger(bitDepth);
			final float value = ref + (scale * sample);
			dst[i] = value;
		}
	}

	@Override
	public KryptonArray newArray(KryptonArrayFactory arrayFactory) {
		if (arrayFactory == null) throw new IllegalArgumentException("object is null");
		final float[] values = arrayFactory.newEmptyFloatArray();
		final float ref = (float) (Math.pow(10.0, -m_spec.decimalScale) * m_spec.referenceValue);
		final float scale = (float) (Math.pow(10.0, -m_spec.decimalScale) * Math.pow(2.0, m_spec.binaryScale));

		if (m_spec.bitDepth == 0) {
			fillConstant(values, ref);
		} else {
			fillVariable(values, ref, scale);
		}
		return new KryptonArray(values);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("spec", m_spec);
		return ds.s();
	}

	public DataSourceBD1Type00(SimplePackingSpec spec, IOctetIndexer indexerBD) {
		if (spec == null) throw new IllegalArgumentException("object is null");
		if (indexerBD == null) throw new IllegalArgumentException("object is null");
		m_spec = spec;
		m_factory = new IntegerFactory(indexerBD);
	}
	private final SimplePackingSpec m_spec;
	private final IntegerFactory m_factory;
}
