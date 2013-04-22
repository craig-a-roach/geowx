/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionGD2Reader extends Section2Reader {

	public static final double DefaultAngleNumerator = 1.0;
	public static final double DefaultAngleDenominator = 1.0e6;

	public short b06_sourceOfDefinition() {
		return shortu1(6);
	}

	public int b0710_numberOfDataPoints() {
		return int4(7);
	}

	public int b11() {
		return shortu1(11);
	}

	public int b12() {
		return shortu1(12);
	}

	public int b1314_template() {
		return int2(13);
	}

	protected SectionGD2Reader(SectionGD2Reader base) {
		super(base);
	}

	public SectionGD2Reader(byte[] section) {
		super(section);
	}

}
