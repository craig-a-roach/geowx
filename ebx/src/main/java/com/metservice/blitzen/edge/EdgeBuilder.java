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

	private void fill(BitMesh dst, BitMesh image, int x, int y, Bearing bpre, Bearing b) {
		if (bpre == Bearing.W || b == Bearing.E) return;
		final Bearing brpre = bpre.orthogonalRight();
		final Bearing br = b.orthogonalRight();
		if (brpre.dx == 1 || br.dx == 1) {
			final int delta = brpre.deltaCW(br);
			if (delta > 2) {
				fillRight(dst, image, x + 1, y);
			}
		}
	}

	private void fillRight(BitMesh dst, BitMesh image, int xEdge, int yEdge) {
		assert dst != null;
		assert image != null;
		final int width = image.width();
		int x = xEdge;
		final int ydst = m_yB + yEdge;
		while (!image.value(x, yEdge) && x < width) {
			final int xdst = m_xL + x;
			dst.set(xdst, ydst, false);
			x++;
		}
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

	private void moveHead(Bearing head) {
		m_headX += head.dx;
		m_headY += head.dy;
		m_xL = Math.min(m_xL, m_headX);
		m_xR = Math.max(m_xR, m_headX);
		m_yB = Math.min(m_yB, m_headY);
		m_yT = Math.max(m_yT, m_headY);
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

	private BitMesh newRampImage() {
		final int width = m_xR - m_xL + 1;
		final int height = m_yT - m_yB + 1;
		return new BitMesh(width, height);
	}

	private List<Vertex> newVertices(List<IEdge> edges) {
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

	private void outline(BitMesh image) {
		int x = xInit();
		int y = yInit();
		image.set(x, y, true);
		final int rampCount = m_ramps.size();
		for (int r = 0; r < rampCount; r++) {
			final Ramp ramp = m_ramps.get(r);
			final Bearing b = ramp.bearing;
			final int c = ramp.count();
			for (int i = 0; i < c; i++) {
				x += b.dx;
				y += b.dy;
				image.set(x, y, true);
			}
		}
	}

	private int xInit() {
		return m_start.x - m_xL;
	}

	private int yInit() {
		return m_start.y - m_yB;
	}

	public void add(Bearing head) {
		if (head == null) throw new IllegalArgumentException("object is null");
		if (m_headRamp.bearing == head) {
			m_headRamp.increment();
		} else {
			m_headRamp = new Ramp(head);
			m_ramps.add(m_headRamp);
		}
		moveHead(head);
	}

	public void fillPolygon(BitMesh dst) {
		if (dst == null) throw new IllegalArgumentException("object is null");
		final BitMesh image = newRampImage();
		outline(image);
		int x = xInit();
		int y = yInit();
		final int rampCount = m_ramps.size();
		Ramp rampPre = m_ramps.get(rampCount - 1);
		for (int r = 0; r < rampCount; r++) {
			final Ramp ramp = m_ramps.get(r);
			final Bearing b = ramp.bearing;
			final int c = ramp.count();
			for (int i = 0; i < c; i++) {
				final Bearing bpre = i == 0 ? rampPre.bearing : b;
				fill(dst, image, x, y, bpre, b);
				x += b.dx;
				y += b.dy;
			}
			rampPre = ramp;
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
		m_headX = start.x;
		m_headY = start.y;
		m_xL = start.x;
		m_yB = start.y;
		m_xR = start.x;
		m_yT = start.y;
		moveHead(head);
	}

	private final Vertex m_start;
	private Ramp m_headRamp;
	private final List<Ramp> m_ramps = new ArrayList<Ramp>();
	private final int m_maxStride;
	private final boolean m_orthogonal;
	private int m_headX;
	private int m_headY;
	private int m_xL;
	private int m_yB;
	private int m_xR;
	private int m_yT;

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
	}

	private static interface IEdge {

		public int dx();

		public int dy();
	}

	private static class Ramp implements IEdge {

		public int count() {
			return m_count;
		}

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
