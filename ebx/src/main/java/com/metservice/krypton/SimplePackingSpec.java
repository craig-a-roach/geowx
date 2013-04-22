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
class SimplePackingSpec {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("referenceValue", referenceValue);
		ds.a("binaryScale", binaryScale);
		ds.a("decimalScale", decimalScale);
		ds.a("bitDepth", bitDepth);
		return ds.s();
	}

	public SimplePackingSpec(float referenceValue, int binaryScale, int decimalScale, int bitDepth) {
		this.referenceValue = referenceValue;
		this.binaryScale = binaryScale;
		this.decimalScale = decimalScale;
		this.bitDepth = bitDepth;
	}
	public final float referenceValue;
	public final int binaryScale;
	public final int decimalScale;
	public final int bitDepth;
}
