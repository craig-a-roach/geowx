/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class DataSourceBD2Template00 implements IKryptonDataSource {

	@Override
	public KryptonArray newArray(KryptonArrayFactory arrayFactory) {
		final float[] values = arrayFactory.newEmptyFloatArray();
		final float mv = Float.NaN;
		boolean[] oMask = null;
		if (m_oBitmap != null) {
			oMask = m_oBitmap.newBooleanArray(arrayFactory);
		}
		final int D = m_spec.decimalScale;
		final float R = m_spec.referenceValue;
		final int E = m_spec.binaryScale;
		final int nb = m_spec.bitDepth;
		final float DD = (float) java.lang.Math.pow(10.0, D);
		final float EE = (float) java.lang.Math.pow(2.0, E);
		final int numberPoints = values.length;
		for (int i = 0; i < numberPoints; i++) {
			final float value;
			if (oMask == null || oMask[i]) {
				final int sample = m_factory.nextInteger(nb);
				value = (R + (sample * EE)) / DD;
			} else {
				value = mv;
			}
			values[i] = value;
		}
		return new KryptonArray(values);
	}

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("spec", m_spec);
		return ds.s();
	}

	public DataSourceBD2Template00(SimplePackingSpec spec, IKryptonBitmapSource oBitmap, IOctetIndexer indexer) {
		if (spec == null) throw new IllegalArgumentException("object is null");
		if (indexer == null) throw new IllegalArgumentException("object is null");
		m_spec = spec;
		m_oBitmap = oBitmap;
		m_factory = new IntegerFactory(indexer);
	}
	private final SimplePackingSpec m_spec;
	private final IKryptonBitmapSource m_oBitmap;
	private final IntegerFactory m_factory;
}
