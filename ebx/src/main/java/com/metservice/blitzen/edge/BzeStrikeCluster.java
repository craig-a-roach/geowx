/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
public class BzeStrikeCluster {

	public BzeStrikeClusterShape clusterShape() {
		return m_clusterShape;
	}

	public float qtyMagnitudeAverage() {
		return m_magSum / m_strikes.length;
	}

	public float qtyMagnitudeMax() {
		return m_magMax;
	}

	public float qtyMagnitudeSum() {
		return m_magSum;
	}

	public BzeStrike[] strikes() {
		return m_strikes;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("cid:").append(m_cid);
		sb.append(", shape=").append(m_clusterShape);
		sb.append(", strikes=").append(m_strikes.length);
		sb.append(", magSum=").append(m_magSum);
		sb.append(", magMax=").append(m_magMax);
		return sb.toString();
	}

	public BzeStrikeCluster(int cid, BzeStrike[] strikes, BzeStrikeClusterShape clusterShape, float magSum, float magMax) {
		m_cid = cid;
		m_strikes = strikes;
		m_clusterShape = clusterShape;
		m_magSum = magSum;
		m_magMax = magMax;
	}
	private final int m_cid;
	private final BzeStrike[] m_strikes;
	private final BzeStrikeClusterShape m_clusterShape;
	private final float m_magSum;
	private final float m_magMax;
}
