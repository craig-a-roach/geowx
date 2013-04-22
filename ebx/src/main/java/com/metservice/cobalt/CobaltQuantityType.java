/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.ICodedEnum;

/**
 * @author roach
 */
public enum CobaltQuantityType implements ICodedEnum {

	Temperature, Pressure, Linear, PotentialVorticity, Ratio, Index;

	public static final CodedEnumTable<CobaltQuantityType> Table = new CodedEnumTable<CobaltQuantityType>(
			CobaltQuantityType.class, true, CobaltQuantityType.values());

	@Override
	public String qCode() {
		return name();
	}
}
