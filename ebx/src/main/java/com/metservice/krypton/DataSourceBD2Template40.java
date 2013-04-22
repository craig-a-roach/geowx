/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import ucar.jpeg.jj2000.j2k.decoder.Grib2JpegDecoder;

/**
 * @author roach
 */
class DataSourceBD2Template40 implements IKryptonDataSource {

	private void fillMissing(float[] data) {
		final int numberPoints = data.length;
		for (int i = 0; i < numberPoints; i++) {
			data[i] = Float.NaN;
		}
	}

	private int[] j2kDecode(int nb)
			throws KryptonUnpackException {
		try {
			Grib2JpegDecoder g2j = null;
			final String[] argv = new String[4];
			argv[0] = "-rate";
			argv[1] = Integer.toString(nb);
			argv[2] = "-verbose";
			argv[3] = "off";
			g2j = new Grib2JpegDecoder(argv);
			g2j.decode(m_array.payloadOctets());
			return g2j.data;
		} catch (final RuntimeException ex) {
			throw new KryptonUnpackException("JPEG2000 bit rate too small (" + nb + ")");
		}
	}

	private boolean unpack(float[] data, boolean[] oMask)
			throws KryptonUnpackException {
		final int nb = m_simple.bitDepth;
		if (nb == 0) return false;

		final int numberPoints = data.length;
		final int[] decode = j2kDecode(nb);
		if (decode.length != numberPoints) return false;

		final float mv = Float.NaN;
		final int D = m_simple.decimalScale;
		final float R = m_simple.referenceValue;
		final int E = m_simple.binaryScale;
		final float DD = (float) java.lang.Math.pow(10.0, D);
		final float EE = (float) java.lang.Math.pow(2.0, E);
		if (oMask == null) {
			for (int i = 0; i < numberPoints; i++) {
				final int sample = decode[i];
				final float value = (R + (sample * EE)) / DD;
				data[i] = value;
			}
		} else {
			int ir = 0;
			for (int i = 0; i < oMask.length; i++) {
				if (oMask[i]) {
					final int sample = decode[ir];
					ir++;
					data[i] = (R + sample * EE) / DD;
				} else {
					data[i] = mv;
				}
			}
		}
		return true;
	}

	@Override
	public KryptonArray newArray(KryptonArrayFactory arrayFactory)
			throws KryptonUnpackException {
		if (arrayFactory == null) throw new IllegalArgumentException("object is null");
		final float[] data = arrayFactory.newEmptyFloatArray();
		final boolean[] oMask = m_oBitmap == null ? null : m_oBitmap.newBooleanArray(arrayFactory);
		if (!unpack(data, oMask)) {
			fillMissing(data);
		}
		return new KryptonArray(data);
	}

	public DataSourceBD2Template40(SimplePackingSpec simple, Jpeg2000PackingSpec jpeg, IKryptonBitmapSource oBitmap,
			IOctetArray array) {
		if (simple == null) throw new IllegalArgumentException("object is null");
		if (jpeg == null) throw new IllegalArgumentException("object is null");
		if (array == null) throw new IllegalArgumentException("object is null");
		m_simple = simple;
		m_oBitmap = oBitmap;
		m_array = array;
	}
	private final SimplePackingSpec m_simple;
	private final IKryptonBitmapSource m_oBitmap;
	private final IOctetArray m_array;
}
