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
	public static final int DefaultBackgroundGeneratingProcessId = 0;

	@Override
	void save(Section2Buffer dst)
			throws KryptonBuildException {
		if (m_oDefinition == null) throw new KryptonBuildException("Missing Template 4");
		dst.u2(CoordinateCount); // 6-7
		m_oDefinition.save(dst); // 8+
	}

	public Template4_0 newTemplate4_0(int parameterCategory_Table4_1, int parameterNo_Table4_2) {
		final Template4_0 neo = new Template4_0(parameterCategory_Table4_1, parameterNo_Table4_2);
		m_oDefinition = neo;
		return neo;
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
			dst.u2(m_templateNo); // 8-9
			dst.u1(m_parameterCategory); // 10
			dst.u1(m_parameterNo); // 11
			dst.u1(m_typeOfGeneratingProcess); // 12
			dst.u1(m_backgroundGeneratingProcessId); // 13
			dst.u1(m_on388GeneratingProcessId); // 14
			saveSubDefinition(dst); // 15+
		}

		abstract void saveSubDefinition(Section2Buffer dst);

		public void backgroundGeneratingProcessId(int value) {
			m_backgroundGeneratingProcessId = value;
		}

		public void on388GeneratingProcessId(int valueTableON388_A) {
			m_on388GeneratingProcessId = valueTableON388_A;
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
		private int m_backgroundGeneratingProcessId = DefaultBackgroundGeneratingProcessId;
		private int m_on388GeneratingProcessId = TableON388_A.Reserved;
	}

	public static class HorizontalLayer {

		void save(Section2Buffer dst) {
			m_level1.save(dst);
			m_level2.save(dst);
		}

		public HorizontalLevel level1() {
			return m_level1;
		}

		public HorizontalLevel level2() {
			return m_level2;
		}

		public HorizontalLayer() {
			m_level1 = new HorizontalLevel(Table4_5.GroundOrWaterSurface, 0, 0);
			m_level2 = new HorizontalLevel(Table4_5.Missing, 0, 0);
		}
		private final HorizontalLevel m_level1;
		private final HorizontalLevel m_level2;
	}

	public static class HorizontalLevel {

		void save(Section2Buffer dst) {
			dst.u1(m_type);
			dst.u1(m_scaleFactor);
			dst.int4(m_scaledValue);
		}

		public HorizontalLevel scaledValue(int value) {
			m_scaledValue = value;
			return this;
		}

		public HorizontalLevel scaleFactor(int factor) {
			m_scaleFactor = factor;
			return this;
		}

		public HorizontalLevel type(int valueTable4_5) {
			m_type = valueTable4_5;
			return this;
		}

		HorizontalLevel(int typeTable4_5, int scaleFactor, int scaledValue) {
			m_type = typeTable4_5;
			m_scaleFactor = scaleFactor;
			m_scaledValue = scaledValue;
		}
		private int m_type;
		private int m_scaleFactor;
		private int m_scaledValue;
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
		public static final int Nowcast = 14;
		public static final int Forecast_Confidence_Indicator = 192;
		public static final int Missing = 255;
	}

	public static class Table4_4 {

		public static final int Minute = 0;
		public static final int Hour = 1;
		public static final int Day = 2;
		public static final int Month = 3;
		public static final int Year = 4;
		public static final int Decade = 5;
		public static final int Normal = 6;
		public static final int Century = 7;
		public static final int Hours3 = 10;
		public static final int Hours6 = 11;
		public static final int Hours12 = 12;
		public static final int Second = 13;
		public static final int Missing = 255;
	}

	public static class Table4_5 {

		public static final int GroundOrWaterSurface = 1;
		public static final int CloudBaseLevel = 2;
		public static final int LevelOfCloudTops = 3;
		public static final int LevelOfZeroCIsotherm = 4;
		public static final int LevelOfAdiabaticCondensationLiftedFromSurface = 5;
		public static final int MaximumWindLevel = 6;
		public static final int Tropopause = 7;
		public static final int NominalTopOfAtmosphere = 8;
		public static final int SeaBottom = 9;
		public static final int EntireAtmosphere = 10;
		public static final int CumulonimbusBase_m = 11;
		public static final int CumulonimbusTop_m = 12;
		public static final int IsothermalLevel_K = 20;
		public static final int IsobaricSurface_Pa = 100;
		public static final int MeanSeaLevel = 101;
		public static final int SpecificAltitudeAboveMeanSeaLevel_m = 102;
		public static final int SpecifiedHeightLevelAboveGround_m = 103;
		public static final int SigmaLevel = 104;
		public static final int HybridLevel = 105;
		public static final int DepthBelowLandSurface_m = 106;
		public static final int IsentropicLevel_K = 107;
		public static final int LevelAtSpecifiedPressureDifferenceFromGroundToLevel_Pa = 108;
		public static final int PotentialVorticitySurface_K_m2_kgn1 = 109;
		public static final int EtaLevel = 111;
		public static final int LogarithmicHybridLevel = 113;
		public static final int SnowLevel = 114;
		public static final int MixedLayerDepth_m = 117;
		public static final int HybridHeightLevel = 118;
		public static final int HybridPressureLevel = 119;
		public static final int GeneralizedVerticalHeightCoordinate = 150;
		public static final int DepthBelowSeaLevel_m = 160;
		public static final int DepthBelowWaterSurface_m = 161;
		public static final int LakeOrRiverBottom = 162;
		public static final int BottomOfSedimentLayer = 163;
		public static final int BottomOfThermallyActiveSedimentLayer = 164;
		public static final int BottomOfSedimentLayerPenetratedByThermalWave = 165;
		public static final int MaxingLayer = 166;
		public static final int EntireAtmosphereConsideredSingleLayer = 200;
		public static final int EntireOceanConsideredSingleLayer = 201;
		public static final int HighestTroposphericFreezingLevel = 204;
		public static final int GridScaleCloudBottomLevel = 206;
		public static final int GridScaleCloudTopLevel = 207;
		public static final int BoundaryLayerCloudBottomLevel = 209;
		public static final int BoundaryLayerCloudTopLevel = 210;
		public static final int BoundaryLayerCloudLayer = 211;
		public static final int LowCloudBottomLevel = 212;
		public static final int LowCloudTopLevel = 213;
		public static final int LowCloudLayer = 214;
		public static final int CloudCeiling = 215;
		public static final int PlanetaryBoundaryLayer = 220;
		public static final int LayerBetweenTwoHybridLevels = 221;
		public static final int MiddleCloudBottomLevel = 222;
		public static final int MiddleCloudTopLevel = 223;
		public static final int MiddleCloudLayer = 224;
		public static final int HighCloudBottomLevel = 232;
		public static final int HighCloudTopLevel = 233;
		public static final int HighCloudLayer = 234;
		public static final int OceanIsothermLevel_C10 = 235;
		public static final int LayerBetweenTwoDepthsBelowOceanSurface = 236;
		public static final int BottomOfOceanMixedLayer_m = 237;
		public static final int BottomOfOceanIsothermalLayer_m = 238;
		public static final int LayerOceanSurfaceAnd26COceanIsothermalLevel = 239;
		public static final int OceanMixedLayer = 240;
		public static final int OrderedSequenceOfData = 241;
		public static final int ConvectiveCloudBottomLevel = 242;
		public static final int ConvectiveCloudTopLevel = 243;
		public static final int ConvectiveCloudLayer = 244;
		public static final int LowestLevelOfWetBulbZero = 245;
		public static final int MaximumEquivalentPotentialTemperatureLevel = 246;
		public static final int EquilibriumLevel = 247;
		public static final int ShallowConvectiveCloudBottomLevel = 248;
		public static final int ShallowConvectiveCloudTopLevel = 249;
		public static final int DeepConvectiveCloudBottomLevel = 251;
		public static final int DeepConvectiveCloudTopLevel = 252;
		public static final int LowestBottomLevelSupercooledLiquidWaterLayer = 253;
		public static final int HighestTopLevelSupercooledLiquidWaterLayer = 254;
		public static final int Missing = 255;
	}

	public static class TableON388_A {

		public static final int Reserved = 0;
		public static final int Missing = 255;
	}

	public static class Template4_0 extends DefinitionTemplate {

		public static final int DefaultHoursAfterReferenceTimeCutoff = 0;
		public static final int DefaultMinutesAfterReferenceTimeCutoff = 0;
		public static final int DefaultForecastTime = 0;

		@Override
		void saveSubDefinition(Section2Buffer dst) {
			dst.u2(m_hoursAfterReferenceTimeCutoff); // 15-16
			dst.u1(m_minutesAfterReferenceTimeCutoff); // 17
			dst.u1(m_unitOfTimeRange); // 18
			dst.int4(m_forecastTime);// 19-22
			m_horizontalLayer.save(dst); // 23+
		}

		public void forecastTime(int units) {
			m_forecastTime = units;
		}

		public HorizontalLayer horizontalLayer() {
			return m_horizontalLayer;
		}

		public void hoursAfterReferenceTimeCutoff(int hours) {
			m_hoursAfterReferenceTimeCutoff = hours;
		}

		public void minutesAfterReferenceTimeCutoff(int minutes) {
			m_minutesAfterReferenceTimeCutoff = minutes;
		}

		public void unitOfTimeRange(int valueTable4_4) {
			m_unitOfTimeRange = valueTable4_4;
		}

		private Template4_0(int parameterCategory_Table4_1, int parameterNo_Table4_2) {
			super(0, parameterCategory_Table4_1, parameterNo_Table4_2);
		}
		private int m_hoursAfterReferenceTimeCutoff = DefaultHoursAfterReferenceTimeCutoff;
		private int m_minutesAfterReferenceTimeCutoff = DefaultHoursAfterReferenceTimeCutoff;
		private int m_unitOfTimeRange = Table4_4.Hour;
		private int m_forecastTime = DefaultForecastTime;
		private final HorizontalLayer m_horizontalLayer = new HorizontalLayer();
	}

}
