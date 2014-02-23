/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author roach
 */
class StrikeClusteringEngineRC {

	private static final int CID_NOISE = -1;
	private static final int CID_UNCLASSIFIED = 0;
	private static final int CID_FIRST = 1;

	private static final Comparator<Strike> XComparator = new Comparator<Strike>() {

		@Override
		public int compare(Strike lhs, Strike rhs) {
			if (lhs.x < rhs.x) return -1;
			if (lhs.x > rhs.x) return +1;
			return 0;
		}
	};

	public static StrikeClusteringEngineRC newInstance(List<Strike> strikeList) {
		final StrikeBase base = StrikeBase.newInstance(strikeList);
		return new StrikeClusteringEngineRC(base);
	}

	private boolean expandCluster(Constraint cons, ClusterState clusterState, int strikeId, int clusterId) {
		assert cons != null;
		final StrikeAgenda seeds = new StrikeAgenda();
		m_base.regionQuery(cons, strikeId, seeds);
		final int seedCount = seeds.count();
		clusterState.setRegion(strikeId, seedCount);
		if (seedCount < cons.minStrikes) {
			clusterState.setNoise(strikeId);
			return false;
		}
		clusterState.setClusterId(strikeId, clusterId);
		clusterState.setClusterId(seeds, clusterId);
		while (!seeds.isEmpty()) {
			final int seedStrikeId = seeds.pop();
			final StrikeAgenda result = new StrikeAgenda();
			m_base.regionQuery(cons, seedStrikeId, result);
			final int resultCount = result.count();
			clusterState.setRegion(seedStrikeId, resultCount);
			if (resultCount < cons.minStrikes) {
				continue;
			}
			while (!result.isEmpty()) {
				final int resultStrikeId = result.pop();
				final int resultClusterId = clusterState.clusterId(resultStrikeId);
				if (resultClusterId == CID_NOISE) {
					clusterState.setClusterId(resultStrikeId, clusterId);
					continue;
				}
				if (resultClusterId == CID_UNCLASSIFIED) {
					seeds.add(resultStrikeId);
					clusterState.setClusterId(resultStrikeId, clusterId);
				}
			}
		}
		return true;
	}

	public StrikeClusterTable solve(float eps, int minStrikes) {
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
		final int lastClusterId = clusterId == CID_FIRST ? 0 : (clusterId - 1);
		final StrikeClusterTable table = clusterState.newTable(lastClusterId);
		return table;
	}

	private StrikeClusteringEngineRC(StrikeBase base) {
		assert base != null;
		m_base = base;
	}

	private final StrikeBase m_base;

	private static class ArrayBuilder {

		public void add(Strike strike) {
			assert strike != null;
			m_strikes[m_nextIndex] = strike;
			m_nextIndex++;
			m_qtyMagnitude += Math.abs(strike.qty);
		}

		public StrikeCluster newCluster() {
			final int depth = m_strikes.length;
			if (depth != m_nextIndex)
				throw new IllegalStateException("expecting " + depth + " in cluster, but " + m_nextIndex);
			Arrays.sort(m_strikes, XComparator);
			return new StrikeCluster(m_strikes, m_qtyMagnitude);
		}

		public float qtyMagnitude() {
			return m_qtyMagnitude;
		}

		public Strike[] strikes() {
			return m_strikes;
		}

		public ArrayBuilder(int depth) {
			m_strikes = new Strike[depth];
		}
		private final Strike[] m_strikes;
		private int m_nextIndex;
		private float m_qtyMagnitude;
	}

	private static class ClusterState {

		public static ClusterState newInstance(StrikeBase base) {
			assert base != null;
			final int strikeCount = base.strikeCount();
			final int[] cidArray = new int[strikeCount];
			final int[] rgnArray = new int[strikeCount];
			return new ClusterState(base, cidArray, rgnArray);
		}

