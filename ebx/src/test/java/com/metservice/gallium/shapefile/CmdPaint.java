/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import com.metservice.gallium.GalliumBoundingBoxD;
import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
public class CmdPaint {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: -fill|-stroke srcShapeFile dstPNG");
			return;
		}
		final boolean fill = args[0].toLowerCase().equals("-fill");
		final Path src = Paths.get(args[1]);
		final Path dst = Paths.get(args[2]);
		final ShapeReader r = new ShapeReader(src);
		final Color fg = fill ? new Color(220, 220, 180) : new Color(150, 100, 50);
		final Color bg = fill ? new Color(50, 50, 80) : new Color(200, 200, 230);
		final Canvas c = new Canvas(16000, 8000, fill, bg, fg);
		final Handler h = new Handler(c);
		try {
			r.scan(h);
			c.save(dst.toFile());
		} catch (GalliumShapefileFormatException | GalliumShapefileReadException ex) {
			ex.printStackTrace();
		}
	}

	private static class Canvas {

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
			final double xd = ((pt.x / 360.0) + 0.5) * m_width;
			return (float) xd;
		}

		public float y(GalliumPointD pt) {
			final double yd = (0.5 - (pt.y / 180.0)) * m_height;
			return (float) yd;
		}

		public Canvas(int w, int h, boolean fill, Color bg, Color fg) {
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
		private final int m_width;
		private final int m_height;
		private final boolean m_fill;

		private final BufferedImage m_bimg;
		private final Graphics2D m_g2d;
	}

	private static class Handler implements IGalliumShapefileHandler {

		@Override
		public boolean acceptFile(Path path, long bcPayload) {
			return true;
		}

		@Override
		public boolean acceptHeader(GalliumShapefileHeader header) {
			System.out.println(header);
			return true;
		}

		@Override
		public IGalliumShapefileMultiPoint createMultiPoint(int recNo, GalliumBoundingBoxD box, int pointCount) {
			return null;
		}

		@Override
		public IGalliumShapefilePolygon createPolygon(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount) {
			return new Polygon(m_canvas);
		}

		@Override
		public IGalliumShapefilePolyLine createPolyLine(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount) {
			return null;
		}

		@Override
		public void point(int recNo, GalliumPointD pt) {
		}

		public Handler(Canvas canvas) {
			m_canvas = canvas;
		}
		private final Canvas m_canvas;
	}

	private static class Polygon implements IGalliumShapefilePolygon {

		@Override
		public IGalliumShapefilePolygonPart createPart(int partIndex, int pointCount) {
			return new PolygonPart(m_canvas, pointCount);
		}

		public Polygon(Canvas canvas) {
			m_canvas = canvas;
		}
		private final Canvas m_canvas;
	}

	private static class PolygonPart implements IGalliumShapefilePolygonPart {

		@Override
		public void close() {
			m_path.closePath();
			m_canvas.draw(m_path);
		}

		@Override
		public void vertex(int vertexIndex, GalliumPointD pt) {
			final float x = m_canvas.x(pt);
			final float y = m_canvas.y(pt);
			if (vertexIndex == 0) {
				m_path.moveTo(x, y);
			} else {
				m_path.lineTo(x, y);
			}
		}

		public PolygonPart(Canvas canvas, int pointCount) {
			m_canvas = canvas;
			m_path = new Path2D.Float(Path2D.WIND_NON_ZERO, pointCount);
		}
		private final Canvas m_canvas;
		private final Path2D.Float m_path;
	}

}
