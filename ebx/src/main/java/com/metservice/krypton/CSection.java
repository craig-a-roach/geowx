/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
class CSection {

	public static final String PD1 = "PDS1";
	public static final String GD1 = "GDS1";
	public static final String BM1 = "BMS1";
	public static final String BD1 = "BDS1";

	public static final int PD1_bcLo = 28;
	public static final int GD1_bcLo = 32;
	public static final int BM1_bcLo = 6;
	public static final int BD1_bcLo = 11;

	public static final String ID2 = "IDS2";
	public static final int ID2_No = 1;
	public static final int ID2_bcLo = 21;

	public static final String LU2 = "LUS2";
	public static final int LU2_No = 2;
	public static final int LU2_bcLo = 5;

	public static final String GD2 = "GDS2";
	public static final int GD2_No = 3;
	public static final int GD2_bcLo = 14;

	public static final String PD2 = "PDS2";
	public static final int PD2_No = 4;
	public static final int PD2_bcLo = 9;

	public static final String DR2 = "DRS2";
	public static final int DR2_No = 5;
	public static final int DR2_bcLo = 11;

	public static final String BM2 = "BMS2";
	public static final int BM2_No = 6;
	public static final int BM2_bcLo = 6;

	public static final String BD2 = "BDS2";
	public static final int BD2_No = 7;
	public static final int BD2_bcLo = 0;

	public static final int END_bc = 4;

	public static String BD1(String octetSpec) {
		return BD1 + "[" + octetSpec + "]";
	}

	public static String BM2(String octetSpec) {
		return BM2 + "[" + octetSpec + "]";
	}

	public static String DR2(String octetSpec) {
		return DR2 + "[" + octetSpec + "]";
	}

	public static String GD1(String octetSpec) {
		return GD1 + "[" + octetSpec + "]";
	}

	public static String GD2(String octetSpec) {
		return GD2 + "[" + octetSpec + "]";
	}

	public static String GD2(String templateSpec, String octetSpec) {
		return GD2 + " Template " + templateSpec + "[" + octetSpec + "]";
	}

	public static String ID2(String octetSpec) {
		return ID2 + "[" + octetSpec + "]";
	}

	public static String PD1(String octetSpec) {
		return PD1 + "[" + octetSpec + "]";
	}

	public static String PD2(String octetSpec) {
		return PD2 + "[" + octetSpec + "]";
	}

	public static String PD2(String templateSpec, String octetSpec) {
		return PD2 + " Template " + templateSpec + "[" + octetSpec + "]";
	}

	public static String source(String sectionName, String octetSpec) {
		return sectionName + "[" + octetSpec + "]";
	}

	private CSection() {
	}

}
