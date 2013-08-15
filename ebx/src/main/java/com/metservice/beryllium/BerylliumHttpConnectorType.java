/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.ICodedEnum;

/**
 * @author roach
 */
public enum BerylliumHttpConnectorType implements ICodedEnum {

	BLOCKING("blocking"), SELECTOR("selector"), PLATFORM("platform");

	private BerylliumHttpConnectorType(String qlcCode) {
		assert qlcCode != null && qlcCode.length() > 0;
		m_qlcCode = qlcCode;
	}
	private final String m_qlcCode;

	public static final CodedEnumTable<BerylliumHttpConnectorType> Table = new CodedEnumTable<BerylliumHttpConnectorType>(
			BerylliumHttpConnectorType.class, false, BerylliumHttpConnectorType.values());

	@Override
	public String qCode() {
		return m_qlcCode;
	}
}
