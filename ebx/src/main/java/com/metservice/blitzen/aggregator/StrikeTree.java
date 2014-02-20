/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.awt.geom.Rectangle2D;

/**
 * @author roach
 */
class StrikeTree {

	public static final int DefaultNodeCapacity = 64;

	private static Bounds newBounds(Strike[] strikes) {
		assert strikes != null;
		final int strikeCount = strikes.length;
		assert strikeCount > 0;
		final Strike s0 = strikes[0];
		float yB = s0.y, xL = s0.x;
		float yT = yB, xR = xL;
		for (int i = 1; i < strikeCount; i++) {
			final Strike strike = strikes[i];
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
		}
		return new Bounds(yB, xL, yT, xR);
	}

	public static StrikeTree newInstance(Strike[] strikes) {
		return newInstance(strikes, DefaultNodeCapacity);
	}

	public static StrikeTree newInstance(Strike[] strikes, int nodeCapacity) {
		if (strikes == null) throw new IllegalArgumentException("object is null");
		if (strikes.length == 0) throw new IllegalArgumentException("strikeList is empty");
		final Bounds bounds = newBounds(strikes);
		final StrikeTree tree = new StrikeTree(bounds);
		final int strikeCount = strikes.length;
		for (int sid = 0; sid < strikeCount; sid++) {
			final Strike strike = strikes[sid];
			tree.insert(sid, strike, nodeCapacity);
		}
		return tree;
	}

	private boolean isCore(Strike[] strikes, int originStrikeId, Ring rangeRing) {
		// TODO Auto-generated method stub
		return false;
	}

	boolean boundsContains(Strike strike) {
		assert strike != null;
		return m_bounds.contains(strike.y, strike.x);
	}

	void insert(int sid, Strike strike, int nodeCapacity) {
		assert strike != null;
		if (m_memberCount < nodeCapacity) {
			if (m_oMemberIds == null) {
				m_oMemberIds = new int[nodeCapacity];
			}
			m_oMemberIds[m_memberCount] = sid;
			m_memberCount++;
		} else {
			if (m_oSubTree == null) {
				m_oSubTree = new SubTree(m_bounds);
			}
			m_oSubTree.insert(sid, strike, nodeCapacity);
		}
	}

	void query(Result result) {
		final Strike[] strikes = result.strikes;
		final Ring rangeRing = result.rangeRing;
		final int idOrigin = result.idOrigin;
		if (m_memberCount == 0 && m_oSubTree == null) return;
		if (!m_bounds.intersects(rangeRing.box)) return;

		for (int i = 0; i < m_memberCount; i++) {
			final int sid = m_oMemberIds[i];
			if (sid != idOrigin) {
				final Strike strike = strikes[sid];
				if (rangeRing.contains(strike.y, strike.x)) {
					result.agenda.add(sid);
				}
			}
		}
		if (m_oSubTree != null) {
			m_oSubTree.query(result);
		}
	}

	public Rectangle2D.Float boundingRectangle() {
		return m_bounds.boundingRectangle();
	}

	public boolean isCore(Strike[] strikes, int originStrikeId, float range) {
		if (strikes == null) throw new IllegalArgumentException("object is null");
		final Strike originStrike = strikes[originStrikeId];
		final Ring rangeRing = new Ring(originStrike.y, originStrike.x, range);
		return isCore(strikes, originStrikeId, rangeRing);
	}

	public void query(Strike[] strikes, int originStrikeId, float range, StrikeAgenda agenda) {
		if (strikes == null) throw new IllegalArgumentException("object is null");
		final Strike originStrike = strikes[originStrikeId];
		final Ring rangeRing = new Ring(originStrike.y, originStrike.x, range);
		final Result result = new Result(strikes, originStrikeId, rangeRing, agenda);
		query(result);
	}

	public int size() {
		final int subCount = m_oSubTree == null ? 0 : m_oSubTree.size();
		return m_memberCount + subCount;
	}

	@Override
	public String toString() {
		final boolean hasSub = m_oSubTree != null;
		return "bounds(" + m_bounds + "), members=" + m_memberCount + ", hasSubTree=" + hasSub;
	}

