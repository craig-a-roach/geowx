/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;

/**
 * @author roach
 */
public class TestStrikePaint {

	private static final String[] popD = { "1341965133494,-6.3704, 152.8754,-2.0,GROUND",
			"1341976756872,-6.3213, 152.8321,-11.0,GROUND", "1341978101046,-6.3412, 152.9054,-5.0,GROUND",
			"1341978815425,-6.3373, 152.8744,-1.0,GROUND", "1341979254621,-6.3779, 152.8444,-64.0,GROUND",
			"1342013599646,-6.3079, 152.9032,-2.0,GROUND", "1342014685240,-6.3908, 152.9207,-2.0,GROUND" };

	private void render(List<Strike> strikes, StrikeClusterTable table, Canvas canvas, Color oAll, Color oNoise, Color oConvex,
			Color oConcave) {
		final StrikeCluster[] clusterArray = table.clusterArray();
		final int clusterCount = clusterArray.length;
		for (int i = 0; i < clusterCount; i++) {
			final StrikeCluster strikeCluster = clusterArray[i];
			if (strikeCluster == null) {
				continue; // TODO
			}
			final StrikePolygon polygon = strikeCluster.strikePolygon();
			if (oConvex != null) {
				final Strike[] convex = polygon.convexVertices();
				canvas.plotPolygon(convex, oConvex);
			}
			if (oConcave != null) {
				final Strike[] concave = polygon.concaveVertices();
				canvas.plotPolygon(concave, oConcave);
			}
		}
		if (oAll != null) {
			final int strikeCount = strikes.size();
			for (int i = 0; i < strikeCount; i++) {
				final Strike strike = strikes.get(i);
				canvas.plotPoint(strike, oAll);
			}
		}
		if (oNoise != null) {
			final Strike[] noiseArray = table.noiseArray();
			final int noiseCount = noiseArray.length;
			for (int i = 0; i < noiseCount; i++) {
				final Strike strike = noiseArray[i];
				canvas.plotPoint(strike, oNoise);
			}
		}
	}

	@Test
	public void a10() {
		final List<Strike> strikes = TestHelpLoader.newListFromLines(popD);
		final StrikeClusteringEngine engine = StrikeClusteringEngine.newInstance(strikes);
		final StrikeClusterTable table = engine.solve(0.05f, 3);
		final Canvas canvas = new Canvas(table.bounds(), 1024, 640);
		render(strikes, table, canvas, Color.gray, Color.blue, Color.cyan, Color.orange);
		canvas.save("a10");
	}

	@Test
	public void t50() {
		final List<Strike> strikes = TestHelpLoader.newListFromResource(getClass(), "2012_07_11_lightning_data.csv");
		final StrikeClusteringEngine engine = StrikeClusteringEngine.newInstance(strikes);
		final StrikeClusterTable table = engine.solve(0.05f, 3);
		final Canvas canvas = new Canvas(table.bounds(), 1024, 640);
		render(strikes, table, canvas, null, null, null, Color.orange);
		canvas.save("base");
	}

	private static class Canvas {

		public void plotPoint(Strike s, Paint paint) {
			final int x = x(s.x);
			final int y = y(s.y);
			m_g2d.setPaint(paint);
			m_g2d.fillRect(x, y, 1, 1);
		}

		public void plotPolygon(Strike[] strikeConvexHull, Color paint) {
			final int vertexCount = strikeConvexHull.length;
			final int[] xPoints = new int[vertexCount];
			final int[] yPoints = new int[vertexCount];
			for (int i = 0; i < vertexCount; i++) {
				final Strike s = strikeConvexHull[i];
				xPoints[i] = x(s.x);
				yPoints[i] = y(s.y);
			}
			m_g2d.setPaint(paint);
			m_g2d.fillPolygon(xPoints, yPoints, vertexCount);
		}

		public Path save(String filePrefix) {
			try {
				final String home = System.getProperty("user.home");
				final Path homePath = FileSystems.getDefault().getPath(home, "blitzen");
				Files.createDirectories(homePath);
				final Path outPath = homePath.resolve(filePrefix + ".png");
				ImageIO.write(m_bimg, "png", outPath.toFile());
				System.out.println("Saved to " + outPath);
				return outPath;
			} catch (final IOException ex) {
				System.err.println(ex);
				return null;
			}
		}

		public int x(float strikeX) {
			return Math.round(((strikeX - bounds.x) / bounds.width) * m_width);
		}

		public int y(float strikeY) {
			return Math.round(((bounds.y - strikeY) / bounds.height) * m_height);
		}

		public Canvas(Rectangle2D.Float b, int w, int h) {
			this.bounds = b;
			m_width = w;
			m_height = h;
			m_bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			m_g2d = m_bimg.createGraphics();
			m_g2d.setColor(Color.WHITE);
			m_g2d.fillRect(0, 0, w, h);
		}
		final Rectangle2D.Float bounds;
		private final int m_width;
		private final int m_height;
		private final BufferedImage m_bimg;
		private final Graphics2D m_g2d;
	}

}
