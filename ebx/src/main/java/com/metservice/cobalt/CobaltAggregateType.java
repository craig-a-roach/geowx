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
public enum CobaltAggregateType implements ICodedEnum {

	Accumulation("ACC"), Average("AVG"), Difference("DIFF"), Between("THRU"), Maximum("MAX"), Minimum("MIN");

	public String qccPrefix() {
		return m_qccPrefix;
	}

	private CobaltAggregateType(String qccPrefix) {
		assert qccPrefix != null && qccPrefix.length() > 0;
		m_qccPrefix = qccPrefix;
	}
	private final String m_qccPrefix;

	public static final CodedEnumTable<CobaltAggregateType> Table = new CodedEnumTable<CobaltAggregateType>(
			CobaltAggregateType.class, true, CobaltAggregateType.values());

	public static CobaltAggregateType newInstance(JsonObject src, String pname)
			throws JsonSchemaException {
		if (src == null) throw new IllegalArgumentException("object is null");
		return src.accessor(pname).datumCoded(Table);
	}

	public int compareByName(CobaltAggregateType rhs) {
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
