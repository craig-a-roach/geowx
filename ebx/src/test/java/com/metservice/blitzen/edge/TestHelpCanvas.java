/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

/**
 * @author roach
 */
public class TestHelpCanvas {

	private static final int MARGIN = 100;
	private static final int TMARGIN = 50;

	public int h(float height) {
		return Math.round(height / bounds.height() * m_height);
	}

	public void plot(BzeStrike strike, Paint oPaint, String oText) {
		if (oPaint == null) return;
		final int xL = x(strike.x);
		final int yB = y(strike.y);
		m_g2d.setPaint(oPaint);
		m_g2d.fillRect(xL, yB + 4, 4, 4);
		if (oText != null) {
			m_g2d.setColor(Color.black);
			m_g2d.drawString(oText, xL, yB);
		}
	}

	public void plot(BzeStrikeBounds box, Color paint, String oText) {
		final int x = x(box.xL);
		final int y = y(box.yT);
		final int w = w(box.width());
		final int h = h(box.height());
		m_g2d.setPaint(paint);
		m_g2d.fillRect(x, y, w, h);
		if (oText != null) {
			m_g2d.setColor(Color.black);
			m_g2d.drawString(oText, x, y);
		}
	}

	public void plot(BzeStrikeCell cell, Paint oPaint, String oText) {
		if (oPaint != null) {
			final int xL = x(cell.xLeft());
			final int yT = y(cell.yTop());
			final int w = w(cell.width());
			final int h = w(cell.height());
			m_g2d.setPaint(oPaint);
			m_g2d.fillRect(xL, yT, w, h);
			if (oText != null) {
				m_g2d.setColor(Color.black);
				m_g2d.drawString(oText, xL, yT);
			}
		}
	}

	public void plot(BzeStrikePolygon polygon, Color oPaint, String oText) {
		final int vertexCount = polygon.vertexCount();
		if (oPaint != null) {
			final int[] xPoints = new int[vertexCount];
			final int[] yPoints = new int[vertexCount];
			for (int i = 0; i < vertexCount; i++) {
				xPoints[i] = x(polygon.x(i));
				yPoints[i] = y(polygon.y(i));
			}
			m_g2d.setPaint(oPaint);
			m_g2d.fillPolygon(xPoints, yPoints, vertexCount);
			if (oText != null) {
				m_g2d.setColor(Color.black);
				m_g2d.drawString(oText, xPoints[0], yPoints[0]);
			}
		}
	}

	public void plot(BzeStrikePolyline polyline, Color oOutline, String oText) {
		final int vertexCount = polyline.vertexCount();
		if (oOutline != null) {
			final float sw = w(polyline.thick());
			final Stroke s = new BasicStroke(sw);
			final int[] xPoints = new int[vertexCount];
			final int[] yPoints = new int[vertexCount];
			for (int i = 0; i < vertexCount; i++) {
				xPoints[i] = x(polyline.x(i));
				yPoints[i] = y(polyline.y(i));
			}
			m_g2d.setColor(oOutline);
			m_g2d.setStroke(s);
			m_g2d.drawPolyline(xPoints, yPoints, vertexCount);
			if (oText != null) {
				m_g2d.setColor(Color.black);
				m_g2d.drawString(oText, xPoints[0], yPoints[0]);
			}
		}
	}

	public Path save(String filePrefix, String sub) {
		try {
			final String home = System.getProperty("user.home");
			final Path homePath = FileSystems.getDefault().getPath(home, "blitzen.edge." + sub);
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

	public void text(BzeStrikeBounds box, String oText) {
		final int x = x(box.xL);
		final int y = y(box.yB);
		if (oText != null) {
			m_g2d.setColor(Color.black);
			m_g2d.drawString(oText, x, y);
		}
	}

	public int w(float width) {
		return Math.round(width / bounds.width() * m_width);
	}

	public int x(float strikeX) {
		return Math.round(((strikeX - bounds.xL) / bounds.width()) * m_width);
	}

	public int y(float strikeY) {
		return m_height - Math.round(((strikeY - bounds.yB) / bounds.height()) * m_height);
	}

	public TestHelpCanvas(BzeStrikeBounds b, int w, int h, String t1, String t2) {
		this.bounds = b;
		m_width = w;
		m_height = h;
		final int wm2 = MARGIN + w + MARGIN;
		final int hm2 = MARGIN + h + MARGIN;
		m_bimg = new BufferedImage(wm2, hm2, BufferedImage.TYPE_INT_ARGB);
		m_g2d = m_bimg.createGraphics();
		m_g2d.setPaint(Color.WHITE);
		m_g2d.fillRect(0, 0, wm2, hm2);
		m_g2d.setColor(Color.BLACK);
		m_g2d.drawRect(MARGIN, MARGIN, w, h);
		final Font std = m_g2d.getFont();
		final Font fbig = std.deriveFont(24.0f);
		final Font fsmall = std.deriveFont(10.0f);
		if (t1 != null) {
			m_g2d.setFont(fbig);
			m_g2d.drawString(t1, 10, TMARGIN);
		}
		if (t2 != null) {
			m_g2d.setFont(fbig);
			m_g2d.drawString(t2, 10, (hm2 - TMARGIN));
		}
		m_g2d.setFont(fsmall);
		m_g2d.translate(MARGIN, MARGIN);

	}
	final BzeStrikeBounds bounds;
	private final int m_width;
	private final int m_height;
	private final BufferedImage m_bimg;
	private final Graphics2D m_g2d;
}
