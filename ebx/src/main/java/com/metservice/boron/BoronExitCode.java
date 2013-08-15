/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

/**
 * @author roach
 */
public class BoronExitCode {

	@Override
	public String toString() {
		return Integer.toString(m_value);
	}

	public int value() {
		return m_value;
	}

	public BoronExitCode(int value) {
		m_value = value;
	}

	private final int m_value;
}
