/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonGrid2Builder extends Section2Builder {

	public static final int SourceOfGridDefinition_Table3_0 = 0;
	public static final int QuasiRegularOctets = 0;
	public static final int QuasiRegularInterpretation = 0;
	public static final int DefaultBasicAngle = 0;
	public static final int DefaultSubAngle = 0;
	public static final boolean DefaultIncrementIGiven = true;
	public static final boolean DefaultIncrementJGiven = true;
	public static final boolean DefaultResolveVectorUVToGridXY = false;
	public static final boolean DefaultScanIWestEast = true;
	public static final boolean DefaultScanJSouthNorth = false;
	public static final boolean DefaultScanIAdjacent = true;
	public static final boolean DefaultScanRowsAlternating = false;

	public static final int ZeroBasicAngle = 1;
	public static final int ZeroSubAngle = 1_000_000;

	public Template3_0 newTemplate3_0(int nparallel, float dparallel, int nmeridian, float dmeridian, float la1, float lo1,
			float la2, float lo2) {
		final Template3_0 neo = new Template3_0(nparallel, dparallel, nmeridian, dmeridian, la1, lo1, la2, lo2);
		m_oDefinition = neo;
		return neo;
	}

	@Override
	public void save(Section2Buffer dst)
			throws KryptonBuildException {
		if (m_oDefinition == null) throw new KryptonBuildException("Missing Template 3");
		final int n = m_oDefinition.numberOfDataPoints();
		dst.u1(SourceOfGridDefinition_Table3_0); // 6
		dst.int4(n); // 7-10
		dst.u1(QuasiRegularOctets); // 11
		dst.u1(QuasiRegularInterpretation); // 12
		m_oDefinition.save(dst); // 13+
	}

	@Override
	public int sectionNo() {
		return 3;
	}

	public KryptonGrid2Builder() {
	}

	private DefinitionTemplate m_oDefinition;

	abstract static class DefinitionTemplate {

		abstract int numberOfDataPoints();

		void save(Section2Buffer dst) {
			dst.int2(m_templateNo); // 13-14
			dst.u1(m_shapeOfEarth); // 15
			dst.u1(m_scaleFactorSpherical); // 16
			dst.int4(m_scaleValueSpherical); // 17-20
			dst.u1(m_scaleFactorMajor); // 21
			dst.int4(m_scaleValueMajor); // 22-25
			dst.u1(m_scaleFactorMinor); // 26
			dst.int4(m_scaleValueMinor); // 27-30
			saveProjection(dst);
		}

		abstract void saveProjection(Section2Buffer dst);

		protected int resolutionAndComponentFlags() {
			int mask = 0x0;
			if (m_incrementIgiven) {
				mask |= 0x20;
			}
			if (m_incrementJgiven) {
				mask |= 0x10;
			}
			if (m_resolveVectorUVToGridXY) {
				mask |= 0x8;
			}
			return mask;
		}

		protected int scale(float in) {
			final int ba = m_basicAngle == 0 ? ZeroBasicAngle : m_basicAngle;
			final int sa = m_subAngle == 0 ? ZeroSubAngle : m_subAngle;
			return Math.round(in * sa / ba);
		}

		protected int scanningMode() {
			int mask = 0x0;
			if (!m_scanIWestEast) {
				mask |= 0x80;
			}
			if (m_scanJSouthNorth) {
				mask |= 0x40;
			}
			if (!m_scanIAdjacent) {
				mask |= 0x20;
			}
			if (m_scanRowsAlternating) {
				mask |= 0x10;
			}
			return mask;
		}

		public void basicAngle(int degrees) {
			m_basicAngle = degrees;
		}

		public void incrementsGiven(boolean directionI, boolean directionJ) {
			m_incrementIgiven = directionI;
			m_incrementJgiven = directionJ;
		}

		public void oblate(int major, int minor, int exp10) {
			m_scaleValueMajor = major;
			m_scaleValueMinor = minor;
			m_scaleFactorMajor = exp10;
			m_scaleFactorMinor = exp10;
		}

		public void resolveUVToGridXY(boolean enabled) {
			m_resolveVectorUVToGridXY = enabled;
		}

		public void scanAdjacent(boolean directionI) {
			m_scanIAdjacent = directionI;
		}

		public void scanDirection(boolean westEast, boolean southNorth) {
			m_scanIWestEast = westEast;
			m_scanJSouthNorth = southNorth;
		}

		public void scanRowsAlternating(boolean enabled) {
			m_scanRowsAlternating = enabled;
		}

		public void shapeOfEarth(int valueTable3_2) {
			m_shapeOfEarth = valueTable3_2;
		}

		public void spherical(int radius, int exp10) {
			m_scaleValueSpherical = radius;
			m_scaleFactorSpherical = exp10;
		}

		public void subdivisionsOfBasicAngle(int count) {
			m_subAngle = count;
		}

		protected DefinitionTemplate(int templateNo_Table3_1) {
			m_templateNo = templateNo_Table3_1;
		}
		private final int m_templateNo;
		private int m_shapeOfEarth = Table3_2.Spherical_R6371;
		private int m_scaleFactorSpherical;
		private int m_scaleValueSpherical;
		private int m_scaleFactorMajor;
		private int m_scaleValueMajor;
		private int m_scaleFactorMinor;
		private int m_scaleValueMinor;
		protected int m_basicAngle = DefaultBasicAngle;
		protected int m_subAngle = DefaultSubAngle;
		protected boolean m_incrementIgiven = DefaultIncrementIGiven;
		protected boolean m_incrementJgiven = DefaultIncrementJGiven;
		protected boolean m_resolveVectorUVToGridXY = DefaultResolveVectorUVToGridXY;
		protected boolean m_scanIWestEast = DefaultScanIWestEast;
		protected boolean m_scanJSouthNorth = DefaultScanJSouthNorth;
		protected boolean m_scanIAdjacent = DefaultScanIAdjacent;
		protected boolean m_scanRowsAlternating = DefaultScanRowsAlternating;
	}

	public static class Table3_2 {

		public static final int Spherical_R6367 = 0;
		public static final int Spherical_Specified = 1;
		public static final int Oblate_IAU1965 = 2;
		public static final int Oblate_SpecifiedKM = 3;
		public static final int Oblate_GRS1980 = 4;
		public static final int Oblate_WGS1984 = 5;
		public static final int Spherical_R6371 = 6;
		public static final int Oblate_SpecifiedM = 7;
		public static final int Spherical_R6371WGS84 = 8;
		public static final int Oblate_OSGB1936 = 9;
	}

	public static class Template3_0 extends DefinitionTemplate {

		@Override
		int numberOfDataPoints() {
			return m_nparallel * m_nmeridian;
		}

		@Override
		void saveProjection(Section2Buffer dst) {
			dst.int4(m_nparallel); // 31-34
			dst.int4(m_nmeridian); // 35-38
			dst.int4(m_basicAngle); // 39-42
			dst.int4(m_subAngle); // 43-46
			dst.int4(scale(m_la1)); // 47-50
			dst.int4(scale(m_lo1)); // 51-54
			dst.u1(resolutionAndComponentFlags()); // 55
			dst.int4(scale(m_la2)); // 56-59
			dst.int4(scale(m_lo2)); // 60-63
			dst.int4(scale(m_dparallel)); // 64-67
			dst.int4(scale(m_dmeridian)); // 68-71
			dst.u1(scanningMode()); // 72
		}

		private Template3_0(int nparallel, float dparallel, int nmeridian, float dmeridian, float la1, float lo1, float la2,
				float lo2) {
			super(0);
			m_nparallel = nparallel;
			m_dparallel = dparallel;
			m_nmeridian = nmeridian;
			m_dmeridian = dmeridian;
			m_la1 = la1;
			m_lo1 = lo1;
			m_la2 = la2;
			m_lo2 = lo2;
		}
		private final int m_nparallel;
		private final float m_dparallel;
		private final int m_nmeridian;
		private final float m_dmeridian;
		private final float m_la1;
		private final float m_lo1;
		private final float m_la2;
		private final float m_lo2;
	}
}
