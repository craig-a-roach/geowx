/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium.shapefile;

import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

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
			r.scan();
		} catch (final GalliumShapefileFormatException ex) {
			Assert.fail("Format exception: " + ex.getMessage());
		} catch (final GalliumShapefileReadException ex) {
			Assert.fail("Read exception: " + ex.getMessage());
		} finally {
			TestFolder.Instance.scrub(src);
		}
	}

}
