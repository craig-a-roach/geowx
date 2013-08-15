/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public class BoronProductExitCode implements IBoronProduct {

	public BoronExitCode exitCode() {
		return m_exitCode;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}

	@Override
	public String toString() {
		return "EXIT-CODE " + m_exitCode;
	}

	public BoronProductExitCode(BoronExitCode exitCode) {
		if (exitCode == null) throw new IllegalArgumentException("object is null");
		m_exitCode = exitCode;
	}

	private final BoronExitCode m_exitCode;
}
