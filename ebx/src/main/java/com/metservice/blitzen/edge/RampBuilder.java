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

	private void drainRef() {
		for (int i = 0; i < m_refCount; i++) {
			m_vertices.add(m_vbuffer[i]);
			m_vbuffer[i] = null;
		}
		m_refCount = 0;
	}

	public void add(Bearing bearing, Vertex vertex) {
		final int depth = m_ref.length;
		if (m_refCount < depth) {
			m_ref[m_refCount] = bearing;
			m_vbuffer[m_refCount] = vertex;
			m_refCount++;
			return;
		}
		if (m_matchIndex == depth) {
			drainRef();
			m_matchIndex = 0;
		}

	}

	public void add(Vertex vertex) {
		m_vertices.add(vertex);
	}

	public RampBuilder(int depth) {
		m_ref = new Bearing[depth];
		m_vbuffer = new Vertex[depth];
		m_vertices = new ArrayList<>();
	}
	private final Bearing[] m_ref;
	private final Vertex[] m_vbuffer;
	private int m_refCount;
	private int m_matchIndex;
	private final List<Vertex> m_vertices;
}
