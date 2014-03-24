/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.awt.Color;
import java.util.List;

import org.junit.Test;

/**
 * @author roach
 */
public class TestClusterPaint {

	private static final Color colorStrike = null; // new Color(0.8f, 0.8f, 0.1f, 0.2f);
	private static final Color colorCell = new Color(0.3f, 0.9f, 0.3f, 0.9f);
	private static final Color colorPolygon = new Color(0.7f, 0.9f, 0.9f, 0.9f);
	private static final Color colorPolyline = new Color(0.3f, 0.3f, 0.7f, 0.9f);

	private void paint(TestHelpCanvas canvas, BzeStrikeClusterShape shape, String label, BzeStrike[] strikes) {
		final BzeStrikePolygon[] polygons = shape.polygons();
		for (int i = 0; i < polygons.length; i++) {
			canvas.plot(polygons[i], colorPolygon, null);
		}
		final BzeStrikePolyline[] polylines = shape.polylines();
		for (int i = 0; i < polylines.length; i++) {
			canvas.plot(polylines[i], colorPolyline, null);
		}
		final BzeStrikeCell[] cells = shape.cells();
		for (int i = 0; i < cells.length; i++) {
			canvas.plot(cells[i], colorCell, null);
		}
		final int strikeCount = strikes.length;
		for (int i = 0; i < strikeCount; i++) {
			final BzeStrike strike = strikes[i];
			canvas.plot(strike, colorStrike, null);
		}
	}

	private void paint(TestHelpCanvas canvas, BzeStrikeClusterTable table) {
		final BzeStrikeCluster[] clusters = table.clusterArray();
		for (int cid = 0; cid < clusters.length; cid++) {
			final BzeStrikeCluster cluster = clusters[cid];
			final BzeStrikeClusterShape shape = cluster.clusterShape();
			final BzeStrike[] strikes = cluster.strikes();
			paint(canvas, shape, "#" + cid, strikes);
		}
	}

	@Test
	public void t50() {
		final List<BzeStrike> strikes = TestHelpLoader.newListFromResource(getClass(), "2012_07_11_lightning_data.csv");
		System.out.println("Strikes=" + strikes.size());
		final BzeStrikeClusteringEngine engine = BzeStrikeClusteringEngine.newInstance(strikes);
		final BzeStrikeClusterTable table = engine.solve(0.1f, 3, 0.6f);
		final BzeStrikeBounds bounds = table.bounds();
		final TestHelpCanvas canvas = new TestHelpCanvas(bounds, 1500, 1200);
		paint(canvas, table);
		canvas.save("t50");
	}

}
