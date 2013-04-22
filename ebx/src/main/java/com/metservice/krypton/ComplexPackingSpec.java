/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.Ds;

/**
 * @author roach
 */
class ComplexPackingSpec {

	@Override
	public String toString() {
		final Ds ds = Ds.o(getClass());
		ds.a("groupSplittingMethod", groupSplittingMethod);
		ds.a("missingValueManagement", missingValueManagement);
		ds.a("primaryMissingValue", primaryMissingValue);
		ds.a("secondaryMissingValue", secondaryMissingValue);
		ds.a("numberOfGroups", numberOfGroups);
		ds.a("referenceGroupWidths", referenceGroupWidths);
		ds.a("bitsGroupWidths", bitsGroupWidths);
		ds.a("referenceGroupLengths", referenceGroupLengths);
		ds.a("lengthIncrement", lengthIncrement);
		ds.a("lengthLastGroup", lengthLastGroup);
		ds.a("bitsScaledGroupLength", bitsScaledGroupLength);
		return ds.s();
	}

	public ComplexPackingSpec(int groupSplittingMethod, int missingValueManagement, float primaryMissingValue,
			float secondaryMissingValue, int numberOfGroups, int referenceGroupWidths, int bitsGroupWidths,
			int referenceGroupLengths, int lengthIncrement, int lengthLastGroup, int bitsScaledGroupLength) {
		this.groupSplittingMethod = groupSplittingMethod;
		this.missingValueManagement = missingValueManagement;
		this.primaryMissingValue = primaryMissingValue;
		this.secondaryMissingValue = secondaryMissingValue;
		this.numberOfGroups = numberOfGroups;
		this.referenceGroupWidths = referenceGroupWidths;
		this.bitsGroupWidths = bitsGroupWidths;
		this.referenceGroupLengths = referenceGroupLengths;
		this.lengthIncrement = lengthIncrement;
		this.lengthLastGroup = lengthLastGroup;
		this.bitsScaledGroupLength = bitsScaledGroupLength;
	}
	public final int groupSplittingMethod;
	public final int missingValueManagement;
	public final float primaryMissingValue;
	public final float secondaryMissingValue;
	public final int numberOfGroups;
	public final int referenceGroupWidths;
	public final int bitsGroupWidths;
	public final int referenceGroupLengths;
	public final int lengthIncrement;
	public final int lengthLastGroup;
	public final int bitsScaledGroupLength;
}
