/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionBD1Reader extends Section1Reader implements IOctetIndexer {

	public short b04hi_flag() {
		return shortu1hi(4);
	}

	public short b04lo_unusedTrailingBits() {
		return shortu1lo(4);
	}

	public int b0506_scaleFactor() {
		return int2(5);
	}

	public float b07_10_referenceValue() {
		return float4(7);
	}

	public short b11_bitsPerValue() {
		return shortu1(11);
	}

	@Override
	public int firstDataOctetIndex() {
		return 12;
	}

	@Override
	public int octetValue(int octetPos) {
		return intu1(octetPos);
	}

	public SectionBD1Reader(byte[] section) {
		super(section);
	}
}
