/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import com.metservice.gallium.GalliumBoundingBoxD;
import com.metservice.gallium.GalliumPointD;

/**
 * @author roach
 */
public class TestUnit1Parse {

	private Path newGHHS(String res, int level) {
		return TestFolder.Instance.newFile("GSHHS_" + res + "_L" + level + ".shp");
	}

	private Path newWDBII_river(String res, int level) {
		return TestFolder.Instance.newFile("WDBII_river_" + res + "_L" + level + ".shp");
	}

	@Test
	public void a10_gshhs() {
		Path src = null;
		try {
			src = newGHHS("c", 1);
			final ShapeReader r = new ShapeReader(src);
			final Handler h = new Handler();
			r.scan(h);
		} catch (final GalliumShapefileFormatException ex) {
			Assert.fail("Format exception: " + ex.getMessage());
		} catch (final GalliumShapefileReadException ex) {
			Assert.fail("Read exception: " + ex.getMessage());
		} finally {
			TestFolder.Instance.scrub(src);
		}
	}

	@Test
	public void a20_river() {
		Path src = null;
		try {
			src = newWDBII_river("c", 1);
			final ShapeReader r = new ShapeReader(src);
			final Handler h = new Handler();
			r.scan(h);
		} catch (final GalliumShapefileFormatException ex) {
			Assert.fail("Format exception: " + ex.getMessage());
		} catch (final GalliumShapefileReadException ex) {
			Assert.fail("Read exception: " + ex.getMessage());
		} finally {
			TestFolder.Instance.scrub(src);
		}
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
			System.out.println("Polygon record " + recNo + " :" + box);
			return new Polygon();
		}

		@Override
		public IGalliumShapefilePolyLine createPolyLine(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount) {
			System.out.println("PolyLine record " + recNo + " :" + box);
			return new PolyLine();
		}

		@Override
		public void point(int recNo, GalliumPointD pt) {
			System.out.println("Point " + recNo + ":" + pt);
		}

		public Handler() {
		}
	}

	private static class Polygon implements IGalliumShapefilePolygon {

		@Override
		public IGalliumShapefilePolygonPart createPart(int partIndex, int pointCount) {
			System.out.println("Part " + partIndex + ": " + pointCount + " vertices");
			return new PolygonPart();
		}
	}

	private static class PolygonPart implements IGalliumShapefilePolygonPart {

		@Override
		public void close() {
			System.out.println("Part CLOSE");
		}

		@Override
		public void vertex(int vertexIndex, GalliumPointD pt) {
			System.out.println("Part vertex " + vertexIndex + ":" + pt);
		}
	}

	private static class PolyLine implements IGalliumShapefilePolyLine {

		@Override
		public IGalliumShapefilePolyLinePart createPart(int partIndex, int pointCount) {
			System.out.println("Part " + partIndex + ": " + pointCount + " vertices");
			return new PolyLinePart();
		}
	}

	private static class PolyLinePart implements IGalliumShapefilePolyLinePart {

		@Override
		public void end() {
			System.out.println("Part END");
		}

		@Override
		public void vertex(int vertexIndex, GalliumPointD pt) {
			System.out.println("Part vertex " + vertexIndex + ":" + pt);
		}
	}
}
