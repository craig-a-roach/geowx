/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public class BoronProductInterpreterFailure implements IBoronProduct {

	public BoronInterpreterFailure diagnostic() {
		return m_bif;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}

	@Override
	public String toString() {
		return "INTERPRETER-FAILURE\n" + m_bif;
	}

	public BoronProductInterpreterFailure(BoronInterpreterFailure bif) {
		if (bif == null) throw new IllegalArgumentException("object is null");
		m_bif = bif;
	}

	private final BoronInterpreterFailure m_bif;
}
