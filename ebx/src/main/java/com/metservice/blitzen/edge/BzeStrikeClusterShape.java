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

	private static BzeStrikeClusterShape newInstance(BitMesh store, BzeStrikeBounds bounds, float eps) {
		final VertexGenerator vg = new VertexGenerator(store);
		final List<IPolyline> polylines = vg.newShape();
		return newInstance(polylines, bounds, eps);
	}

	private static BzeStrikeClusterShape newInstance(List<IPolyline> polylines, BzeStrikeBounds bounds, float eps) {
		final int count = polylines.size();
		final BzeStrikeClusterShape neo = newEmpty(polylines, bounds);
		int cellIndex = 0;
		int polylineIndex = 0;
		int polygonIndex = 0;
		for (int i = 0; i < count; i++) {
			final IPolyline p = polylines.get(i);
			if (p instanceof Vertex) {
				final Vertex vertex = (Vertex) p;
				final float sx = vertex.strikeX(bounds, eps);
				final float sy = vertex.strikeY(bounds, eps);
				neo.m_cellArray[cellIndex] = new BzeStrikeCell(sx, sy, eps);
				cellIndex++;
				continue;
			}
			if (p instanceof Polyline) {
				final Polyline polyline = (Polyline) p;
				final float[] xyPairs = polyline.xyPairs(bounds, eps);
				if (polyline.isPolygon()) {
					neo.m_polygonArray[polygonIndex] = new BzeStrikePolygon(xyPairs);
					polygonIndex++;
				} else {
					neo.m_polylineArray[polylineIndex] = new BzeStrikePolyline(xyPairs);
					polylineIndex++;
				}
				continue;
			}
		}
		return neo;
	}

	public static BzeStrikeClusterShape newInstance(BzeStrike[] strikes, float eps) {
		if (strikes == null || strikes.length == 0) throw new IllegalArgumentException("array is null or empty");
		final BzeStrikeBounds bounds = BzeStrikeBounds.newInstance(strikes);
		final int height = ((int) (bounds.height() / eps)) + 1;
		final int width = ((int) (bounds.width() / eps)) + 1;
		final BitMesh store = new BitMesh(height, width);
		final int strikeCount = strikes.length;
		for (int i = 0; i < strikeCount; i++) {
			final BzeStrike strike = strikes[i];
			final int ey = (int) ((strike.y - bounds.yB) / eps);
			final int ex = (int) ((strike.x - bounds.xL) / eps);
			store.set(ey, ex, true);
		}
		return newInstance(store, bounds, eps);
	}

	public BzeStrikeBounds bounds() {
		return m_bounds;
	}

	public BzeStrikeCell[] cells() {
		return m_cellArray;
	}

	public BzeStrikePolygon[] polygons() {
		return m_polygonArray;
	}

	public BzeStrikePolyline[] polylines() {
		return m_polylineArray;
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
