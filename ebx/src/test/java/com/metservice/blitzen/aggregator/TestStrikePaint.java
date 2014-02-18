/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.metservice.gallium.GalliumBoundingBoxD;
import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
public class TestStrikePaint {

	@Test
	public void t50() {
		final List<Strike> strikes = TestHelpLoader.newListFromResource(getClass(), "2012_07_11_lightning_data.csv");
		final StrikeClusteringEngine engine = StrikeClusteringEngine.newInstance(strikes);
		final StrikeClusterTable table = engine.solve(0.1f, 3);
	}

	private static class Canvas {

		// final Color fg = fill ? new Color(210, 200, 180) : new Color(150, 100, 50);
		// final Color bg = fill ? new Color(50, 50, 80) : new Color(200, 200, 230);

		public void draw(Path2D.Float path) {
			if (m_fill) {
				m_g2d.fill(path);
			} else {
				m_g2d.draw(path);
			}
		}

		public void save(File dst) {
			try {
				ImageIO.write(m_bimg, "png", dst);
			} catch (final IOException ex) {
				System.err.println(ex);
			}
		}

		public float x(GalliumPointD pt) {
			final double w = bounds.width();
			final double xLo = bounds.xLo();
			final double xd = ((pt.x - xLo) / w) * m_width;
			return (float) xd;
		}

		public float y(GalliumPointD pt) {
			final double h = bounds.height();
			final double yHi = bounds.yHi();
			final double xd = ((yHi - pt.y) / h) * m_height;
			return (float) xd;
		}

		public Canvas(GalliumBoundingBoxD b, boolean fill, Color bg, Color fg) {
			final int w = (int) Math.round(b.width() * 50.0);
			final int h = (int) Math.round(b.height() * 50.0);
			this.bounds = b;
			m_width = w;
			m_height = h;
			m_fill = fill;
			m_bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			m_g2d = m_bimg.createGraphics();
			m_g2d.setColor(bg);
			m_g2d.fillRect(0, 0, w, h);
			if (fill) {
				m_g2d.setPaint(fg);
			} else {
				m_g2d.setColor(fg);
			}
		}
		final GalliumBoundingBoxD bounds;
		private final int m_width;
		private final int m_height;
		private final boolean m_fill;
		private final BufferedImage m_bimg;
		private final Graphics2D m_g2d;
	}

}
