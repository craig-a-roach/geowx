/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionBM2Reader extends Section2Reader implements IOctetIndexer {

	public int b06_bitmapIndicator() {
		return shortu1(6);
	}

	@Override
	public int firstDataOctetIndex() {
		return 7;
	}

	@Override
	public int octetValue(int octetPos) {
		return intu1(octetPos);
	}

	public SectionBM2Reader(byte[] section) {
		super(section);
	}

	public SectionBM2Reader(SectionBM2Reader base) {
		super(base);
	}
}
