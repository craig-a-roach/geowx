/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class DataDecoder {

	public IKryptonDataSource newTemplate00(SimplePackingSpec spec, IKryptonBitmapSource oBitmap, IOctetIndexer indexerBD) {
		if (spec == null) throw new IllegalArgumentException("object is null");
		if (indexerBD == null) throw new IllegalArgumentException("object is null");
		return new DataSourceBD2Template00(spec, oBitmap, indexerBD);
	}

	public IKryptonDataSource newTemplate03(int numberOfDataPoints, SimplePackingSpec simple, ComplexPackingSpec complex,
			SpatialPackingSpec spatial, IKryptonBitmapSource oBitmap, IOctetIndexer indexerBD) {
		if (simple == null) throw new IllegalArgumentException("object is null");
		if (complex == null) throw new IllegalArgumentException("object is null");
		if (spatial == null) throw new IllegalArgumentException("object is null");
		if (indexerBD == null) throw new IllegalArgumentException("object is null");
		return new DataSourceBD2Template03(numberOfDataPoints, simple, complex, spatial, oBitmap, indexerBD);
	}

	public IKryptonDataSource newTemplate40(int numberDataPoints, SimplePackingSpec simple, Jpeg2000PackingSpec jpeg,
			IKryptonBitmapSource oBitmap, IOctetArray arrayBD) {
		if (simple == null) throw new IllegalArgumentException("object is null");
		if (jpeg == null) throw new IllegalArgumentException("object is null");
		if (arrayBD == null) throw new IllegalArgumentException("object is null");
		return new DataSourceBD2Template40(simple, jpeg, oBitmap, arrayBD);
	}

	public IKryptonDataSource newType00(String source, SectionPD1Reader rPD, SectionBD1Reader rBD)
			throws KryptonCodeException {
		if (rPD == null) throw new IllegalArgumentException("object is null");
		if (rBD == null) throw new IllegalArgumentException("object is null");
		final int decimalScale = rPD.b2728_decimalScale();
		final int scaleFactor = rBD.b0506_scaleFactor();
		final float referenceValueUnscaled = rBD.b07_10_referenceValue();
		final short bitDepth = rBD.b11_bitsPerValue();
		final SimplePackingSpec spec = new SimplePackingSpec(referenceValueUnscaled, scaleFactor, decimalScale, bitDepth);
		return new DataSourceBD1Type00(spec, rBD);
	}

	public DataDecoder() {
	}
}
