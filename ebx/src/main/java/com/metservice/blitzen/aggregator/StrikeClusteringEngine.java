/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.awt.geom.Rectangle2D;
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
		final StrikeAgenda seeds = new StrikeAgenda();
		m_base.regionQuery(cons, strikeId, seeds);
		if (seeds.count() < cons.minStrikes) {
			clusterState.setNoise(strikeId);
			return false;
		}
		clusterState.setClusterId(strikeId, clusterId);
		clusterState.setClusterId(seeds, clusterId);
		while (!seeds.isEmpty()) {
			final int seedStrikeId = seeds.pop();
			final StrikeAgenda result = new StrikeAgenda();
			m_base.regionQuery(cons, seedStrikeId, result);
			if (result.count() < cons.minStrikes) {
				continue;
			}
			while (!result.isEmpty()) {
				final int resultStrikeId = result.pop();
				final int resultClusterId = clusterState.clusterId(resultStrikeId);
				if (resultClusterId < CID_FIRST) {
					if (resultClusterId == CID_UNCLASSIFIED) {
						seeds.add(resultStrikeId);
					}
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
		final StrikeClusterTable table = clusterState.newTable(lastClusterId, eps);
		return table;
	}

	private StrikeClusteringEngine(StrikeBase base) {
		assert base != null;
		m_base = base;
	}

	private final StrikeBase m_base;

	private static class ArrayBuilder {

		public void add(Strike strike) {
			assert strike != null;
			m_strikes[m_nextIndex] = strike;
			m_nextIndex++;
			final float absQty = Math.abs(strike.qty);
			m_qtyMagnitudeSum += absQty;
			m_qtyMagnitudeMax = Math.max(m_qtyMagnitudeMax, absQty);
		}

		public StrikePolygon newPolygon(float eps) {
			final int depth = m_strikes.length;
			if (depth != m_nextIndex)
				throw new IllegalStateException("expecting " + depth + " in cluster, but " + m_nextIndex);
			return StrikePolygon.newPolygon(m_strikes, eps);
		}

		public float qtyMagnitudeMax() {
			return m_qtyMagnitudeMax;
		}

		public float qtyMagnitudeSum() {
			return m_qtyMagnitudeSum;
		}

		public int strikeCount() {
			return m_strikes.length;
		}

		public Strike[] strikes() {
			return m_strikes;
		}

		public ArrayBuilder(int depth) {
			m_strikes = new Strike[depth];
		}
		private final Strike[] m_strikes;
		private int m_nextIndex;
		private float m_qtyMagnitudeSum;
		private float m_qtyMagnitudeMax;
	}

	private static class ClusterState {

		public static ClusterState newInstance(StrikeBase base) {
			assert base != null;
			final int[] cidArray = base.newClusterIdArray();
			return new ClusterState(base, cidArray);
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

		public StrikeClusterTable newTable(int lastClusterId, float eps) {
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
			float sumMag = 0.0f;
			for (int clusterId = 1; clusterId <= lastClusterId; clusterId++) {
				final ArrayBuilder ab = builderArray[clusterId];
				final StrikePolygon polygon = ab.newPolygon(eps);
				final int clusterCount = ab.strikeCount();
				final float clusterMagSum = ab.qtyMagnitudeSum();
				final float clusterMagMax = ab.qtyMagnitudeMax();
				final StrikeCluster cluster = new StrikeCluster(polygon, clusterCount, clusterMagSum, clusterMagMax);
				clusterArray[clusterId - 1] = cluster;
				sumMag += clusterMagSum;
			}
			final Strike[] noiseArray = noiseBuilder.strikes();
			final float noiseSumMag = noiseBuilder.qtyMagnitudeSum();
			final Rectangle2D.Float rect = m_base.boundingRectangle();
			return new StrikeClusterTable(clusterArray, noiseArray, strikeCount, sumMag, noiseSumMag, rect);
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

		public Rectangle2D.Float boundingRectangle() {
			return m_tree.boundingRectangle();
		}

		public int[] newClusterIdArray() {
			return new int[m_strikeCount];
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
