/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class SectionPD1Reader extends Section1Reader {

	public short b04_table() {
		return shortu1(4);
	}

	public short b05_centre() {
		return shortu1(5);
	}

	public int b06_process() {
		return intu1(6);
	}

	public short b07_grid() {
		return shortu1(7);
	}

	public int b08_flag() {
		return intu1(8);
	}

	public boolean b08_flagSet(int mask) {
		return (b08_flag() & mask) != 0;
	}

	public short b09_param() {
		return shortu1(9);
	}

	public short b10_leveltype() {
		return shortu1(10);
	}

	public int b11_L1() {
		return intu1(11);
	}

	public int b12_L2() {
		return intu1(12);
	}

	public long b1317_25_ref()
			throws KryptonCodeException {
		final String source = CSection.PD1("13-17,25");
		final int yoc = intu1(13);
		final int moy = intu1(14);
		final int dom = intu1(15);
		final int hod = intu1(16);
		final int moh = intu1(17);
		final int cc = intu1(25);
		if (cc == 20 && yoc == 100) return UGrib.tsG1(source, 20, 0, moy, dom, hod, moh);
		return UGrib.tsG1(source, (cc - 1), yoc, moy, dom, hod, moh);
	}

	public short b18_timeunit() {
		return shortu1(18);
	}

	public int b19_P1() {
		return intu1(19);
	}

	public int b20_P2() {
		return intu1(20);
	}

	public int b21_timerange() {
		return intu1(21);
	}

	public int b2223_averageInclude() {
		return int2(22);
	}

	public int b2425_averageMissing() {
		return int2(24);
	}

	public short b26_subcentre() {
		return shortu1(26);
	}

	public int b2728_decimalScale() {
		return int2(27);
	}

	public int ensembleMember() {
		final int centre = b05_centre();
		if (centre == 98) {
			final int octet = 50;
			if (hasOctets(octet, 1)) return intu1(octet);
		}
		return -1;
	}

	public boolean haveBitmapSection() {
		return b08_flagSet(0x40);
	}

	public boolean haveGridDescriptionSection() {
		return b08_flagSet(0x80);
	}

	public SectionPD1Reader(byte[] section) {
		super(section);
	}
}
