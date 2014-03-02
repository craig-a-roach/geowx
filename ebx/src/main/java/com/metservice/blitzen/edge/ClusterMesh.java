/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

/**
 * @author roach
 */
class ClusterMesh {

	public static ClusterMesh newInstance(BitMesh store, StrikeBounds bounds, float eps) {

		return new ClusterMesh();
	}

	public static ClusterMesh newInstance(Strike[] strikes, StrikeBounds bounds, float eps) {
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

	private ClusterMesh() {
	}
}
