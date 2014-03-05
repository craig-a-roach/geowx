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

	private void addAdjacent(Bearing neo) {
		// TODO Auto-generated method stub

	}

	public void add(Bearing pivotHead) {
		if (pivotHead == null) throw new IllegalArgumentException("object is null");
		if (m_originPivot == pivotHead) return;
		if (m_originPivot.isAdjacent(pivotHead)) {
			addAdjacent(pivotHead);
		}

	}

	public RampBuilder(Vertex origin, Bearing originPivot) {
		if (origin == null) throw new IllegalArgumentException("object is null");
		if (originPivot == null) throw new IllegalArgumentException("object is null");
		m_origin = origin;
		m_originPivot = originPivot;
		m_pivotX = origin.x + originPivot.dx;
		m_pivotY = origin.y + originPivot.dy;
		m_vertices = new ArrayList<>(16);
		m_vertices.add(origin);
	}
	private final Vertex m_origin;
	private final Bearing m_originPivot;
	private final int m_pivotX;
	private final int m_pivotY;
	private final List<Vertex> m_vertices;
}
