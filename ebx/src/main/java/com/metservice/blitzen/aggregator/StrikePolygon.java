/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author roach
 */
class StrikePolygon {

	private static final double TWOPI = 2.0 * Math.PI;
	private static final float FTWOPI = (float) TWOPI;

	private static final Comparator<Strike> XComparator = new Comparator<Strike>() {

		@Override
		public int compare(Strike lhs, Strike rhs) {
			if (lhs.x < rhs.x) return -1;
			if (lhs.x > rhs.x) return +1;
			return 0;
		}
	};

	private static Strike[] convexHull(Strike[] strikesAscX) {
		final int n = strikesAscX.length;
		final Strike[] upper = new Strike[n];
		upper[0] = strikesAscX[0];
		upper[1] = strikesAscX[1];
		int upperSize = 2;
		for (int i = 2; i < n; i++) {
			upper[upperSize] = strikesAscX[i];
			upperSize++;
			while (upperSize > 2 && !rightTurn(upper[upperSize - 3], upper[upperSize - 2], upper[upperSize - 1])) {
				upper[upperSize - 2] = upper[upperSize - 1];
				upperSize--;
			}
		}

		final Strike[] lower = new Strike[n];
		lower[0] = strikesAscX[n - 1];
		lower[1] = strikesAscX[n - 2];
		int lowerSize = 2;
		for (int i = n - 3; i >= 0; i--) {
			lower[lowerSize] = strikesAscX[i];
			lowerSize++;
			while (lowerSize > 2 && !rightTurn(lower[lowerSize - 3], lower[lowerSize - 2], lower[lowerSize - 1])) {
				lower[lowerSize - 2] = lower[lowerSize - 1];
				lowerSize--;
			}
		}

		final Strike[] result = new Strike[upperSize + lowerSize];
		int ri = 0;
		for (int i = 0; i < upperSize; i++) {
			result[ri] = upper[i];
			ri++;
		}
		for (int i = 0; i < lowerSize; i++) {
			result[ri] = lower[i];
			ri++;
		}
		return result;
	}

	private static StrikePolygon createConcave(PolygonSpec ps, Strike[] cluster) {
		final int interiorCount = cluster.length;
		final Tracker tracker = new Tracker(cluster);
		final RTree tree = RTree.newInstance(cluster);
		final int sentinelId = selectLeftmost(cluster);
		final Agenda peers = new Agenda();
		tree.query(cluster, sentinelId, ps.eps, peers);
		final int leftmostPeerId = selectLeftmost(cluster, peers);
		final Agenda vertices = new Agenda(interiorCount / 8);
		vertices.add(sentinelId);
		vertices.add(leftmostPeerId);
		final int[] idpath = new int[2];
		idpath[0] = sentinelId;
		idpath[1] = leftmostPeerId;
		int headId = selectPath(cluster, tree, ps, idpath, tracker, 1);
		final int visitLimit = interiorCount * 2;
		int visitCount = 0;
		while (headId != sentinelId && headId >= 0 && visitCount < visitLimit) {
			vertices.add(headId);
			tracker.visited(headId);
			idpath[0] = idpath[1];
			idpath[1] = headId;
			headId = selectPath(cluster, tree, ps, idpath, tracker, 1);
			visitCount++;
		}
		final boolean isClosed = (headId == sentinelId);
		if (!isClosed) return null;
		return new StrikePolygon(cluster, newStrikeVertices(cluster, vertices));
	}

	private static StrikePolygon newConvex(Strike[] cluster) {
		final int interiorCount = cluster.length;
		final Strike[] ascX = new Strike[interiorCount];
		System.arraycopy(cluster, 0, ascX, 0, interiorCount);
		Arrays.sort(ascX, XComparator);
		final Strike[] convexHull = convexHull(ascX);
		return new StrikePolygon(cluster, convexHull);
	}

	private static Strike[] newStrikeVertices(Strike[] strikes, Agenda vertices) {
		final int vcount = vertices.count();
		final Strike[] array = new Strike[vcount];
		for (int i = 0; i < vcount; i++) {
			array[i] = strikes[vertices.id(i)];
		}
		return array;
	}

	private static float pathAngle(Strike[] strikes, int[] idpath, int headId) {
		final int id0 = idpath[0];
		if (id0 == headId) return FTWOPI;
		final int id1 = idpath[1];
		final int id2 = headId;
		final Strike sa = strikes[id0];
		final Strike sb = strikes[id1];
		final Strike sc = strikes[id2];
		final float ABy = sb.y - sa.y;
		final float ABx = sb.x - sa.x;
		final float BCy = sc.y - sb.y;
		final float BCx = sc.x - sb.x;
		final double AB = Math.atan2(ABy, ABx);
		final double BC = Math.atan2(BCy, BCx);
		final double ABC = AB - BC + Math.PI;
		final double result = ABC > 0.0 ? (ABC > TWOPI ? (ABC - TWOPI) : ABC) : (TWOPI + ABC);
		return (float) result;
	}

