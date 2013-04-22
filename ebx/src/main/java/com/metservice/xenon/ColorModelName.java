/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievnumable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.xenon;

import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.ICodedEnum;

/**
 * @author roach
 */
enum ColorModelName implements ICodedEnum {
	rgb("rgb", 3, true), hsb("hsb", 3, true);

	public static final CodedEnumTable<ColorModelName> Table = new CodedEnumTable<>(ColorModelName.class, false,
			ColorModelName.values());

	public int componentCount() {
		return m_componentCount;
	}

	@Override
	public String qCode() {
		return m_qCode;
	}

	public boolean supportsAlpha() {
		return m_supportsAlpha;
	}

	private ColorModelName(String qCode, int componentCount, boolean supportsAlpha) {
		m_qCode = qCode;
		m_componentCount = componentCount;
		m_supportsAlpha = supportsAlpha;
	}
	private final String m_qCode;
	private final int m_componentCount;
	private final boolean m_supportsAlpha;
}
