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

	public static final int ZeroBasicAngle = 1;
	public static final int ZeroSubAngle = 1_000_000;

	@Override
	public void save(Section2Buffer dst) {
		dst.u1(SourceOfGridDefinition_Table3_0);
		dst.int4(m_numberOfDataPoints); // 7-10
		dst.u1(QuasiRegularOctets); // 11
		dst.u1(QuasiRegularInterpretation); // 12
	}

	@Override
	public int sectionNo() {
		return 3;
	}

	public KryptonGrid2Builder(int numberOfDataPoints) {
		m_numberOfDataPoints = numberOfDataPoints;
	}
	private final int m_numberOfDataPoints;

	static abstract class DefinitionTemplate {

		void save(Section2Buffer dst) {
			dst.int2(m_templateNo);
			saveProjection(dst);
		}

		abstract void saveProjection(Section2Buffer dst);

		protected DefinitionTemplate(int templateNo_Table3_1) {
			m_templateNo = templateNo_Table3_1;
		}
		private final int m_templateNo;
	}

	public static class LatitudeLongitudeTemplate3_0 extends DefinitionTemplate {

		private int resolutionAndComponentFlags() {
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

		private int scale(float in) {
			final int ba = m_basicAngle == 0 ? ZeroBasicAngle : m_basicAngle;
			final int sa = m_subAngle == 0 ? ZeroSubAngle : m_subAngle;
			return Math.round(in * sa / ba);
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
		}

		public void basicAngle(int degrees) {
			m_basicAngle = degrees;
		}

		public void subdivisionsOfBasicAngle(int count) {
			m_subAngle = count;
		}

		public LatitudeLongitudeTemplate3_0(int nparallel, int nmeridian, float la1, float lo1, float la2, float lo2) {
			super(0);
			m_nparallel = nparallel;
			m_nmeridian = nmeridian;
			m_la1 = la1;
			m_lo1 = lo1;
			m_la2 = la2;
			m_lo2 = lo2;
		}
		private final int m_nparallel;
		private final int m_nmeridian;
		private final float m_la1;
		private final float m_lo1;
		private final float m_la2;
		private final float m_lo2;
		private int m_basicAngle = DefaultBasicAngle;
		private int m_subAngle = DefaultSubAngle;
		private final boolean m_incrementIgiven = DefaultIncrementIGiven;
		private final boolean m_incrementJgiven = DefaultIncrementJGiven;
		private final boolean m_resolveVectorUVToGridXY = DefaultResolveVectorUVToGridXY;
	}
	// 15u1=6
	// 16u1=0
	// 17-20i4=0
	// 21u1=0
	// 22-25i4=0
	// 26u1=0
	// 27-30i4=0
	// 31-34i4=360
	// 35-38i4=181
	// 39-42i4=0
	// 43-46i4=0
	// 47-50i4=90000000
	// 51-54i4=0
	// 55u1=48
	// 56-59i4=-90000000
	// 60-63i4=359000000
	// 64-67i4=1000000
	// 68-71i4=1000000
	// 72u1=0

	// System.out.println("15u1=" + r.intu1(15));
	// System.out.println("16u1=" + r.intu1(16));
	// System.out.println("17-20i4=" + r.int4(17));
	// System.out.println("21u1=" + r.intu1(21));
	// System.out.println("22-25i4=" + r.int4(22));
	// System.out.println("26u1=" + r.intu1(26));
	// System.out.println("27-30i4=" + r.int4(27));
	// System.out.println("31-34i4=" + r.int4(31));
	// System.out.println("35-38i4=" + r.int4(35));
	// System.out.println("39-42i4=" + r.int4(39));
	// System.out.println("43-46i4=" + r.int4(43));
	// System.out.println("47-50i4=" + r.int4(47));
	// System.out.println("51-54i4=" + r.int4(51));
	// System.out.println("55u1=" + r.intu1(55));
	// System.out.println("56-59i4=" + r.int4(56));
	// System.out.println("60-63i4=" + r.int4(60));
	// System.out.println("64-67i4=" + r.int4(64));
	// System.out.println("68-71i4=" + r.int4(68));
	// System.out.println("72u1=" + r.intu1(72));

}
