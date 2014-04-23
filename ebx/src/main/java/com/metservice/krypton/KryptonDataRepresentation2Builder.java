/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;


/**
 * @author roach
 */
public class KryptonDataRepresentation2Builder extends Section2Builder {

	public static final float DefaultReferenceValue = 0.0f;
	public static final int DefaultBinaryScale = 0;
	public static final int DefaultDecimalScale = 0;

	@Override
	public void save(Section2Buffer dst) {
		if (m_oDefinition == null) throw new IllegalStateException("missing Template 5");
		dst.int4(m_countValidPoints); // 6-9
		m_oDefinition.save(dst); // 10+

	}

	@Override
	public int sectionNo() {
		return 5;
	}

	public KryptonDataRepresentation2Builder(int countValidPoints) {
		m_countValidPoints = countValidPoints;
	}
	private final int m_countValidPoints;
	private DefinitionTemplate m_oDefinition;

	abstract static class DefinitionTemplate {

		void save(Section2Buffer dst) {
			dst.int2(m_templateNo); // 10-11
			dst.float4(m_referenceValue); // 12-15
			dst.int2(m_binaryScale); // 16-17.
			dst.int2(m_decimalScale); // 18-19.
		}

		public void binaryScale(int signedScale) {
			m_binaryScale = signedScale;
		}

		public void decimalScale(int signedScale) {
			m_decimalScale = signedScale;
		}

		public void referenceValue(float value) {
			m_referenceValue = value;
		}

		protected DefinitionTemplate(int templateNo_Table5_0) {
			m_templateNo = templateNo_Table5_0;
		}
		private final int m_templateNo;

		private float m_referenceValue = DefaultReferenceValue;
		private int m_binaryScale = DefaultBinaryScale;
		private int m_decimalScale = DefaultDecimalScale;
	}

}
