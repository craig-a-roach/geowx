/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public class BoronProductStreamWarnDecode implements IBoronProduct {

	public boolean isStdErr() {
		return m_type == OutStreamType.StdErr;
	}

	@Override
	public boolean isTerminal() {
		return false;
	}

	@Override
	public String toString() {
		return "DECODE-WARN " + m_type;
	}

	BoronProductStreamWarnDecode(OutStreamType type) {
		if (type == null) throw new IllegalArgumentException("object is null");
		m_type = type;
	}

	private final OutStreamType m_type;
}
