/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

/**
 * @author roach
 */
class StrikeClusterTable {

	@Override
	public String toString() {
		final int clusterCount = m_clusterArray.length;
		final StringBuilder sb = new StringBuilder();
		sb.append("strikes=").append(m_strikeCount).append('\n');
		sb.append("noise(").append(m_noise).append(")\n");
		sb.append("clusterCount=").append(clusterCount).append("\n");
		sb.append("sumClusterMagnitude=").append(m_sumClusterMagnitude).append("\n");
		sb.append("clusters(").append("\n");
		for (int i = 0; i < clusterCount; i++) {
			sb.append(m_clusterArray[i]).append("\n");
		}
		sb.append(")");
		return sb.toString();
	}

	public StrikeClusterTable(StrikeCluster[] clusterArray, StrikeCluster noise, int strikeCount, float sumClusterMagnitude) {
		if (clusterArray == null) throw new IllegalArgumentException("object is null");
		if (noise == null) throw new IllegalArgumentException("object is null");
		m_clusterArray = clusterArray;
		m_noise = noise;
		m_strikeCount = strikeCount;
		m_sumClusterMagnitude = sumClusterMagnitude;
	}
	private final StrikeCluster[] m_clusterArray;
	private final StrikeCluster m_noise;
	private final int m_strikeCount;
	private final float m_sumClusterMagnitude;
}
