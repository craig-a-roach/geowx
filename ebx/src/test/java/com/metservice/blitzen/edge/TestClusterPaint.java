/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

/**
 * @author roach
 */
public class TestClusterPaint {

	private static final Color colorStrike = new Color(0.8f, 0.8f, 0.1f, 0.2f);
	private static final Color colorPolygon = new Color(0.1f, 0.5f, 0.9f, 0.9f);
	private static final Color colorPolyline = colorPolygon; // new Color(0.3f, 0.3f, 0.7f, 0.9f);
	private static final Color colorCell = colorPolygon; // new Color(0.3f, 0.9f, 0.3f, 0.9f);
	private static final NumberFormat formatMag = formatMag();

	private static NumberFormat formatMag() {
		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(1);
		nf.setMaximumFractionDigits(1);
		return nf;
	}

	private void drawMag(TestHelpCanvas canvas, BzeStrikeClusterTable table, float minMag) {
		final BzeStrikeCluster[] clusters = table.clusterArray();
		for (int cid = 0; cid < clusters.length; cid++) {
			final BzeStrikeCluster cluster = clusters[cid];
			final BzeStrikeClusterShape shape = cluster.clusterShape();
			final float avg = cluster.qtyMagnitudeAverage();
			if (avg > minMag) {
				final float max = cluster.qtyMagnitudeMax();
				final String mag = formatMag.format(avg) + "/" + formatMag.format(max);
				canvas.text(shape.bounds(), mag);
			}
		}
	}

	private void paint(TestHelpCanvas canvas, BzeStrike[] strikes) {
		final int strikeCount = strikes.length;
		for (int i = 0; i < strikeCount; i++) {
			final BzeStrike strike = strikes[i];
			canvas.plot(strike, colorStrike, null);
		}
	}

	private void paint(TestHelpCanvas canvas, BzeStrikeClusterShape shape, String label) {
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
	}

	private void paint(TestHelpCanvas canvas, BzeStrikeClusterTable table) {
		final BzeStrikeCluster[] clusters = table.clusterArray();
		for (int cid = 0; cid < clusters.length; cid++) {
			final BzeStrikeCluster cluster = clusters[cid];
			final BzeStrikeClusterShape shape = cluster.clusterShape();
			paint(canvas, shape, "#" + cid);
		}
	}

	private void printStrikeInfo(List<BzeStrike> strikes) {
		final int count = strikes.size();
		if (count == 0) return;
		long tmin = strikes.get(0).t;
		long tmax = tmin;
		for (int i = 1; i < count; i++) {
			final BzeStrike strike = strikes.get(i);
			tmin = Math.min(tmin, strike.t);
			tmax = Math.max(tmax, strike.t);
		}
		System.out.println("c=" + count + ", t=" + new Date(tmin) + " to " + new Date(tmax));
	}

	// @Test
	public void t50() {
		final List<BzeStrike> strikes = TestHelpLoader.newListByTimeFromResource(getClass(), "2012_07_11_lightning_data.csv");
		final BzeStrike[] strikeArray = TestHelpLoader.toArray(strikes);
		final BzeStrikeBounds bounds = BzeStrikeBounds.newInstance(strikeArray);
		final int strikeCount = strikeArray.length;
		printStrikeInfo(strikes);
		if (strikeCount == 0) return;
		final int pixelW = 1600;
		final int pixelH = 1200;
		final float minMag = Float.MAX_VALUE;
		final BzeStrikeClusteringEngine engine = BzeStrikeClusteringEngine.newInstance(strikes);
		for (int n = 3; n <= 3; n++) {
			for (int e = 1; e <= 4; e++) {
				final float eps = 0.05f * e;
				for (int q = 0; q < 10; q++) {
					final float quality = 0.2f + (q * 0.2f);
					final long tsStart = System.currentTimeMillis();
					final BzeStrikeClusterTable table = engine.solve(eps, n, quality);
					final long tsElapsed = System.currentTimeMillis() - tsStart;
					final int vertexCount = table.vertexCount();
					final int vertexPct = Math.round(vertexCount * 100.0f / strikeCount);
					final int polygonCount = table.polygonCount();
					final String legend1 = "Cluster: n >=" + n + ", separation<=" + eps + "deg | Quality=" + quality;
					final String legend2 = "vertices=" + vertexCount + "(" + vertexPct + "%) fills=" + polygonCount
							+ ", strikes=" + strikeCount + ", elapsed=" + tsElapsed + "ms, bounds=" + bounds + " deg";
					final TestHelpCanvas canvas = new TestHelpCanvas(bounds, pixelW, pixelH, legend1, legend2);
					paint(canvas, strikeArray);
					paint(canvas, table);
					drawMag(canvas, table, minMag);
					final String fname = "cluster" + n + "_e" + e + "_q" + q;
					canvas.save(fname, "series");
				}
			}
		}
		final TestHelpCanvas canvas = new TestHelpCanvas(bounds, pixelW, pixelH, "No Clustering: 11-July-2012", "strikes="
				+ strikeCount + ", bounds=" + bounds + " deg");
		paint(canvas, strikeArray);
		canvas.save("strikes", "series");
	}

	@Test
	public void t60() {
		final List<BzeStrike> strikesDay = TestHelpLoader
				.newListByTimeFromResource(getClass(), "2012_07_11_lightning_data.csv");
		final BzeStrike[] strikeDayArray = TestHelpLoader.toArray(strikesDay);
		final BzeStrikeBounds bounds = BzeStrikeBounds.newInstance(strikeDayArray);
		final int pixelW = 1600;
		final int pixelH = 1200;
		final int n = 3;
		final float eps = 0.1f;
		final float quality = 0.6f;
		for (int hour = 0; hour < 23; hour++) {
			final List<BzeStrike> strikes = TestHelpLoader.newHourFromListByTime(strikesDay, hour, 1);
			final int strikeCount = strikes.size();
			final BzeStrike[] strikeArray = TestHelpLoader.toArray(strikes);
			printStrikeInfo(strikes);
			final BzeStrikeClusteringEngine engine = BzeStrikeClusteringEngine.newInstance(strikes);
			final long tsStart = System.currentTimeMillis();
			final BzeStrikeClusterTable table = engine.solve(eps, n, quality);
			final long tsElapsed = System.currentTimeMillis() - tsStart;
			final int vertexCount = table.vertexCount();
			final int vertexPct = Math.round(vertexCount * 100.0f / strikeCount);
			final int polygonCount = table.polygonCount();
			final String legend1 = "Cluster: n >=" + n + ", separation<=" + eps + "deg | Quality=" + quality;
			final String legend2 = "vertices=" + vertexCount + "(" + vertexPct + "%) fills=" + polygonCount + ", strikes="
					+ strikeCount + ", elapsed=" + tsElapsed + "ms, bounds=" + bounds + " deg";
			final TestHelpCanvas canvas = new TestHelpCanvas(bounds, pixelW, pixelH, legend1, legend2);
			paint(canvas, strikeArray);
			paint(canvas, table);
			final String fname = "hour" + hour;
			canvas.save(fname, "day");
		}
	}

}
