/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

/**
 * @author roach
 */
class StrikeCluster {

	public float qtyMagnitude() {
		return m_qtyMagnitude;
	}

	@Override
	public String toString() {
		return "strikes=" + m_strikes.length + ", mag=" + m_qtyMagnitude;
	}

	public StrikeCluster(Strike[] strikes, float qtyMagnitude) {
		assert strikes != null;
		m_strikes = strikes;
		m_qtyMagnitude = qtyMagnitude;
	}
	private final Strike[] m_strikes;
	private final float m_qtyMagnitude;
}
