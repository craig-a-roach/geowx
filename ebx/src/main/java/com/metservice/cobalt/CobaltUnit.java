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
public enum CobaltUnit implements ICodedEnum {

	K("K", CobaltQuantityType.Temperature),
	hPa("hPa", CobaltQuantityType.Pressure),
	m("m", CobaltQuantityType.Linear),
	cm("cm", CobaltQuantityType.Linear),
	PV("K m2 kg-1 s-1", CobaltQuantityType.PotentialVorticity),
	ETA("", CobaltQuantityType.Ratio),
	SIGMA("", CobaltQuantityType.Ratio),
	level("", CobaltQuantityType.Index);

	@Override
	public String qCode() {
		return name();
	}

	public CobaltQuantityType quantity() {
		return m_quantity;
	}

	public String zccSuffix() {
		return m_zccSuffix;
	}

	private CobaltUnit(String zccSuffix, CobaltQuantityType quantity) {
		assert zccSuffix != null;
		assert quantity != null;
		m_zccSuffix = zccSuffix;
		m_quantity = quantity;
	}

	private final String m_zccSuffix;
	private final CobaltQuantityType m_quantity;

	public static final CodedEnumTable<CobaltUnit> Table = new CodedEnumTable<CobaltUnit>(CobaltUnit.class, true,
			CobaltUnit.values());

}
