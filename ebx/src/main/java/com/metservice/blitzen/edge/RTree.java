/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
class RTree {

	public static final int DefaultNodeCapacity = 64;

	public static RTree newInstance(BzeStrike[] strikes) {
		return newInstance(strikes, DefaultNodeCapacity);
	}

	public static RTree newInstance(BzeStrike[] strikes, int nodeCapacity) {
		if (strikes == null) throw new IllegalArgumentException("object is null");
		if (strikes.length == 0) throw new IllegalArgumentException("strikeList is empty");
		final BzeStrikeBounds bounds = BzeStrikeBounds.newInstance(strikes);
		final RTree tree = new RTree(bounds);
		final int strikeCount = strikes.length;
		for (int sid = 0; sid < strikeCount; sid++) {
			final BzeStrike strike = strikes[sid];
			tree.insert(sid, strike, nodeCapacity);
		}
		return tree;
	}

	boolean boundsContains(BzeStrike strike) {
		assert strike != null;
		return m_bounds.contains(strike.y, strike.x);
	}

	void insert(int sid, BzeStrike strike, int nodeCapacity) {
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

	void query(QResult result) {
		final BzeStrike[] strikes = result.strikes;
		final Ring rangeRing = result.rangeRing;
		final int originId = result.originId;
		if (m_memberCount == 0 && m_oSubTree == null) return;
		if (!m_bounds.intersects(rangeRing.box)) return;
		for (int i = 0; i < m_memberCount; i++) {
			final int sid = m_oMemberIds[i];
			if (sid == originId) {
				continue;
			}
			final BzeStrike strike = strikes[sid];
			if (rangeRing.contains(strike.y, strike.x)) {
				result.agenda.add(sid);
			}
		}
		if (m_oSubTree != null) {
			m_oSubTree.query(result);
		}
	}

	public BzeStrikeBounds bounds() {
		return m_bounds;
	}

	public void query(BzeStrike[] strikes, int originStrikeId, float range, Agenda agenda) {
		if (strikes == null) throw new IllegalArgumentException("object is null");
		final BzeStrike originStrike = strikes[originStrikeId];
		final Ring rangeRing = new Ring(originStrike.y, originStrike.x, range);
		final QResult result = new QResult(strikes, originStrikeId, rangeRing, agenda);
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

	private RTree(BzeStrikeBounds b) {
		assert b != null;
		m_bounds = b;
	}
	private final BzeStrikeBounds m_bounds;
	private int[] m_oMemberIds;
	private int m_memberCount;
	private SubTree m_oSubTree;

	private static class QResult {

		public QResult(BzeStrike[] strikes, int originId, Ring rangeRing, Agenda agenda) {
			assert strikes != null;
			this.strikes = strikes;
			this.originId = originId;
			this.rangeRing = rangeRing;
			this.agenda = agenda;
		}
		public final BzeStrike[] strikes;
		public final int originId;
		public final Ring rangeRing;
		public final Agenda agenda;
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
			box = new BzeStrikeBounds(yC - r, xC - r, yC + r, xC + r);
		}
		public final float yC;
		public final float xC;
		public final float r;
		public final BzeStrikeBounds box;
	}

	private static class SubTree {

		void insert(int sid, BzeStrike strike, int nodeCapacity) {
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

		void query(QResult result) {
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

		public SubTree(BzeStrikeBounds parent) {
			assert parent != null;
			final float yM = parent.yM();
			final float xM = parent.xM();
			tl = new RTree(new BzeStrikeBounds(yM, parent.xL, parent.yT, xM));
			tr = new RTree(new BzeStrikeBounds(yM, xM, parent.yT, parent.xR));
			bl = new RTree(new BzeStrikeBounds(parent.yB, parent.xL, yM, xM));
			br = new RTree(new BzeStrikeBounds(parent.yB, xM, yM, parent.xR));
		}
		private final RTree tl;
		private final RTree tr;
		private final RTree bl;
		private final RTree br;
	}
}
