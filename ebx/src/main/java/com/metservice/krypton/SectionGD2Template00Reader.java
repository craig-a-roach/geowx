/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionGD2Template00Reader extends SectionGD2Reader {

	public double angleUnit() {
		final int ni = int4(39);
		final int di = int4(43);
		final double n = (ni == 0 || ni == UGrib.INT_UNDEFINED) ? DefaultAngleNumerator : ((double) ni);
		final double d = (di == 0 || di == UGrib.INT_UNDEFINED) ? DefaultAngleDenominator : ((double) di);
		return n / d;
	}

	public String DUnit() {
		return "deg";
	}

	public int DX() {
		return int4(64);
	}

	public int DY() {
		return int4(68);
	}

	public int latitude1() {
		return int4(47);
	}

	public int latitude2() {
		return int4(56);
	}

	public int longitude1() {
		return int4(51);
	}

	public int longitude2() {
		return int4(60);
	}

	public int NX() {
		return int4(31);
	}

	public int NY() {
		return int4(35);
	}

	public int resolutionFlags() {
		return intu1(55);
	}

	public int scanningMode() {
		return intu1(72);
	}

	public short shapeOfEarth() {
		return shortu1(15);
	}

	public SectionGD2Template00Reader(SectionGD2Reader base) {
		super(base);
	}

}
