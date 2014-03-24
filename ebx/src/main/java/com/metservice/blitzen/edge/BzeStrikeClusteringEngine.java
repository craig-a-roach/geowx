/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.util.List;

/**
 * @author roach
 */
public class BzeStrikeClusteringEngine {

	private static final int CID_NOISE = -1;
	private static final int CID_UNCLASSIFIED = 0;
	private static final int CID_FIRST = 1;

	public static BzeStrikeClusteringEngine newInstance(List<BzeStrike> strikeList) {
		final StrikeBase base = StrikeBase.newInstance(strikeList);
		return new BzeStrikeClusteringEngine(base);
	}

	private boolean expandCluster(Constraint cons, ClusterState clusterState, int strikeId, int clusterId) {
		assert cons != null;
		final Agenda seeds = new Agenda();
		m_base.regionQuery(cons, strikeId, seeds);
		if (seeds.count() < cons.minStrikes) {
			clusterState.setNoise(strikeId);
			return false;
		}
		clusterState.setClusterId(strikeId, clusterId);
		clusterState.setClusterId(seeds, clusterId);
		while (!seeds.isEmpty()) {
			final int seedStrikeId = seeds.pop();
			final Agenda result = new Agenda();
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

	public BzeStrikeClusterTable solve(float eps, int minStrikes, float quality) {
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
		final BzeStrikeClusterTable table = clusterState.newTable(eps, quality, lastClusterId);
		return table;
	}

	private BzeStrikeClusteringEngine(StrikeBase base) {
		assert base != null;
		m_base = base;
	}

	private final StrikeBase m_base;

	private static class ArrayBuilder {

		public void add(BzeStrike strike) {
			assert strike != null;
			m_strikes[m_nextIndex] = strike;
			m_nextIndex++;
			final float absQty = Math.abs(strike.qty);
			m_qtyMagnitudeSum += absQty;
			m_qtyMagnitudeMax = Math.max(m_qtyMagnitudeMax, absQty);
		}

		public BzeStrikeCluster newCluster(int cid, float eps, float quality) {
			final int depth = m_strikes.length;
			if (depth != m_nextIndex) {
				final String msg = "Expecting " + depth + " in cluster # " + cid + ", but " + m_nextIndex;
				throw new IllegalStateException(msg);
			}
			final float grid = eps / Math.max(0.01f, quality);
			final BzeStrikeClusterShape clusterShape = BzeStrikeClusterShape.newInstance(m_strikes, grid);
			return new BzeStrikeCluster(cid, m_strikes, clusterShape, m_qtyMagnitudeMax, m_qtyMagnitudeMax);
		}

		public float qtyMagnitudeSum() {
			return m_qtyMagnitudeSum;
		}

		public BzeStrike[] strikes() {
			return m_strikes;
		}

		public ArrayBuilder(int depth) {
			m_strikes = new BzeStrike[depth];
		}
		private final BzeStrike[] m_strikes;
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

		public BzeStrikeClusterTable newTable(float eps, float quality, int lastClusterId) {
			final int[] extentArray = newExtentArray(lastClusterId);
			final int noiseCount = extentArray[0];
			final ArrayBuilder noiseBuilder = new ArrayBuilder(noiseCount);
			final ArrayBuilder[] builderArray = newBuilderArray(extentArray, lastClusterId);
			final int strikeCount = m_cidArray.length;
			for (int strikeId = 0; strikeId < strikeCount; strikeId++) {
				final BzeStrike strike = m_base.strike(strikeId);
				final int clusterId = m_cidArray[strikeId];
				final boolean isNoise = clusterId < CID_FIRST;
				final ArrayBuilder builder = isNoise ? noiseBuilder : builderArray[clusterId];
				builder.add(strike);
			}
			final BzeStrikeCluster[] clusterArray = new BzeStrikeCluster[lastClusterId];
			float sumMag = 0.0f;
			for (int clusterId = 0; clusterId < lastClusterId; clusterId++) {
				final ArrayBuilder ab = builderArray[clusterId + 1];
				final BzeStrikeCluster cluster = ab.newCluster(clusterId, eps, quality);
				clusterArray[clusterId] = cluster;
				sumMag += cluster.qtyMagnitudeSum();
			}
			final BzeStrike[] noiseArray = noiseBuilder.strikes();
			final float noiseSumMag = noiseBuilder.qtyMagnitudeSum();
			final BzeStrikeBounds bounds = m_base.bounds();
			return new BzeStrikeClusterTable(clusterArray, noiseArray, strikeCount, sumMag, noiseSumMag, bounds);
		}

		public void setClusterId(Agenda agenda, int clusterId) {
			assert agenda != null;
			final int count = agenda.count();
			for (int i = 0; i < count; i++) {
				m_cidArray[agenda.id(i)] = clusterId;
			}
		}

		public void setClusterId(int strikeId, int clusterId) {
			m_cidArray[strikeId] = clusterId;
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

		public static StrikeBase newInstance(List<BzeStrike> strikeList) {
			if (strikeList == null) throw new IllegalArgumentException("object is null");
			final int strikeCount = strikeList.size();
			if (strikeCount == 0) throw new IllegalArgumentException("empty strike list");
			final BzeStrike[] strikes = strikeList.toArray(new BzeStrike[strikeCount]);
			final RTree tree = RTree.newInstance(strikes);
			return new StrikeBase(strikes, tree);
		}

		public BzeStrikeBounds bounds() {
			return m_tree.bounds();
		}

		public int[] newClusterIdArray() {
			return new int[m_strikeCount];
		}

		public void regionQuery(Constraint cons, int strikeId, Agenda agenda) {
			m_tree.query(m_strikes, strikeId, cons.eps, agenda);
		}

		public BzeStrike strike(int strikeId) {
			return m_strikes[strikeId];
		}

		public int strikeCount() {
			return m_strikeCount;
		}

		public StrikeBase(BzeStrike[] strikes, RTree tree) {
			assert strikes != null;
			assert tree != null;
			m_strikes = strikes;
			m_tree = tree;
			m_strikeCount = strikes.length;
		}
		private final BzeStrike[] m_strikes;
		private final RTree m_tree;
		private final int m_strikeCount;
	}
}
