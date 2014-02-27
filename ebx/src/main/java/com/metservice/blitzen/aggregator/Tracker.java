/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

/**
 * @author roach
 */
class Tracker {

	public boolean hasVisited(int strikeId) {
		return m_visited[strikeId];
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append('[');
		final int clusterCount = m_visited.length;
		boolean sep = false;
		for (int id = 0; id < clusterCount; id++) {
			if (m_visited[id]) {
				if (sep) {
					sb.append(",");
				}
				sb.append(id);
				sep = true;
			}
		}
		sb.append(']');
		return sb.toString();
	}

	public void visited(int strikeId) {
		m_visited[strikeId] = true;
	}

	public Tracker(Strike[] cluster) {
		m_visited = new boolean[cluster.length];
	}
	private final boolean[] m_visited;
}
