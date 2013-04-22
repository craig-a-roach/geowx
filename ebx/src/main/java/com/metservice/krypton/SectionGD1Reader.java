/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionGD1Reader extends Section1Reader implements ISectionGD1Reader {

	public short b04_NV() {
		return shortu1(4);
	}

	public short b05_PV_or_PL() {
		return shortu1(5);
	}

	public short b06_type() {
		return shortu1(6);
	}

	public short b0708_nx() {
		return shortu2(7);
	}

	public short b0910_ny() {
		return shortu2(9);
	}

	public boolean hasPL() {
		final short NV = b04_NV();
		final short PV_or_PL = b05_PV_or_PL();
		return (NV == 0 || NV == 255) && (PV_or_PL != 255);
	}

	public boolean hasPV() {
		final short NV = b04_NV();
		final short PV_or_PL = b05_PV_or_PL();
		return (NV != 0 && NV != 255 && PV_or_PL != 255);
	}

	public double latitude1() {
		return int3(11) / 1.0e3;
	}

	public double longitude1() {
		return int3(14) / 1.0e3;
	}

	public int NX() {
		final short nx = b0708_nx();
		if (nx == -1) return 1;
		return nx;
	}

	public int NY() {
		final short ny = b0910_ny();
		if (ny == -1) return 1;
		return ny;
	}

	protected SectionGD1Reader(SectionGD1Reader base) {
		super(base);
	}

	public SectionGD1Reader(byte[] section) {
		super(section);
	}

}
