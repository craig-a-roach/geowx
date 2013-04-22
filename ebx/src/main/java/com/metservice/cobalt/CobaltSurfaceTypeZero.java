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
public enum CobaltSurfaceTypeZero implements ICodedEnum {

	Ground, // G1=1
	CloudBase, // G1=2
	CloudTops, // G1=3
	IsothermC0, // G1=4
	AdiabaticCondensationLifted, // G1=5
	MaximumWind, // G1=6
	Tropopause, // G1=7
	NominalTopOfAtmosphere, // G1=8
	SeaBottom, // G1=9
	MSL, // G1=102
	EntireAtmosphereAsSingleLayer, // G1=200
	EntireOceanAsSingleLayer, // G2=201
	HighestTroposphericFreezingLevel, // G2=204
	BoundaryCloudBottom, // G2 209
	BoundaryCloudTop, // G2 210
	BoundaryCloudLayer, // G2 211
	LowCloudBottom, // G2 212
	LowCloudTop, // G2 213
	LowCloudLayer, // G2 214
	MiddleCloudBottom, // G2 222
	MiddleCloudTop, // G2 223
	MiddleCloudLayer, // G2 224
	HighCloudBottom, // G2 232
	HighCloudTop, // G2 233
	HighCloudLayer, // G2 234
	PlanetaryBoundaryLayer, // G2=220
	ConvectiveCloudBottom, // G2=242
	ConvectiveCloudTop, // G2=243
	ConvectiveCloudLayer; // G2=244

	public static final CodedEnumTable<CobaltSurfaceTypeZero> Table = new CodedEnumTable<CobaltSurfaceTypeZero>(
			CobaltSurfaceTypeZero.class, true, CobaltSurfaceTypeZero.values());

	public static CobaltSurfaceTypeZero newInstance(JsonObject src, String pname)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		return src.accessor(pname).datumCoded(Table);
	}

	public int compareByName(CobaltSurfaceTypeZero rhs) {
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
}
