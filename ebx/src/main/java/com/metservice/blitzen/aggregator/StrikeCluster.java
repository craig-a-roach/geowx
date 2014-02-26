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

	public float qtyMagnitudeAverage() {
		return m_magSum / m_strikeCount;
	}

	public float qtyMagnitudeMax() {
		return m_magMax;
	}

	public float qtyMagnitudeSum() {
		return m_magSum;
	}

	public StrikePolygon strikePolygon() {
		return m_strikePolygon;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("vertices=").append(m_strikePolygon.vertexCount());
		sb.append(", strikes=").append(m_strikeCount);
		sb.append(", magSum=").append(m_magSum);
		sb.append(", magMax=").append(m_magMax);
		return sb.toString();
	}

	public StrikeCluster(StrikePolygon polygon, int strikeCount, float magSum, float magMax) {
		assert polygon != null;
		assert strikeCount > 0;
		m_strikePolygon = polygon;
		m_strikeCount = strikeCount;
		m_magSum = magSum;
		m_magMax = magMax;
	}
	private final StrikePolygon m_strikePolygon;
	private final int m_strikeCount;
	private final float m_magSum;
	private final float m_magMax;
}
