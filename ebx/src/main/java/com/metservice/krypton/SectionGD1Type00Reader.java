/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionGD1Type00Reader extends SectionGD1Reader implements ISectionGD1ThinGridReader {

	public KryptonThinGrid createThinGrid() {
		if (!hasPL()) return null;
		return KryptonThinGrid.newInstance(this, 33);
	}

	public String DUnit() {
		return "deg";
	}

	public double DX() {
		return double2(24, 1.0e3);
	}

	public double DY() {
		return double2(26, 1.0e3);
	}

	public double latitude2() {
		return int3(18) / 1.0e3;
	}

	public double longitude2() {
		return int3(21) / 1.0e3;
	}

	public int scanningMode() {
		return intu1(28);
	}

	public SectionGD1Type00Reader(SectionGD1Reader base) {
		super(base);
	}

}
