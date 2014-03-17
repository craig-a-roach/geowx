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

	private Bearing consume(Vertex start, int x, int y, Bearing bearing) {
		final int sx = start.x;
		final int sy = start.y;
		for (int i = 0; i < 8; i++) {
			final Bearing next = bearing.path(i);
			final int nx = x + next.dx;
			final int ny = y + next.dy;
			if (nx == sx && ny == sy) return next;
			if (m_store.clear(nx, ny)) return next;
		}
		return Bearing.STAY;
	}

	private Vertex consumeLeftBottom() {
		for (int x = 0; x < m_width; x++) {
			for (int y = 0; y < m_height; y++) {
				if (m_store.clear(x, y)) return new Vertex(x, y);
			}
		}
		return null;
	}

	private Vertex consumePolarMax(Vertex ref) {
		final Bearing[] Polar = Bearing.Polar;
		final int polarCount = Polar.length;
		for (int i = 0; i < polarCount; i++) {
			final Bearing b = Polar[i];
			final int px = ref.x + b.dx;
			final int py = ref.y + b.dy;
			if (m_store.clear(px, py)) return new Vertex(px, py);
		}
		return null;
	}

	private IPolyline consumePolyline(Vertex start, Vertex polar) {
		assert start != null;
		assert polar != null;
		boolean isClosed = false;
		int originX = start.x;
		int originY = start.y;
		int pivotX = polar.x;
		int pivotY = polar.y;
		Bearing originPivot = Bearing.select(pivotX - originX, pivotY - originY);
		final EdgeBuilder eb = new EdgeBuilder(start, originPivot);
		boolean detecting = true;
		while (detecting) {
			final Bearing pivotHead = consume(start, pivotX, pivotY, originPivot);
			if (pivotHead == Bearing.STAY) {
				detecting = false;
				continue;
			}
			eb.add(pivotHead);
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
			originPivot = pivotHead;
		}
		if (isClosed) {
			eb.fillPolygon(m_store);
		}
		final List<Vertex> vertices = eb.newVertices();
		if (isClosed) return new Polygon(vertices);
		return new Polyline(vertices);
	}

	public List<IPolyline> newShape() {
		final List<IPolyline> result = new ArrayList<IPolyline>();
		boolean morePolyLines = true;
		while (morePolyLines) {
			final Vertex oLB = consumeLeftBottom();
			if (oLB == null) {
				morePolyLines = false;
				continue;
			}
			final Vertex oPM = consumePolarMax(oLB);
			if (oPM == null) {
				result.add(oLB);
				continue;
			}
			final IPolyline polyline = consumePolyline(oLB, oPM);
			System.out.println(polyline);
			result.add(polyline);
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
