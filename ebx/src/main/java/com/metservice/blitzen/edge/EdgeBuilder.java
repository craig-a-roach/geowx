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

	private void fill(IEdge[] dst, List<IEdge> src, int srcFrom) {
		final int card = dst.length;
		for (int i = 0; i < card; i++) {
			dst[i] = src.get(srcFrom + i);
		}
	}

	private void merge(List<IEdge> dst, List<IEdge> src, int card) {
		final int srcCount = src.size();
		final int card2 = card * 2;
		if (srcCount < card2) {
			for (int i = 0; i < srcCount; i++) {
				dst.add(src.get(i));
			}
			return;
		}
		final IEdge[] steps = new IEdge[card];
		fill(steps, src, 0);
		int ilhs = 0;
		int irhs = 3;
		while (irhs < srcCount) {
			for (int i = 0; i < card; i++) {
				steps[i] = src.get(i);
			}

			ilhs++;
			irhs++;
		}
		while (ilhs < srcCount) {
			dst.add(src.get(ilhs));
			ilhs++;
		}
	}

	public void add(Bearing head) {
		if (head == null) throw new IllegalArgumentException("object is null");
		if (m_headEdge.bearing == head) {
			m_headEdge.increment();
		} else {
			m_headEdge = new Edge1(head);
			m_edges.add(m_headEdge);
		}
	}

	public List<Vertex> newVertices() {
		final int segmentCount = m_edges.size();
		final List<IEdge> src = m_edges;
		final List<IEdge> dst = new ArrayList<IEdge>(segmentCount);
		merge(dst, src, 2);
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
		for (final IEdge edge : m_edges) {
			sb.append(edge);
			sb.append("|");
		}
		return sb.toString();
	}

	public EdgeBuilder(Vertex start, Bearing head) {
		if (start == null) throw new IllegalArgumentException("object is null");
		if (head == null) throw new IllegalArgumentException("object is null");
		m_start = start;
		m_headEdge = new Edge1(head);
		m_edges.add(m_headEdge);
	}
	private final Vertex m_start;
	private Edge1 m_headEdge;
	private final List<IEdge> m_edges = new ArrayList<IEdge>();

	private static class Edge1 implements IEdge {

		@Override
		public boolean canMerge(IEdge r) {
			if (r instanceof Edge1) {
				final Edge1 rs = (Edge1) r;
				return bearing == rs.bearing && m_count == rs.m_count;
			}
			if (r instanceof EdgeN) {
				final EdgeN re = (EdgeN) r;
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

		public Edge1(Bearing bearing) {
			this.bearing = bearing;
			m_count = 1;
		}
		public final Bearing bearing;
		private int m_count;
	}

	private static class EdgeN implements IEdge {

		@Override
		public boolean canMerge(IEdge r) {
			// TODO Auto-generated method stub
			return false;
		}

		public void increment() {
			m_count++;
		}

		public EdgeN(IEdge[] steps) {
			m_steps = steps;
		}
		private final IEdge[] m_steps;
		private int m_count;
	}

	private static interface IEdge {

		public boolean canMerge(IEdge r);
	}
}
