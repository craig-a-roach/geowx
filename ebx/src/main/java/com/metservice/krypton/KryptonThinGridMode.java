/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

import com.metservice.argon.CodedEnumTable;
import com.metservice.argon.ICodedEnum;

/**
 * @author roach
 */
public enum KryptonThinGridMode implements ICodedEnum {
	FixYVarX, FixXVarY;

	public static final CodedEnumTable<KryptonThinGridMode> Table = new CodedEnumTable<KryptonThinGridMode>(
			KryptonThinGridMode.class, true, KryptonThinGridMode.values());

	@Override
	public String qCode() {
		return name();
	}
}
