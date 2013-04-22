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
public enum CobaltResolutionUnit implements ICodedEnum {

	Degrees("deg"), Metres("m");

	public String qccSuffix() {
		return m_qccSuffix;
	}

	private CobaltResolutionUnit(String qccSuffix) {
		if (qccSuffix == null || qccSuffix.length() == 0) throw new IllegalArgumentException("string is null or empty");
		m_qccSuffix = qccSuffix;
	}

	private final String m_qccSuffix;

	public static final CodedEnumTable<CobaltResolutionUnit> Table = new CodedEnumTable<CobaltResolutionUnit>(
			CobaltResolutionUnit.class, true, CobaltResolutionUnit.values());

	public int compareByName(CobaltResolutionUnit rhs) {
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

	public static CobaltResolutionUnit newInstance(JsonObject src, String pname)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		return src.accessor(pname).datumCoded(Table);
	}
}
