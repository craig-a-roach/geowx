/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author roach
 */
class StrikePolygon {

	private static final double TWOPI = 2.0 * Math.PI;
	private static final float FTWOPI = (float) TWOPI;
	private static final int MinHullVertices = 10;

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
		if (n < MinHullVertices) return strikesAscX;
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

	private static float pathAngle(Strike[] strikes, int[] idtriple) {
		final int id0 = idtriple[0];
		final int id1 = idtriple[1];
		final int id2 = idtriple[2];
		if (id0 == id2) return FTWOPI;
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

	private static int selectLeftmost(Strike[] strikes, StrikeAgenda agenda) {
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

	private static void selectPath(Strike[] strikes, StrikeTree tree, float eps, int[] idtriple) {
		final int vertexId = idtriple[1];
		final StrikeAgenda agenda = new StrikeAgenda();
		tree.query(strikes, vertexId, eps, agenda);
		final int agendaCount = agenda.count();
		idtriple[2] = agenda.id(0);
		int resultId = idtriple[2];
		float minAngle = pathAngle(strikes, idtriple);
		for (int i = 1; i < agendaCount; i++) {
			idtriple[2] = agenda.id(i);
			final float pa = pathAngle(strikes, idtriple);
			if (pa < minAngle) {
				resultId = idtriple[2];
				minAngle = pa;
			}
		}
		idtriple[2] = resultId;
	}

	public static StrikePolygon newPolygon(Strike[] strikes, float eps) {
		final int[] idtriple = new int[3];
		final StrikeTree tree = StrikeTree.newInstance(strikes);
		final int sentinelId = selectLeftmost(strikes);
		final int vertexId = sentinelId;
		final StrikeAgenda peers = new StrikeAgenda();
		tree.query(strikes, vertexId, eps, peers);
		final int leftmostPeerId = selectLeftmost(strikes, peers);
		final int strikeCount = strikes.length;
		final StrikeAgenda vertices = new StrikeAgenda(strikeCount / 8);
		vertices.add(vertexId);
		vertices.add(leftmostPeerId);
		idtriple[0] = vertexId;
		idtriple[1] = leftmostPeerId;
		selectPath(strikes, tree, eps, idtriple);
		final int visitLimit = strikeCount * 2;
		int visitCount = 0;
		while (idtriple[2] != sentinelId && visitCount < visitLimit) {
			vertices.add(idtriple[2]);
			idtriple[0] = idtriple[1];
			idtriple[1] = idtriple[2];
			selectPath(strikes, tree, eps, idtriple);
			visitCount++;
		}
		final boolean isClosed = (idtriple[2] == sentinelId);
		if (!isClosed) throw new IllegalStateException("Circularity in hull construction");
		return new StrikePolygon(strikes, vertices);
	}

	public Rectangle2D.Float bounds() {
		return m_bounds;
	}

	public Strike[] concaveVertices() {
		return m_vertices;
	}

	public Strike[] convexVertices() {
		final Strike[] ascX = new Strike[m_vertices.length];
		System.arraycopy(m_vertices, 0, ascX, 0, m_vertices.length);
		Arrays.sort(ascX, XComparator);
		final Strike[] convexHull = convexHull(ascX);
		return convexHull;
	}

	public float maxDimension() {
		return Math.max(m_bounds.height, m_bounds.width);
	}

	@Override
	public String toString() {
		return "#" + m_vertices.length + " " + m_bounds;
	}

	public int vertexCount() {
		return m_vertices.length;
	}

	private StrikePolygon(Strike[] strikes, StrikeAgenda vertices) {
		assert vertices != null;
		assert strikes != null;
		assert vertices != null;
		final int vcount = vertices.count();
		final Strike s0 = strikes[0];
		float yB = s0.y, xL = s0.x;
		float yT = yB, xR = xL;
		final Strike[] array = new Strike[vcount];
		for (int i = 0; i < vcount; i++) {
			final int vid = vertices.id(i);
			final Strike strike = strikes[vid];
			final float sy = strike.y;
			final float sx = strike.x;
			if (sy < yB) {
				yB = sy;
			}
			if (sx < xL) {
				xL = sx;
			}
			if (sy > yT) {
				yT = sy;
			}
			if (sx > xR) {
				xR = sx;
			}
			array[i] = strike;
		}
		final float width = (xR - xL);
		final float height = (yT - yB);
		m_vertices = array;
		m_bounds = new Rectangle2D.Float(xL, yT, width, height);
	}
	private final Strike[] m_vertices;
	private final Rectangle2D.Float m_bounds;
}