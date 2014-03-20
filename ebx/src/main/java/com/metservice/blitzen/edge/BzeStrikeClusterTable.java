/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
public class BzeStrikeClusterTable {

	public BzeStrikeBounds bounds() {
		return m_bounds;
	}

	public BzeStrikeCluster[] clusterArray() {
		return m_clusterArray;
	}

	public BzeStrike[] noiseArray() {
		return m_noiseArray;
	}

	@Override
	public String toString() {
		final int clusterCount = m_clusterArray.length;
		final int noiseCount = m_noiseArray.length;
		final StringBuilder sb = new StringBuilder();
		sb.append("eps=").append(m_eps).append('\n');
		sb.append("strikes=").append(m_strikeCount).append('\n');
		sb.append("clusterCount=").append(clusterCount).append("\n");
		sb.append("noiseCount=").append(noiseCount).append(")\n");
		sb.append("sumClusterMagnitude=").append(m_sumClusterMagnitude).append("\n");
		sb.append("maxClusterMagnitude=").append(m_sumClusterMagnitude).append("\n");
		sb.append("sumNoiseMagnitude=").append(m_sumNoiseMagnitude).append("\n");
		sb.append("bounds(").append(m_bounds).append(")\n");
		sb.append("clusters(").append("\n");
		for (int i = 0; i < clusterCount; i++) {
			sb.append(m_clusterArray[i]).append("\n");
		}
		sb.append(")");
		return sb.toString();
	}

	public BzeStrikeClusterTable(float eps, BzeStrikeCluster[] clusterArray, BzeStrike[] noiseArray, int strikeCount,
			float sumClusterMag, float sumNoiseMag, BzeStrikeBounds bounds) {
		if (clusterArray == null) throw new IllegalArgumentException("object is null");
		if (noiseArray == null) throw new IllegalArgumentException("object is null");
		m_eps = eps;
		m_clusterArray = clusterArray;
		m_noiseArray = noiseArray;
		m_strikeCount = strikeCount;
		m_sumClusterMagnitude = sumClusterMag;
		m_sumNoiseMagnitude = sumNoiseMag;
		m_bounds = bounds;
	}
	private final float m_eps;
	private final BzeStrikeCluster[] m_clusterArray;
	private final BzeStrike[] m_noiseArray;
	private final int m_strikeCount;
	private final float m_sumClusterMagnitude;
	private final float m_sumNoiseMagnitude;
	private final BzeStrikeBounds m_bounds;
}
