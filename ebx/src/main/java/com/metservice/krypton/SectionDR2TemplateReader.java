/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
abstract class SectionDR2TemplateReader extends SectionDR2Reader {

	protected String templateSpec() {
		return "5." + m_templateNo;
	}

	public float b12_15_referenceValue() {
		return float4(12);
	}

	public int b1617_binaryScaleFactor() {
		return int2(16);
	}

	public int b1819_decimalScaleFactor() {
		return int2(18);
	}

	public int b20_bitsPerValue() {
		return intu1(20);
	}

	public int b22_compressionMethod() {
		return intu1(22);
	}

	public int b22_groupSplittingMethod() {
		return intu1(22);
	}

	public int b23_compressionRatio() {
		return intu1(22);
	}

	public int b23_missingValueManagement() {
		return intu1(23);
	}

	public float b24_27_primaryMissingValue() {
		return float4(24);
	}

	public float b28_31_secondaryMissingValue() {
		return float4(28);
	}

	public int b32_35_numberOfGroups() {
		return int4(32);
	}

	public int b36_referenceGroupWidths() {
		return intu1(36);
	}

	public int b37_bitsGroupWidths() {
		return intu1(37);
	}

	public int b38_41_referenceGroupLengths() {
		return int4(38);
	}

	public int b42_lengthIncrement() {
		return intu1(42);
	}

	public int b43_46_lengthLastGroup() {
		return int4(43);
	}

	public int b47_bitsScaledGroupLength() {
		return intu1(47);
	}

	public int b48_orderSpatial() {
		return intu1(48);
	}

	public int b49_descriptorSpatial() {
		return intu1(49);
	}

	public ComplexPackingSpec newComplexPackingSpec() {
		return new ComplexPackingSpec(b22_groupSplittingMethod(), b23_missingValueManagement(), b24_27_primaryMissingValue(),
				b28_31_secondaryMissingValue(), b32_35_numberOfGroups(), b36_referenceGroupWidths(), b37_bitsGroupWidths(),
				b38_41_referenceGroupLengths(), b42_lengthIncrement(), b43_46_lengthLastGroup(),
				b47_bitsScaledGroupLength());
	}

	public abstract IKryptonDataSource newDataSource(KryptonDecoder decoder, IKryptonBitmapSource oBitmap, SectionBD2Reader rBD)
			throws KryptonTableException, KryptonCodeException;

	public Jpeg2000PackingSpec newJpeg2000PackingSpec() {
		return new Jpeg2000PackingSpec(b22_compressionMethod(), b23_compressionRatio());
	}

	public SimplePackingSpec newSimplePackingSpec() {
		return new SimplePackingSpec(b12_15_referenceValue(), b1617_binaryScaleFactor(), b1819_decimalScaleFactor(),
				b20_bitsPerValue());
	}

	public SpatialPackingSpec newSpatialPackingSpec() {
		return new SpatialPackingSpec(b48_orderSpatial(), b49_descriptorSpatial());
	}

	public SectionDR2TemplateReader(SectionDR2Reader base, int templateNo) {
		super(base);
		m_templateNo = templateNo;
	}
	protected final int m_templateNo;
}
