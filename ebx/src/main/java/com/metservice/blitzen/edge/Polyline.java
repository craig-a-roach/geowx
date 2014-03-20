/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.util.List;

/**
 * @author roach
 */
class Polyline implements IPolyline {

	public boolean isPolygon() {
		return m_isClosed;
	}

	@Override
	public String toString() {
		return (m_isClosed ? "POLYGON:" : "") + m_vertices.toString();
	}

	public float[] xyPairs(BzeStrikeBounds bounds, float eps) {
		final int vertexCount = m_vertices.size();
		final int coordCount = vertexCount * 2;
		final float[] xy = new float[coordCount];
		final float xL = bounds.xL;
		final float yB = bounds.yB;
		for (int iv = 0, ix = 0, iy = 1; iv < vertexCount; iv++, ix += 2, iy += 2) {
			final Vertex vertex = m_vertices.get(iv);
			xy[ix] = Vertex.strikeX(vertex, xL, eps);
			xy[iy] = Vertex.strikeY(vertex, yB, eps);
		}
		return xy;
	}

	public Polyline(List<Vertex> vertices, boolean isClosed) {
		m_vertices = vertices;
		m_isClosed = isClosed;
	}
	private final List<Vertex> m_vertices;
	private final boolean m_isClosed;
}
