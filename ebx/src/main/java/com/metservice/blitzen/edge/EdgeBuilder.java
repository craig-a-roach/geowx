/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.util.ArrayList;
import java.util.List;

/**
 * @author roach
 */
class EdgeBuilder {

	private int advance(List<IEdge> dst, int startPos) {
		for (int stride = m_maxStride; stride >= 2; stride--) {
			final int advance = advanceByStride(dst, stride, startPos);
			if (advance > 0) return advance;
		}
		return 0;
	}

	private int advanceByStride(List<IEdge> dst, int stride, int startPos) {
		final int rampCount = m_ramps.size();
		final int stride2 = stride * 2;
		int rem = rampCount - startPos;
		if (rem < stride2) return 0;
		if (!isAdjacent(startPos, stride)) return 0;
		if (!matches(startPos, stride)) return 0;
		final Edge edge = newEdge(startPos, stride);
		dst.add(edge);
		int pos = startPos + stride;
		rem -= stride2;
		int advance = stride2;
		while (rem >= stride && matches(pos, stride)) {
			edge.increment();
			rem -= stride;
			advance += stride;
			pos += stride;
		}
		return advance;
	}

	private boolean isAdjacent(int startPos, int stride) {
		final int pairCount = stride - 1;
		for (int i = 0, ilhs = startPos, irhs = startPos + 1; i < pairCount; i++, ilhs++, irhs++) {
			final Bearing lhs = m_ramps.get(ilhs).bearing;
			final Bearing rhs = m_ramps.get(irhs).bearing;
			if (!lhs.isAdjacent(rhs, m_orthogonal)) return false;
		}
		return true;
	}

	private boolean matches(int startPos, int stride) {
		for (int i = 0, ilhs = startPos, irhs = startPos + stride; i < stride; i++, ilhs++, irhs++) {
			final Ramp lhs = m_ramps.get(ilhs);
			final Ramp rhs = m_ramps.get(irhs);
			if (!lhs.matches(rhs)) return false;
		}
		return true;
	}

	private Edge newEdge(int startPos, int stride) {
		int dx = 0;
		int dy = 0;
		for (int i = 0, pos = startPos; i < stride; i++, pos++) {
			final Ramp ramp = m_ramps.get(pos);
			dx += ramp.dx();
			dy += ramp.dy();
		}
		return new Edge(dx, dy);
	}

	public void add(Bearing head) {
		if (head == null) throw new IllegalArgumentException("object is null");
		if (m_headRamp.bearing == head) {
			m_headRamp.increment();
		} else {
			m_headRamp = new Ramp(head);
			m_ramps.add(m_headRamp);
		}
	}

	public List<Vertex> newVertices() {
		final int rampCount = m_ramps.size();
		final List<IEdge> dst = new ArrayList<IEdge>(rampCount);
		int pos = 0;
		while (pos < rampCount) {
			final int advance = advance(dst, pos);
			if (advance > 0) {
				pos += advance;
			} else {
				dst.add(m_ramps.get(pos));
				pos++;
			}
		}
		return newVertices(dst);
	}

	public List<Vertex> newVertices(List<IEdge> edges) {
		final int edgeCount = edges.size();
		final List<Vertex> vertices = new ArrayList<Vertex>(edgeCount + 1);
		vertices.add(m_start);
		Vertex head = m_start;
		for (int i = 0; i < edgeCount; i++) {
			final IEdge edge = edges.get(i);
			head = new Vertex(head.x + edge.dx(), head.y + edge.dy());
			vertices.add(head);
		}
		return vertices;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_start);
		sb.append(":");
		for (final Ramp ramp : m_ramps) {
			sb.append(ramp);
			sb.append("|");
		}
		return sb.toString();
	}

	public EdgeBuilder(Vertex start, Bearing head) {
		this(start, head, 3, true);
	}

	public EdgeBuilder(Vertex start, Bearing head, int maxStride, boolean orthogonal) {
		if (start == null) throw new IllegalArgumentException("object is null");
		if (head == null) throw new IllegalArgumentException("object is null");
		m_start = start;
		m_headRamp = new Ramp(head);
		m_ramps.add(m_headRamp);
		m_maxStride = maxStride;
		m_orthogonal = orthogonal;
	}
	private final Vertex m_start;
	private Ramp m_headRamp;
	private final List<Ramp> m_ramps = new ArrayList<Ramp>();
	private final int m_maxStride;
	private final boolean m_orthogonal;

	private static class Edge implements IEdge {

		@Override
		public int dx() {
			return dx * m_count;
		}

		@Override
		public int dy() {
			return dy * m_count;
		}

		public void increment() {
			m_count++;
		}

		@Override
		public String toString() {
			return "[" + dx + "," + dy + "]*" + m_count;
		}

		public Edge(int dx, int dy) {
			this.dx = dx;
			this.dy = dy;
			m_count = 2;
		}
		public final int dx;
		public final int dy;
		private int m_count;
	};

	private static interface IEdge {

		public int dx();

		public int dy();
	}

	private static class Ramp implements IEdge {

		@Override
		public int dx() {
			return bearing.dx * m_count;
		}

		@Override
		public int dy() {
			return bearing.dy * m_count;
		}

		public void increment() {
			m_count++;
		}

		public boolean matches(Ramp rhs) {
			return bearing == rhs.bearing && m_count == rhs.m_count;
		}

		@Override
		public String toString() {
			return bearing + "*" + m_count;
		}

		public Ramp(Bearing bearing) {
			this.bearing = bearing;
			m_count = 1;
		}
		public final Bearing bearing;
		private int m_count;
	}
}
