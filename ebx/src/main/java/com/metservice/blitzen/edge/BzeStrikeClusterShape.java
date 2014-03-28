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
public class BzeStrikeClusterShape {

	private static final BzeStrikeCell[] ZEROCELLS = new BzeStrikeCell[0];
	private static final BzeStrikePolyline[] ZEROPOLYLINES = new BzeStrikePolyline[0];
	private static final BzeStrikePolygon[] ZEROPOLYGONS = new BzeStrikePolygon[0];

	private static void fillMesh(BzeStrike[] strikes, float grid, BzeStrikeBounds bounds, BitMesh store) {
		final int strikeCount = strikes.length;
		for (int i = 0; i < strikeCount; i++) {
			final BzeStrike strike = strikes[i];
			final int ex = Vertex.gridX(strike, bounds, grid);
			final int ey = Vertex.gridY(strike, bounds, grid);
			store.set(ex, ey, true);
		}
	}

	private static BzeStrikeClusterShape newEmpty(List<IPolyline> polylines, BzeStrikeBounds bounds) {
		final int count = polylines.size();
		int cellCount = 0;
		int polylineCount = 0;
		int polygonCount = 0;
		for (int i = 0; i < count; i++) {
			final IPolyline p = polylines.get(i);
			if (p instanceof Vertex) {
				cellCount++;
				continue;
			}
			if (p instanceof Polyline) {
				final Polyline polyline = (Polyline) p;
				if (polyline.isPolygon()) {
					polygonCount++;
				} else {
					polylineCount++;
				}
				continue;
			}
			throw new IllegalStateException("unsupported polyline" + p.getClass());
		}
		BzeStrikeCell[] cellArray = ZEROCELLS;
		if (cellCount > 0) {
			cellArray = new BzeStrikeCell[cellCount];
		}
		BzeStrikePolyline[] polylineArray = ZEROPOLYLINES;
		if (polylineCount > 0) {
			polylineArray = new BzeStrikePolyline[polylineCount];
		}
		BzeStrikePolygon[] polygonArray = ZEROPOLYGONS;
		if (polygonCount > 0) {
			polygonArray = new BzeStrikePolygon[polygonCount];
		}
		return new BzeStrikeClusterShape(cellArray, polylineArray, polygonArray, bounds);
	}

	private static BzeStrikeClusterShape newInstance(BitMesh store, BzeStrikeBounds bounds, float grid) {
		final VertexGenerator vg = new VertexGenerator(store);
		final List<IPolyline> polylines = vg.newShape();
		return newInstance(polylines, bounds, grid);
	}

	private static BzeStrikeClusterShape newInstance(List<IPolyline> polylines, BzeStrikeBounds bounds, float grid) {
		final int count = polylines.size();
		final BzeStrikeClusterShape neo = newEmpty(polylines, bounds);
		int cellIndex = 0;
		int polylineIndex = 0;
		int polygonIndex = 0;
		for (int i = 0; i < count; i++) {
			final IPolyline p = polylines.get(i);
			if (p instanceof Vertex) {
				final Vertex vertex = (Vertex) p;
				final float sx = vertex.strikeX(bounds, grid);
				final float sy = vertex.strikeY(bounds, grid);
				neo.m_cellArray[cellIndex] = new BzeStrikeCell(sx, sy, grid);
				cellIndex++;
				continue;
			}
			if (p instanceof Polyline) {
				final Polyline polyline = (Polyline) p;
				final float[] xyPairs = polyline.xyPairs(bounds, grid);
				if (polyline.isPolygon()) {
					neo.m_polygonArray[polygonIndex] = new BzeStrikePolygon(xyPairs);
					polygonIndex++;
				} else {
					neo.m_polylineArray[polylineIndex] = new BzeStrikePolyline(xyPairs, grid);
					polylineIndex++;
				}
				continue;
			}
		}
		return neo;
	}

	public static BzeStrikeClusterShape newInstance(BzeStrike[] strikes, float grid) {
		if (strikes == null || strikes.length == 0) throw new IllegalArgumentException("array is null or empty");
		final BzeStrikeBounds bounds = BzeStrikeBounds.newInstance(strikes, grid);
		final int width = bounds.widthGrid(grid);
		final int height = bounds.heightGrid(grid);
		final BitMesh store = new BitMesh(width, height);
		fillMesh(strikes, grid, bounds, store);
		return newInstance(store, bounds, grid);
	}

	public BzeStrikeBounds bounds() {
		return m_bounds;
	}

	public int cellCount() {
		return m_cellArray.length;
	}

	public BzeStrikeCell[] cells() {
		return m_cellArray;
	}

	public int polygonCount() {
		return m_polygonArray.length;
	}

	public BzeStrikePolygon[] polygons() {
		return m_polygonArray;
	}

	public int polylineCount() {
		return m_polylineArray.length;
	}

	public BzeStrikePolyline[] polylines() {
		return m_polylineArray;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("polygons=").append(m_polygonArray.length);
		sb.append(", polylines=").append(m_polylineArray.length);
		sb.append(", cells=").append(m_cellArray.length);
		sb.append(", bounds(").append(m_bounds).append(")");
		return sb.toString();
	}

	public int vertexCount() {
		int sum = m_cellArray.length;
		for (int i = 0; i < m_polylineArray.length; i++) {
			sum += m_polylineArray[i].vertexCount();
		}
		for (int i = 0; i < m_polygonArray.length; i++) {
			sum += m_polygonArray[i].vertexCount();
		}
		return sum;
	}

	private BzeStrikeClusterShape(BzeStrikeCell[] cellArray, BzeStrikePolyline[] polylineArray, BzeStrikePolygon[] polygonArray,
			BzeStrikeBounds bounds) {
		assert cellArray != null;
		m_cellArray = cellArray;
		m_polylineArray = polylineArray;
		m_polygonArray = polygonArray;
		m_bounds = bounds;
	}
	private final BzeStrikeCell[] m_cellArray;
	private final BzeStrikePolyline[] m_polylineArray;
	private final BzeStrikePolygon[] m_polygonArray;
	private final BzeStrikeBounds m_bounds;
}
