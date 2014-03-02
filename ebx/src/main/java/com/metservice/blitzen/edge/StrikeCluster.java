/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
class StrikeCluster {

	public float qtyMagnitudeAverage() {
		return m_magSum / m_strikes.length;
	}

	public float qtyMagnitudeMax() {
		return m_magMax;
	}

	public float qtyMagnitudeSum() {
		return m_magSum;
	}

	public Strike[] strikes() {
		return m_strikes;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("cid:").append(m_cid);
		sb.append(", strikes=").append(m_strikes.length);
		sb.append(", magSum=").append(m_magSum);
		sb.append(", magMax=").append(m_magMax);
		return sb.toString();
	}

	public StrikeCluster(int cid, Strike[] strikes, float magSum, float magMax) {
		m_cid = cid;
		m_strikes = strikes;
		m_magSum = magSum;
		m_magMax = magMax;
	}
	private final int m_cid;
	private final Strike[] m_strikes;
	private final float m_magSum;
	private final float m_magMax;
}
