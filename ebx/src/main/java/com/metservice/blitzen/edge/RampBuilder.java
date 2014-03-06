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
class RampBuilder {

	public void add(Bearing head) {
		if (head == null) throw new IllegalArgumentException("object is null");
		if (m_headSegment.bearing == head) {
			m_headSegment.increment();
		} else {
			m_headSegment = new Segment(head);
			m_segments.add(m_headSegment);
		}
	}

	public void newVertices() {
		final int segmentCount = m_segments.size();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(m_start);
		sb.append(":");
		for (final Segment segment : m_segments) {
			sb.append(segment);
			sb.append("|");
		}
		return sb.toString();
	}

	public RampBuilder(Vertex start, Bearing head) {
		if (start == null) throw new IllegalArgumentException("object is null");
		if (head == null) throw new IllegalArgumentException("object is null");
		m_start = start;
		m_headSegment = new Segment(head);
		m_segments.add(m_headSegment);
	}
	private final Vertex m_start;
	private Segment m_headSegment;
	private final List<Segment> m_segments = new ArrayList<Segment>();

	private static class Segment {

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
