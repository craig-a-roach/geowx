/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class BitmapSourceBM2Template00 implements IKryptonBitmapSource {

	private static final int[] BITMASK = { 0x80, 0x40, 0x20, 0x10, 0x8, 0x4, 0x2, 0x1 };

	private void unpack(boolean[] values) {
		int octetIndex = m_octetArray.firstDataOctetIndex();
		int buffer = 0;
		for (int i = 0; i < values.length; i++) {
			final int im8 = i % 8;
			if (im8 == 0) {
				buffer = m_octetArray.octetValue(octetIndex);
				octetIndex++;
			}
			final int mask = BITMASK[im8];
			final boolean value = (buffer & mask) != 0;
			values[i] = value;
		}
	}

	@Override
	public boolean[] newBooleanArray(KryptonArrayFactory arrayFactory) {
		if (arrayFactory == null) throw new IllegalArgumentException("object is null");
		final boolean[] values = arrayFactory.newEmptyBooleanArray();
		unpack(values);
		return values;
	}

	public BitmapSourceBM2Template00(IOctetIndexer arrayBM) {
		if (arrayBM == null) throw new IllegalArgumentException("object is null");
		m_octetArray = arrayBM;
	}

	private final IOctetIndexer m_octetArray;
}
