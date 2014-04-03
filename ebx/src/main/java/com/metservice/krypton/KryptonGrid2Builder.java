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

	public static DefinitionTemplate newLatitudeLongitude3_0() {
		return null;
	}

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

	public static abstract class DefinitionTemplate {

		void save(Section2Buffer dst) {
			dst.int2(m_templateNo);
			saveProjection(dst);
		}

		abstract void saveProjection(Section2Buffer dst);

		public DefinitionTemplate(int templateNo_Table3_1) {
			m_templateNo = templateNo_Table3_1;
		}
		private final int m_templateNo;
	}

}
