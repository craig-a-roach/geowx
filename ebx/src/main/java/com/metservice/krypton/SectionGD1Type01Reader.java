/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionGD1Type01Reader extends SectionGD1Reader {

	public String DUnit() {
		return "m";
	}

	public double DX() {
		return int3(29);
	}

	public double DY() {
		return int3(32);
	}

	public double latitude2() {
		return int3(18) / 1.0e3;
	}

	public double latitudeCylinderIntersection() {
		return int3(24) / 1.0e3;
	}

	public double longitude2() {
		return int3(21) / 1.0e3;
	}

	public int scanningMode() {
		return intu1(28);
	}

	public SectionGD1Type01Reader(SectionGD1Reader base) {
		super(base);
	}
}
