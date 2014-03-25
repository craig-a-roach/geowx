/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
public class BzeStrikePolyline extends AbstractStrikePolyline {

	@Override
	boolean isClosed() {
		return false;
	}

	public float thick() {
		return m_grid;
	}

	public BzeStrikePolyline(float[] xyPairs, float grid) {
		super(xyPairs);
		m_grid = grid;
	}
	private final float m_grid;
}