	private static void reverse(int[] idpath) {
		final int tid = idpath[0];
		idpath[0] = idpath[1];
		idpath[1] = tid;
	}

	private static boolean rightTurn(Strike a, Strike b, Strike c) {
		return ((b.x - a.x) * (c.y - a.y)) - ((b.y - a.y) * (c.x - a.x)) > 0;
	}

	private static int selectLeftmost(Strike[] strikes) {
		final int strikeCount = strikes.length;
		int resultId = 0;
		float xmin = strikes[resultId].x;
		float yxmin = Float.NaN;
		for (int id = 1; id < strikeCount; id++) {
			final float sx = strikes[id].x;
			if (sx <= xmin) {
				xmin = sx;
				final float sy = strikes[id].y;
				if (Float.isNaN(yxmin) || sy < yxmin) {
					resultId = id;
					yxmin = sy;
				}
			}
		}
		return resultId;
	}

	private static int selectLeftmost(Strike[] strikes, Agenda agenda) {
		assert agenda != null;
		final int agendaCount = agenda.count();
		int resultId = agenda.id(0);
		float xmin = strikes[resultId].x;
		for (int i = 1; i < agendaCount; i++) {
			final int id = agenda.id(i);
			final float sx = strikes[id].x;
			if (sx < xmin) {
				resultId = id;
				xmin = sx;
			}
		}
		return resultId;
	}

	private static int selectPath(Strike[] strikes, RTree tree, PolygonSpec ps, int[] idpath, Tracker tracker, int inflator) {
		if (inflator > ps.inflateLimit) return -1;
		final float eps = ps.eps * inflator;
		final int pathId = idpath[0];
		final int vertexId = idpath[1];
		final Agenda agenda = new Agenda();
		tree.query(strikes, vertexId, eps, agenda, pathId, tracker);
		final int agendaCount = agenda.count();
		if (agendaCount == 0) {
			if (inflator == 1) {
				reverse(idpath);
			}
			return selectPath(strikes, tree, ps, idpath, tracker, inflator + 1);
		}
		int headId = agenda.id(0);
		if (agendaCount == 1) return headId;
		int resultId = headId;
		float minAngle = pathAngle(strikes, idpath, headId);
		for (int i = 1; i < agendaCount; i++) {
			headId = agenda.id(i);
			final float pa = pathAngle(strikes, idpath, headId);
			if (pa < minAngle) {
				resultId = headId;
				minAngle = pa;
			}
		}
		return resultId;
	}

	public static StrikePolygon newPolygon(PolygonSpec ps, Strike[] cluster, int cid) {
		if (ps == null) throw new IllegalArgumentException("object is null");
		if (cluster == null) throw new IllegalArgumentException("object is null");
		final int interiorCount = cluster.length;
		if (interiorCount == 0) throw new IllegalArgumentException("non-empty interior strikes");
		StrikePolygon oPolygon = null;
		if (cid == 125) {
			System.out.println("Big");
		}
		if (interiorCount >= ps.minConcave) {
			System.out.println("**" + cid + ".");
			oPolygon = createConcave(ps, cluster);
		}
		if (oPolygon == null) {
			oPolygon = newConvex(cluster);
		}
		if (oPolygon == null) throw new IllegalStateException("Could not construct polygon for cluster #" + cid);
		return oPolygon;

	}

	public StrikeBounds bounds() {
		return m_bounds;
	}

	public Strike[] cluster() {
		return m_cluster;
	}

	public int interiorCount() {
		return m_cluster.length;
	}

	@Override
	public String toString() {
		return "vertices=" + vertexCount() + ", interior=" + interiorCount() + " " + m_bounds;
	}

	public int vertexCount() {
		return m_vertices.length;
	}

	public Strike[] vertices() {
		return m_vertices;
	}

	private StrikePolygon(Strike[] cluster, Strike[] vertices) {
		assert cluster != null && cluster.length > 0;
		assert vertices != null && vertices.length > 0;
		m_cluster = cluster;
		m_vertices = vertices;
		m_bounds = StrikeBounds.newInstance(vertices);
	}
	private final Strike[] m_cluster;
	private final Strike[] m_vertices;
	private final StrikeBounds m_bounds;
}