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
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;

/**
 * @author roach
 */
public class TestStrikePaint {

	private void render(List<Strike> strikes, StrikeClusterTable table, Canvas canvas) {
		final int strikeCount = strikes.size();
		for (int i = 0; i < strikeCount; i++) {
			final Strike strike = strikes.get(i);
			canvas.plotPoint(strike.x, strike.y, Color.black);
		}
	}

	@Test
	public void t50() {
		final List<Strike> strikes = TestHelpLoader.newListFromResource(getClass(), "2012_07_11_lightning_data.csv");
		final StrikeClusteringEngine engine = StrikeClusteringEngine.newInstance(strikes);
		final StrikeClusterTable table = engine.solve(0.1f, 3);
		final Canvas canvas = new Canvas(table.bounds(), 1024, 640);
		render(strikes, table, canvas);
		canvas.save("base");
	}

	private static class Canvas {

		public void plotPoint(float sX, float sY, Paint paint) {
			final int x = x(sX);
			final int y = y(sY);
			m_g2d.setPaint(paint);
			m_g2d.fillRect(x, y, 1, 1);
		}

		public Path save(String filePrefix) {
			try {
				final String home = System.getProperty("user.home");
				final Path outPath = FileSystems.getDefault().getPath(home, "blitzen", filePrefix + ".png");
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
