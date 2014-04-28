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
public class KryptonIdentification2Builder extends KryptonSection2Builder {

	public static final int NoSubCentre = 0;
	public static final int DefaultMasterTablesVersion = 11;
	public static final int NoLocalTables = 0;

	@Override
	void save(Section2Buffer dst) {
		dst.u2(m_originatingCentre); // 6-7
		dst.u2(m_originatingSubCentre); // 8-9
		dst.u1(m_masterTablesVersion); // 10
		dst.u1(m_localTablesVersion); // 11
		dst.u1(m_significanceOfRefTime); // 12
		dst.ts(m_refTime); // 13-19
		dst.u1(m_productionStatus); // 20
		dst.u1(m_typeOfData); // 21
	}

	public void localTablesVersion(int valueTable1_1) {
		m_localTablesVersion = valueTable1_1;
	}

	public void masterTablesVersion(int valueTable1_0) {
		m_masterTablesVersion = valueTable1_0;
	}

	public void originatingSubCentre(int valueTableC) {
		m_originatingSubCentre = valueTableC;
	}

	public void productionStatus(int valueTable1_3) {
		m_productionStatus = valueTable1_3;
	}

	@Override
	public int sectionNo() {
		return 1;
	}

	public void significanceOfRefTime(int valueTable1_2) {
		m_significanceOfRefTime = valueTable1_2;
	}

	public KryptonIdentification2Builder(int originatingCentre_Table0, int typeOfData_Table1_4, Date refTime) {
		if (refTime == null) throw new IllegalArgumentException("object is null");
		m_originatingCentre = originatingCentre_Table0;
		m_typeOfData = typeOfData_Table1_4;
		m_refTime = refTime.getTime();
	}
	private final int m_originatingCentre;
	private final int m_typeOfData;
	private final long m_refTime;
	private int m_originatingSubCentre = NoSubCentre;
	private int m_significanceOfRefTime = Table1_2.Analysis;
	private int m_masterTablesVersion = DefaultMasterTablesVersion;
	private int m_localTablesVersion = NoLocalTables;
	private int m_productionStatus = Table1_3.Operational;

	public static class Table1_2 {

		public static final int Analysis = 0;
		public static final int Start_Of_Forecast = 1;
		public static final int Observation_Time = 3;
		public static final int Missing = 255;
	}

	public static class Table1_3 {

		public static final int Operational = 0;
		public static final int Operational_Test = 1;
		public static final int Research = 2;
		public static final int Reanalysis = 3;
		public static final int Missing = 255;
	}

	public static class Table1_4 {

		public static final int Analysis_Products = 0;
		public static final int Forecast_Products = 1;
		public static final int Analysis_And_Forecast_Products = 2;
		public static final int Control_Forecast_Products = 3;
		public static final int Perturbed_Forecast_Products = 4;
		public static final int Control_And_Perturbed_Forecast_Products = 5;
		public static final int Processed_Satellite_Observations = 6;
		public static final int Processed_Radar_Observations = 7;
		public static final int Missing = 255;
	}
}
