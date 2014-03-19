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
class StrikeClusterShape {

	private static StrikeClusterShape newInstance(BitMesh store, StrikeBounds bounds, float eps) {
		final VertexGenerator vg = new VertexGenerator(store);
		final List<IPolyline> polylines = vg.newShape();
		final int count = polylines.size();
		for (int i = 0; i < count; i++) {
			final IPolyline p = polylines.get(i);
			if (p instanceof Polygon) {

			}

		}
		return new StrikeClusterShape();
	}

	public static StrikeClusterShape newInstance(Strike[] strikes, float eps) {
		if (strikes == null || strikes.length == 0) throw new IllegalArgumentException("array is null or empty");
		final StrikeBounds bounds = StrikeBounds.newInstance(strikes);
		final int height = ((int) (bounds.height() / eps)) + 1;
		final int width = ((int) (bounds.width() / eps)) + 1;
		final BitMesh store = new BitMesh(height, width);
		final int strikeCount = strikes.length;
		for (int i = 0; i < strikeCount; i++) {
			final Strike strike = strikes[i];
			final int ey = (int) ((strike.y - bounds.yB) / eps);
			final int ex = (int) ((strike.x - bounds.xL) / eps);
			store.set(ey, ex, true);
		}
		return newInstance(store, bounds, eps);
	}

	private StrikeClusterShape() {
	}
}
