/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionBM2Template00Reader extends SectionBM2TemplateReader {

	@Override
	public IKryptonBitmapSource newBitmapSource() {
		return new BitmapSourceBM2Template00(this);
	}

	public SectionBM2Template00Reader(SectionBM2Reader rBM) {
		super(rBM, 0);
	}
}