	private StrikeTree(Bounds b) {
		assert b != null;
		m_bounds = b;
	}
	private final Bounds m_bounds;
	private int[] m_oMemberIds;
	private int m_memberCount;
	private SubTree m_oSubTree;

	private static class Bounds {

		public Rectangle2D.Float boundingRectangle() {
			return new Rectangle2D.Float(xL, yT, (xR - xL), (yT - yB));
		}

		public boolean contains(float y, float x) {
			return y >= yB && y <= yT && x >= xL && x <= xR;
		}

		public boolean intersects(Bounds rhs) {
			return rhs.yB <= yT && rhs.yT >= yB && rhs.xL <= xR && rhs.xR >= xL;
		}

		@Override
		public String toString() {
			return "yB=" + yB + ", xL=" + xL + ", yT=" + yT + ", xR=" + xR;
		}

		public float xM() {
			return xL + ((xR - xL) / 2.0f);
		}

		public float yM() {
			return yB + ((yT - yB) / 2.0f);
		}

		Bounds(float yB, float xL, float yT, float xR) {
			this.yB = yB;
			this.xL = xL;
			this.yT = yT;
			this.xR = xR;
		}
		public final float yB;
		public final float xL;
		public final float yT;
		public final float xR;
	}

	private static class Result {

		public Result(Strike[] strikes, int idOrigin, Ring rangeRing, StrikeAgenda agenda) {
			assert strikes != null;
			this.strikes = strikes;
			this.idOrigin = idOrigin;
			this.rangeRing = rangeRing;
			this.agenda = agenda;
		}
		public final Strike[] strikes;
		public final int idOrigin;
		public final Ring rangeRing;
		public final StrikeAgenda agenda;
	}

	private static class Ring {

		public boolean contains(float y, float x) {
			if (!box.contains(y, x)) return false;
			final float dy = yC - y;
			final float dx = xC - x;
			final double rC = Math.sqrt((dy * dy) + (dx * dx));
			return rC <= r;
		}

		@Override
		public String toString() {
			return "cy=" + yC + ", cx=" + xC + ", r=" + r;
		}

		Ring(float yC, float xC, float r) {
			this.yC = yC;
			this.xC = xC;
			this.r = r;
			box = new Bounds(yC - r, xC - r, yC + r, xC + r);
		}
		public final float yC;
		public final float xC;
		public final float r;
		public final Bounds box;
	}

	private static class SubTree {

		void insert(int sid, Strike strike, int nodeCapacity) {
			if (tl.boundsContains(strike)) {
				tl.insert(sid, strike, nodeCapacity);
				return;
			}
			if (tr.boundsContains(strike)) {
				tr.insert(sid, strike, nodeCapacity);
				return;
			}
			if (bl.boundsContains(strike)) {
				bl.insert(sid, strike, nodeCapacity);
				return;
			}
			if (br.boundsContains(strike)) {
				br.insert(sid, strike, nodeCapacity);
				return;
			}
			throw new IllegalStateException("Cannot insert strike (" + strike + ") in subTree(" + toString() + ")");
		}

		void query(Result result) {
			tl.query(result);
			tr.query(result);
			bl.query(result);
			br.query(result);
		}

		public int size() {
			return tl.size() + tr.size() + bl.size() + br.size();
		}

		@Override
		public String toString() {
			return "topLeft(" + tl + ")\ntopRight(" + tr + ")\nbottomLeft(" + bl + ")\nbottomRight(" + br + ")";
		}

		public SubTree(Bounds parent) {
			assert parent != null;
			final float yM = parent.yM();
			final float xM = parent.xM();
			tl = new StrikeTree(new Bounds(yM, parent.xL, parent.yT, xM));
			tr = new StrikeTree(new Bounds(yM, xM, parent.yT, parent.xR));
			bl = new StrikeTree(new Bounds(parent.yB, parent.xL, yM, xM));
			br = new StrikeTree(new Bounds(parent.yB, xM, yM, parent.xR));
		}
		private final StrikeTree tl;
		private final StrikeTree tr;
		private final StrikeTree bl;
		private final StrikeTree br;
	}

}
