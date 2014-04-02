/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import java.util.Date;

/**
 * @author roach
 */
public class KryptonIdentification2Builder {

	public static final int NoSubCentre = 0;
	public static final short MasterTablesNotUsed = 255;

	public static final Table1_2 SignificanceOfRefTime = new Table1_2();

	public KryptonIdentification2Builder newInstance(int originatingCentre, Date refTime) {
		if (refTime == null) throw new IllegalArgumentException("object is null");
		return new KryptonIdentification2Builder(originatingCentre, refTime.getTime());
	}

	public void originatingSubCentre(int code_C) {
		m_originatingSubCentre = code_C;
	}

	public void significanceOfRefTime(short code_1_2) {
		m_significanceOfRefTime = code_1_2;
	}

	private KryptonIdentification2Builder(int originatingCentre, long refTime) {
		m_originatingCentre = originatingCentre;
		m_refTime = refTime;
	}

	private final int m_originatingCentre;
	private final long m_refTime;
	private int m_originatingSubCentre = NoSubCentre;
	private short m_significanceOfRefTime = Table1_2.Analysis;
	private final short m_masterTablesVersion = MasterTablesNotUsed;

	public static class Table1_2 {

		public static final short Analysis = 0;
		public static final short Start_Of_Forecast = 1;
		public static final short Observation_Time = 3;
	}
}
