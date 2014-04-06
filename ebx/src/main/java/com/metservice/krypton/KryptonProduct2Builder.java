/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonProduct2Builder extends Section2Builder {

	public static final int CoordinateCount = 0;

	public Template4_0 newTemplate4_0(int parameterCategory_Table4_1, int parameterNo_Table4_2) {
		final Template4_0 neo = new Template4_0(parameterCategory_Table4_1, parameterNo_Table4_2);
		m_oDefinition = neo;
		return neo;
	}

	@Override
	public void save(Section2Buffer dst) {
		if (m_oDefinition == null) throw new IllegalStateException("missing Template 4");
		dst.u2(CoordinateCount); // 6-7
		m_oDefinition.save(dst); // 8+
	}

	@Override
	public int sectionNo() {
		return 4;
	}

	public KryptonProduct2Builder() {
	}

	private DefinitionTemplate m_oDefinition;

	abstract static class DefinitionTemplate {

		void save(Section2Buffer dst) {
			dst.int2(m_templateNo); // 8-9
			dst.u1(m_parameterCategory); // 10
			dst.u1(m_parameterNo); // 11
			dst.u1(m_typeOfGeneratingProcess); // 12
		}

		public void typeOfGeneratingProcess(int valueTable4_3) {
			m_typeOfGeneratingProcess = valueTable4_3;
		}

		protected DefinitionTemplate(int templateNo_Table4_0, int parameterCategory_Table4_1, int parameterNo_Table4_2) {
			m_templateNo = templateNo_Table4_0;
			m_parameterCategory = parameterCategory_Table4_1;
			m_parameterNo = parameterNo_Table4_2;
		}
		private final int m_templateNo;
		private final int m_parameterCategory;
		private final int m_parameterNo;
		private int m_typeOfGeneratingProcess = Table4_3.Forecast;
	}

	public static class Table4_3 {

		public static final int Analysis = 0;
		public static final int Initialization = 1;
		public static final int Forecast = 2;
		public static final int Bias_Corrected_Forecast = 3;
		public static final int Ensemble_Forecast = 4;
		public static final int Probability_Forecast = 5;
		public static final int Forecast_Error = 6;
		public static final int Analysis_Error = 7;
		public static final int Observation = 8;
		public static final int Climatological = 9;
		public static final int Probability_Weighted_Forecast = 10;
		public static final int Bias_Corrected_Ensemble_Forecast = 11;
		public static final int Missing = 255;
	}

	public static class Template4_0 extends DefinitionTemplate {

		private Template4_0(int parameterCategory_Table4_1, int parameterNo_Table4_2) {
			super(0, parameterCategory_Table4_1, parameterNo_Table4_2);
		}

	}

}
