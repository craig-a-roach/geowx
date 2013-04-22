/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.cobalt;

import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.ICodedEnum;
import com.metservice.argon.json.JsonObject;
import com.metservice.argon.json.JsonSchemaException;

/**
 * @author roach
 */
public enum CobaltSurfaceTypeScalar implements ICodedEnum {

	AboveGround(CobaltUnit.m),
	AboveMSL(CobaltUnit.m),
	BelowGround(CobaltUnit.cm),
	BelowMSL(CobaltUnit.m),
	ETA(CobaltUnit.ETA),
	Hybrid(CobaltUnit.level),
	Isentropic(CobaltUnit.K),
	Isobaric(CobaltUnit.hPa),
	Isothermal(CobaltUnit.K),
	PotentialVorticity(CobaltUnit.PV),
	PressureDifferenceFromGround(CobaltUnit.hPa),
	SIGMA(CobaltUnit.SIGMA);

	public CobaltUnit unit() {
		return m_unit;
	}

	private CobaltSurfaceTypeScalar(CobaltUnit unit) {
		assert unit != null;
		m_unit = unit;
	}
	private final CobaltUnit m_unit;

	public static final CodedEnumTable<CobaltSurfaceTypeScalar> Table = new CodedEnumTable<CobaltSurfaceTypeScalar>(
			CobaltSurfaceTypeScalar.class, true, CobaltSurfaceTypeScalar.values());

	public int compareByName(CobaltSurfaceTypeScalar rhs) {
		final String nl = name();
		final String nr = rhs.name();
		return nl.compareTo(nr);
	}

	@Override
	public String qCode() {
		return name();
	}

	public void saveTo(JsonObject dst, String pname) {
		if (dst == null) throw new IllegalArgumentException("object is null");
		dst.putCoded(pname, this);
	}

	public static CobaltSurfaceTypeScalar newInstance(JsonObject src, String pname)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		return src.accessor(pname).datumCoded(Table);
	}
}
