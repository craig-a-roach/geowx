/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.beryllium;

import com.metservice.argon.ArgonNumber;
import com.metservice.argon.json.JsonObject;

/**
 * @author roach
 */
class TestImpJsonContent implements IBerylliumJsonHttpContent {

	@Override
	public void saveTo(JsonObject dst) {
		dst.putString("id", ArgonNumber.intToDec5(m_id));
		for (int i = 0; i < m_size; i++) {
			final String pname = "field" + ArgonNumber.intToDec5(i);
			final int cc = m_size - i;
			final StringBuilder sb = new StringBuilder();
			for (int c = 0; c < cc; c++) {
				final char ch = (char) ('A' + (c % 26));
				sb.append(ch);
			}
			dst.putString(pname, sb.toString());
		}
	}

	@Override
	public String toString() {
		return "ID " + m_id + "(" + m_size + ")";
	}

	public TestImpJsonContent(int id, int size) {
		m_id = id;
		m_size = size;
	}

	private final int m_id;
	private final int m_size;
}
