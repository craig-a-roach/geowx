/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.util.List;

/**
 * @author roach
 */
class StrikeClusteringEngine {

	private static final int CID_NOISE = -1;
	private static final int CID_UNCLASSIFIED = 0;
	private static final int CID_FIRST = 1;

	public static StrikeClusteringEngine newInstance(List<Strike> strikeList) {
		final StrikeBase base = StrikeBase.newInstance(strikeList);
		return new StrikeClusteringEngine(base);
	}

	private boolean expandCluster(Constraint cons, ClusterState clusterState, int strikeId, int clusterId) {
		assert cons != null;
		final Strike strike = m_base.strike(strikeId);
		final int[] seedStrikeIds = m_base.regionStrikeIds(cons, strikeId);
		final int seedIdCount = seedStrikeIds.length;
		final boolean noCorePoint = seedIdCount < cons.minStrikes;
		if (noCorePoint) {
			clusterState.setNoise(strikeId);
			return false;
		}
		clusterState.setClusterId(strikeId, clusterId);
		clusterState.setClusterId(seedStrikeIds, clusterId);
		for (int iseed = 0; iseed < seedIdCount; iseed++) {
			final int seedStrikeId = seedStrikeIds[iseed];
			final int[] resultStrikeIds = m_base.regionStrikeIds(cons, seedStrikeId);
			final int resultIdCount = resultStrikeIds.length;
			if (resultIdCount < cons.minStrikes) {
				continue;
			}
			for (int iresult = 0; iresult < resultIdCount; iresult++) {
				final int resultStrikeId = resultStrikeIds[iresult];
				final int resultClusterId = clusterState.clusterId(resultStrikeId);

			}
		}
		return true;
	}

	public void solve(float eps, int minStrikes) {
		final Constraint cons = new Constraint(eps, minStrikes);
		final ClusterState clusterState = ClusterState.newInstance(m_base);
		int clusterId = CID_FIRST;
		final int strikeCount = m_base.strikeCount();
		for (int strikeId = 0; strikeId < strikeCount; strikeId++) {
			final int strikeClusterId = clusterState.clusterId(strikeId);
			if (strikeClusterId == CID_UNCLASSIFIED) {
				if (expandCluster(cons, clusterState, strikeId, clusterId)) {
					clusterId++;
				}
			}
		}
	}

	private StrikeClusteringEngine(StrikeBase base) {
		if (base == null) throw new IllegalArgumentException("object is null");
		m_base = base;
	}
	private final StrikeBase m_base;

	private static class ClusterState {

		public static ClusterState newInstance(StrikeBase base) {
			assert base != null;
			final int[] cidArray = base.newClusterIdArray();
			return new ClusterState(base, cidArray);
		}

		public int clusterId(int strikeId) {
			return m_cidArray[strikeId];
		}

		public void setClusterId(int strikeId, int clusterId) {
			m_cidArray[strikeId] = clusterId;
		}

		public void setClusterId(int[] strikeIds, int clusterId) {
			final int idCount = strikeIds.length;
			for (int strikeId = 0; strikeId < idCount; strikeId++) {
				m_cidArray[strikeId] = clusterId;
			}
		}

		public void setNoise(int strikeId) {
			m_cidArray[strikeId] = CID_NOISE;
		}

		private ClusterState(StrikeBase base, int[] cidArray) {
			assert base != null;
			assert cidArray != null;
			m_base = base;
			m_cidArray = cidArray;
		}
		private final StrikeBase m_base;
		private final int[] m_cidArray;
	}

	private static class Constraint {

		public Constraint(float eps, int minStrikes) {
			this.eps = eps;
			this.minStrikes = minStrikes;
		}
		public final float eps;
		public final int minStrikes;
	}

	private static class StrikeBase {

		public static StrikeBase newInstance(List<Strike> strikeList) {
			if (strikeList == null) throw new IllegalArgumentException("object is null");
			final int strikeCount = strikeList.size();
			if (strikeCount == 0) throw new IllegalArgumentException("empty strike list");
			final Strike[] strikes = strikeList.toArray(new Strike[strikeCount]);
			final StrikeTree tree = StrikeTree.newInstance(strikes);
			return new StrikeBase(strikes, tree);
		}

		public int[] newClusterIdArray() {
			return new int[m_strikeCount];
		}

		public int[] regionStrikeIds(Constraint cons, int strikeId) {
			assert cons != null;
			return m_tree.query(m_strikes, strikeId, cons.eps);
		}

		public Strike strike(int idStrike) {
			return m_strikes[idStrike];
		}

		public int strikeCount() {
			return m_strikeCount;
		}

		public StrikeBase(Strike[] strikes, StrikeTree tree) {
			assert strikes != null;
			assert tree != null;
			m_strikes = strikes;
			m_tree = tree;
			m_strikeCount = strikes.length;
		}
		private final Strike[] m_strikes;
		private final StrikeTree m_tree;
		private final int m_strikeCount;
	}
}
