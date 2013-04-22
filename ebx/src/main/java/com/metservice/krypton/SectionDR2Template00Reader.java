/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionDR2Template00Reader extends SectionDR2TemplateReader {

	@Override
	public IKryptonDataSource newDataSource(KryptonDecoder decoder, IKryptonBitmapSource oBitmap, SectionBD2Reader rBD)
			throws KryptonTableException, KryptonCodeException {
		final SimplePackingSpec spec = newSimplePackingSpec();
		return decoder.dataDecoder.newTemplate00(spec, oBitmap, rBD);
	}

	public SectionDR2Template00Reader(SectionDR2Reader base) {
		super(base, 0);
	}
}
