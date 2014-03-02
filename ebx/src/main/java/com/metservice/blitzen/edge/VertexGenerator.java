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
class VertexGenerator {

	private Bearing consume(int x, int y, Bearing bearing) {
		for (int i = 0; i < 8; i++) {
			final Bearing next = bearing.path(i);
			final int nx = x + next.dx;
			final int ny = y + next.dy;
			if (m_store.clear(nx, ny)) return next;
		}
		return Bearing.STAY;
	}

	private Vertex consumePolarMax(Vertex ref) {
		for (int x = ref.x; x < m_width; x++) {
			for (int y = ref.y; y < m_height; y++) {
				if (m_store.value(x, y)) return new Vertex(x, y);
			}
		}
		for (int x = ref.x; x < m_width; x++) {
			for (int y = ref.y - 1; y >= 0; y--) {
				if (m_store.value(x, y)) return new Vertex(x, y);
			}
		}
		return null;
	}

	private IPolyline consumePolyline(Vertex start, Vertex polar) {
		assert start != null;
		assert polar != null;
		final List<Vertex> vertices = new ArrayList<>();
		boolean isClosed = false;
		vertices.add(start);
		vertices.add(polar);
		int originX = start.x;
		int originY = start.y;
		int pivotX = polar.x;
		int pivotY = polar.y;
		final int originPivotX = polar.x - start.x;
		final int originPivotY = polar.y - start.y;
		final Bearing originPivot = Bearing.select(originPivotX, originPivotY);
		boolean detecting = true;
		while (detecting) {
			final Bearing pivotHead = consume(pivotX, pivotY, originPivot);
			if (pivotHead == Bearing.STAY) {
				detecting = false;
				continue;
			}
			final int headX = pivotX + pivotHead.dx;
			final int headY = pivotY + pivotHead.dy;
			if (headX == start.x && headY == start.y) {
				isClosed = true;
				detecting = false;
				continue;
			}

			originX = pivotX;
			originY = pivotY;
			pivotX = headX;
			pivotY = headY;
		}
		if (isClosed) return new Polygon(vertices);
		return new Polyline(vertices);
	}

	private Vertex findLeftBottom() {
		for (int x = 0; x < m_width; x++) {
			for (int y = 0; y < m_height; y++) {
				if (m_store.value(x, y)) return new Vertex(x, y);
			}
		}
		return null;
	}

	public List<IPolyline> newShape() {
		final List<IPolyline> result = new ArrayList<IPolyline>();
		boolean morePolyLines = true;
		while (morePolyLines) {
			final Vertex oLB = findLeftBottom();
			if (oLB == null) {
				morePolyLines = false;
				continue;
			}
			final Vertex oPM = consumePolarMax(oLB);
			if (oPM == null) {
				result.add(oLB);
				continue;
			}
			final IPolyline oPolyline = consumePolyline(oLB, oPM);
		}

		return result;
	}

	public VertexGenerator(BitMesh store) {
		if (store == null) throw new IllegalArgumentException("object is null");
		m_store = store;
		m_height = store.height();
		m_width = store.width();
	}

	private final BitMesh m_store;
	private final int m_height;
	private final int m_width;
}
