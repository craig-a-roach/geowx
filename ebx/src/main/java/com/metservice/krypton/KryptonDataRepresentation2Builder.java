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

	public void countValidPoints(int count) {
		m_countValidPoints = count;
	}

	public Template5_0 newTemplate5_0(int bitDepth) {
		final Template5_0 neo = new Template5_0(bitDepth);
		m_oDefinition = neo;
		return neo;
	}

	@Override
	public void save(Section2Buffer dst)
			throws KryptonBuildException {
		if (m_oDefinition == null) throw new KryptonBuildException("Missing Template 5");
		dst.int4(m_countValidPoints); // 6-9
		m_oDefinition.save(dst); // 10+
	}

	@Override
	public int sectionNo() {
		return 5;
	}

	public KryptonDataRepresentation2Builder() {
	}

	public KryptonDataRepresentation2Builder(KryptonData2Packer00 packer) {
		if (packer == null) throw new IllegalArgumentException("object is null");
		m_countValidPoints = packer.validPointCount();
		final SimplePackingSpec spec = packer.packingSpec();
		final Template5_0 t = newTemplate5_0(spec.bitDepth);
		t.referenceValue(spec.referenceValue);
		t.binaryScale(spec.binaryScale);
		t.decimalScale(spec.decimalScale);
		t.typeOfOriginalValue(Table5_1.FloatingPoint);
	}

	private int m_countValidPoints;
	private DefinitionTemplate m_oDefinition;

	abstract static class DefinitionTemplate {

		void save(Section2Buffer dst) {
			dst.int2(m_templateNo); // 10-11
			dst.float4(m_referenceValue); // 12-15
			dst.int2(m_binaryScale); // 16-17
			dst.int2(m_decimalScale); // 18-19
			saveRepresentation(dst);
		}

		abstract void saveRepresentation(Section2Buffer dst);

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

	public static class Table5_1 {

		public static final int FloatingPoint = 0;
		public static final int Integer = 1;
	}

	public static class Template5_0 extends DefinitionTemplate {

		@Override
		void saveRepresentation(Section2Buffer dst) {
			dst.u1(m_bitDepth); // 20
			dst.u1(m_typeOfOriginalValue); // 21
		}

		public void typeOfOriginalValue(int valueTable5_1) {
			m_typeOfOriginalValue = valueTable5_1;
		}

		private Template5_0(int bitDepth) {
			super(0);
			m_bitDepth = bitDepth;
		}
		private final int m_bitDepth;
		private int m_typeOfOriginalValue = Table5_1.FloatingPoint;
	}

}
