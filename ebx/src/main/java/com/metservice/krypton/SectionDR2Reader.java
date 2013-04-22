/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionDR2Reader extends Section2Reader {

	public int b0609_numberOfDataPoints() {
		return int4(6);
	}

	public int b1011_templateNo() {
		return int2(10);
	}

	protected SectionDR2Reader(SectionDR2Reader base) {
		super(base);
	}

	public SectionDR2Reader(byte[] section) {
		super(section);
	}
}