		private ArrayBuilder[] newBuilderArray(int[] extentArray, int lastClusterId) {
			final ArrayBuilder[] builderArray = new ArrayBuilder[lastClusterId + 1];
			for (int clusterId = 1; clusterId <= lastClusterId; clusterId++) {
				final int extentDepth = extentArray[clusterId];
				builderArray[clusterId] = new ArrayBuilder(extentDepth);
			}
			return builderArray;
		}

		private int[] newExtentArray(int lastClusterId) {
			final int[] extentArray = new int[lastClusterId + 1];
			final int strikeCount = m_cidArray.length;
			int noiseCount = 0;
			for (int strikeId = 0; strikeId < strikeCount; strikeId++) {
				final int clusterId = m_cidArray[strikeId];
				if (clusterId >= CID_FIRST) {
					extentArray[clusterId] = extentArray[clusterId] + 1;
					continue;
				}
				if (clusterId == CID_NOISE) {
					noiseCount++;
					continue;
				}
				throw new IllegalStateException("strike " + strikeId + " is unclassified");
			}
			extentArray[0] = noiseCount;
			return extentArray;
		}

		public int clusterId(int strikeId) {
			return m_cidArray[strikeId];
		}

		public StrikeClusterTable newTable(int lastClusterId) {
			final int[] extentArray = newExtentArray(lastClusterId);
			final int noiseCount = extentArray[0];
			final ArrayBuilder noiseBuilder = new ArrayBuilder(noiseCount);
			final ArrayBuilder[] builderArray = newBuilderArray(extentArray, lastClusterId);
			final int strikeCount = m_cidArray.length;
			for (int strikeId = 0; strikeId < strikeCount; strikeId++) {
				final Strike strike = m_base.strike(strikeId);
				final int clusterId = m_cidArray[strikeId];
				final boolean isNoise = clusterId < CID_FIRST;
				final ArrayBuilder builder = isNoise ? noiseBuilder : builderArray[clusterId];
				builder.add(strike);
			}
			final StrikeCluster[] clusterArray = new StrikeCluster[lastClusterId];
			float sumClusterMag = 0.0f;
			for (int clusterId = 1; clusterId <= lastClusterId; clusterId++) {
				final StrikeCluster cluster = builderArray[clusterId].newCluster();
				clusterArray[clusterId - 1] = cluster;
				sumClusterMag += cluster.qtyMagnitude();
			}
			final Strike[] noiseArray = noiseBuilder.strikes();
			final float sumNoiseMag = noiseBuilder.qtyMagnitude();
			final Rectangle2D.Float rect = m_base.boundingRectangle();
			return new StrikeClusterTable(clusterArray, noiseArray, strikeCount, sumClusterMag, sumNoiseMag, rect);
		}

		public void setClusterId(int strikeId, int clusterId) {
			m_cidArray[strikeId] = clusterId;
		}

		public void setClusterId(StrikeAgenda agenda, int clusterId) {
			assert agenda != null;
			final int count = agenda.count();
			for (int i = 0; i < count; i++) {
				m_cidArray[agenda.id(i)] = clusterId;
			}
		}

		public void setNoise(int strikeId) {
			m_cidArray[strikeId] = CID_NOISE;
		}

		public void setRegion(int strikeId, int regionCount) {
			m_rgnArray[strikeId] = regionCount;
		}

		private ClusterState(StrikeBase base, int[] cidArray, int[] rgnArray) {
			assert base != null;
			assert cidArray != null;
			m_base = base;
			m_cidArray = cidArray;
			m_rgnArray = rgnArray;
		}
		private final StrikeBase m_base;
		private final int[] m_cidArray;
		private final int[] m_rgnArray;
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

		public Rectangle2D.Float boundingRectangle() {
			return m_tree.boundingRectangle();
		}

		public void regionQuery(Constraint cons, int strikeId, StrikeAgenda agenda) {
			assert cons != null;
			m_tree.query(m_strikes, strikeId, cons.eps, agenda);
		}

		public Strike strike(int strikeId) {
			return m_strikes[strikeId];
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
