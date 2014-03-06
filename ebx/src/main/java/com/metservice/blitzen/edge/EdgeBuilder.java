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

	private void merge(List<IEdge> dst, List<IEdge> src) {
		final int srcCount = src.size();
		if (srcCount == 0) return;
		if (srcCount == 1) {
			dst.add(src.get(0));
			return;
		}
		final IEdge lhs = src.get(0);
		final IEdge rhs = src.get(1);
		final IEdge lhsNeo = src.get(2);
		final IEdge rhsNeo = src.get(3);
		if (lhs.equals(lhsNeo) && rhs.equals(rhsNeo)) {

		}
	}

	public void add(Bearing head) {
		if (head == null) throw new IllegalArgumentException("object is null");
		if (m_headSegment.bearing == head) {
			m_headSegment.increment();
		} else {
			m_headSegment = new Segment(head);
			m_segments.add(m_headSegment);
		}
	}

	public List<Vertex> newVertices() {
		final int segmentCount = m_segments.size();
		final List<IEdge> src = m_segments;
		final List<IEdge> dst = new ArrayList<IEdge>(segmentCount);
		merge(dst, src);
		return newVertices(dst);
	}

	public List<Vertex> newVertices(List<IEdge> edges) {
		final int edgeCount = edges.size();
		final List<Vertex> vertices = new ArrayList<Vertex>(edgeCount + 1);
		return vertices;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_start);
		sb.append(":");
		for (final IEdge segment : m_segments) {
			sb.append(segment);
			sb.append("|");
		}
		return sb.toString();
	}

	public EdgeBuilder(Vertex start, Bearing head) {
		if (start == null) throw new IllegalArgumentException("object is null");
		if (head == null) throw new IllegalArgumentException("object is null");
		m_start = start;
		m_headSegment = new Segment(head);
		m_segments.add(m_headSegment);
	}
	private final Vertex m_start;
	private Segment m_headSegment;
	private final List<IEdge> m_segments = new ArrayList<IEdge>();

	private static class Edge implements IEdge {

		@Override
		public boolean canMerge(IEdge r) {
			// TODO Auto-generated method stub
			return false;
		}

		public void increment() {
			m_count++;
		}

		public Edge(IEdge lhs, IEdge rhs) {
			m_lhs = lhs;
			m_rhs = rhs;
		}
		private final IEdge m_lhs;
		private final IEdge m_rhs;
		private int m_count;
	}

	private static interface IEdge {

		public boolean canMerge(IEdge r);
	}

	private static class Segment implements IEdge {

		@Override
		public boolean canMerge(IEdge r) {
			if (r instanceof Segment) {
				final Segment rs = (Segment) r;
				return bearing == rs.bearing && m_count == rs.m_count;
			}
			if (r instanceof Edge) {
				final Edge re = (Edge) r;
			}
			return false;
		}

		public void increment() {
			m_count++;
		}

		@Override
		public String toString() {
			return bearing + "*" + m_count;
		}

		public Segment(Bearing bearing) {
			this.bearing = bearing;
			m_count = 1;
		}
		public final Bearing bearing;
		private int m_count;
	}
}
