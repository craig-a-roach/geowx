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

	@Test
	public void a10_gshhs() {
		Path src = null;
		try {
			src = newGHHS("c", 2);
			final ShapeReader r = new ShapeReader(src, 3);
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
		public boolean acceptPolygon(int recNo, GalliumBoundingBoxD box, int partCount, int pointCount) {
			System.out.println("Accept polygon " + recNo + " box " + box + " parts=" + partCount + ", points=" + pointCount);
			return true;
		}

		@Override
		public void point(int recNo, GalliumPointD pt) {
			System.out.println("Point " + recNo + ":" + pt);
		}

		@Override
		public void polygon(int recNo, int partIndex, int pointIndex, GalliumPointD pt) {
			System.out.println("Rec " + recNo + " part " + partIndex + " point " + pointIndex + "=" + pt);
		}

		public Handler() {
		}
	}

}
