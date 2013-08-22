/*
 * Copyright 2007 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import com.metservice.argon.ArgonNumber;

/**
 * 
 * @author roach
 */
class InstructionAddress {
	public int pc() {
		return m_pc;
	}

	@Override
	public String toString() {
		return ArgonNumber.intToDec4(m_pc);
	}

	public InstructionAddress(int pc) {
		m_pc = pc;
	}

	private final int m_pc;
}
