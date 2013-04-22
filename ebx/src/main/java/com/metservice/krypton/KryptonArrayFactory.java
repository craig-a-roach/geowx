/*
 * Copyright 2012 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.krypton;

/**
 * @author roach
 */
public class KryptonArrayFactory {

	public boolean[] newEmptyBooleanArray() {
		final int len = m_nx * m_ny;
		return new boolean[len];
	}

	public float[] newEmptyFloatArray() {
		final int len = m_nx * m_ny;
		return new float[len];
	}

	public int nx() {
		return m_nx;
	}

	public int ny() {
		return m_ny;
	}

	@Override
	public String toString() {
		return "x" + m_nx + ",y" + m_ny;
	}

	public KryptonArrayFactory(int nx, int ny) {
		m_nx = nx;
		m_ny = ny;
	}
	private final int m_nx;
	private final int m_ny;
}
