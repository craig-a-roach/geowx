/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionID2Reader extends Section2Reader {

	public int b0607_originatingCentre() {
		return int2(6);
	}

	public int b0809_originatingSubCentre() {
		return int2(8);
	}

	public short b10_masterTablesVersion() {
		return shortu1(10);
	}

	public short b11_localTablesVersion() {
		return shortu1(11);
	}

	public short b12_significanceRef() {
		return shortu1(12);
	}

	public long b1319_ref()
			throws KryptonCodeException {
		final String source = CSection.ID2("13-19");
		final int year = int2(13);
		final int moy = intu1(15);
		final int dom = intu1(16);
		final int hod = intu1(17);
		final int moh = intu1(18);
		final int sec = intu1(19);
		return UGrib.tsG2(source, year, moy, dom, hod, moh, sec);
	}

	public short b20_productionStatus() {
		return shortu1(20);
	}

	public short b21_typeOfProcessedData() {
		return shortu1(21);
	}

	public SectionID2Reader(byte[] section) {
		super(section);
	}

}
