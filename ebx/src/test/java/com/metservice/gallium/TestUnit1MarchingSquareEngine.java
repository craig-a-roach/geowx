/*
 * Copyright 2013 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.gallium;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestUnit1MarchingSquareEngine {

	private void assertBounds(GalliumBoundingBoxF oBounds, float yLo, float xLo, float yHi, float xHi) {
		Assert.assertNotNull("has bounds", oBounds);
		final GalliumBoundingBoxF expected = GalliumBoundingBoxF.newCorners(yLo, xLo, yHi, xHi);
		final float delta = Math.ulp(1.0f);
		if (!expected.similarTo(oBounds, delta)) {
			System.err.println("Unexpected bounding box");
			System.err.println("Actual: " + oBounds);
			System.err.println("Expected: " + expected);
		}
		Assert.assertEquals("Bounds.yLo", yLo, oBounds.yLo(), delta);
		Assert.assertEquals("Bounds.xLo", xLo, oBounds.xLo(), delta);
		Assert.assertEquals("Bounds.yHi", yHi, oBounds.yHi(), delta);
		Assert.assertEquals("Bounds.xHi", xHi, oBounds.xHi(), delta);
	}

	private void assertLevels(GalliumTopology t, float[] expectedThresholds, String[] expectedPoints) {
		if (t == null) throw new IllegalArgumentException("object is null");
		if (expectedThresholds == null) throw new IllegalArgumentException("object is null");
		if (expectedPoints == null) throw new IllegalArgumentException("object is null");
		if (expectedThresholds.length != expectedPoints.length) throw new IllegalArgumentException("expected mismatch");
		final GalliumTopologyLevel[] zptLevelAsc = t.zptLevelAsc();
		if (expectedThresholds.length != zptLevelAsc.length) {
			System.err.println("Unexpected levels");
			for (int i = 0; i < zptLevelAsc.length; i++) {
				final GalliumTopologyLevel level = zptLevelAsc[i];
				System.err.println("Level " + i + " threshold " + level.threshold());
				System.err.println(level.describePoints());
			}
		}
		Assert.assertEquals("Level count", expectedThresholds.length, zptLevelAsc.length);
		for (int i = 0; i < zptLevelAsc.length; i++) {
			final GalliumTopologyLevel level = zptLevelAsc[i];
			final float expThreshold = expectedThresholds[i];
			Assert.assertEquals("Threshold " + i, expThreshold, level.threshold(), Math.ulp(1.0f));
			final String expLevel = expectedPoints[i];
			final String actLevel = level.describePoints();
			if (!expLevel.equals(actLevel)) {
				System.err.println("Level " + expThreshold + " poly mismatch");
				System.err.println("Actual: " + actLevel);
				System.err.println("Expected: " + expLevel);
			}
			Assert.assertEquals("Level " + i, expLevel, actLevel);
		}
	}

	@Test
	public void t05_imp() {

		//@formatter:off
		final float[][] matrix = {
				{ 0.0f, 1.0f, 1.0f, 3.0f, 2.0f },
				{ 1.0f, 3.0f, 6.0f, 6.0f, 3.0f },
				{ 3.0f, 7.0f, 9.0f, 7.0f, 3.0f } 
				};
		//@formatter:on
		final TestImpContourable src = TestImpContourable.newInstance(30.0f, 5.0f, 1.0f, matrix);
		Assert.assertEquals(30.0f, src.latitude(0), 0.01f);
		Assert.assertEquals(31.0f, src.latitude(1), 0.01f);
		Assert.assertEquals(32.0f, src.latitude(2), 0.01f);
		Assert.assertEquals(5.0f, src.longitude(0), 0.01f);
		Assert.assertEquals(6.0f, src.longitude(1), 0.01f);
		Assert.assertEquals(7.0f, src.longitude(2), 0.01f);
		Assert.assertEquals(8.0f, src.longitude(3), 0.01f);
		Assert.assertEquals(6.0f, src.datum(1, 2), 0.01f);
		Assert.assertEquals(9.0f, src.datum(0, 2), 0.01f);
	}

	@Test
	public void t10_islands() {
		//@formatter:off
		final float[][] matrix = {
				{ 0.0f, 0.0f, 0.0f, 0.0f },
				{ 0.0f, 1.0f, 0.0f, 0.0f },
				{ 0.0f, 0.0f, 1.0f, 0.0f },
				{ 0.0f, 0.0f, 0.0f, 0.0f }
				};
		//@formatter:on
		final TestImpContourable src = TestImpContourable.newInstance(30.0f, 5.0f, 1.0f, matrix);
		final float[] thresholds = { 0.8f };
		final IGalliumContourEngine e = GalliumContourEngineFactory.newMovingSquare(src);
		final GalliumTopology t = e.newTopology(thresholds);
		assertBounds(t.getBounds(), 30.8f, 5.8f, 32.2f, 7.2f);
		final String[] xlevels = { "(31.0,6.8)(31.2,7.0)(31.0,7.2)(30.8,7.0)CLOSE\n(32.0,5.8)(32.2,6.0)(32.0,6.2)(31.8,6.0)CLOSE" };
		assertLevels(t, thresholds, xlevels);
	}

	@Test
	public void t15_bridge() {
		//@formatter:off
		final float[][] matrix = {
				{ 0.0f, 0.0f, 0.0f, 0.0f },
				{ 0.0f, 1.0f, 0.0f, 0.0f },
				{ 0.0f, 0.0f, 1.0f, 0.0f },
				{ 0.0f, 0.0f, 0.0f, 0.0f }
				};
		//@formatter:on
		final TestImpContourable src = TestImpContourable.newInstance(30.0f, 5.0f, 1.0f, matrix);
		final float[] thresholds = { 0.2f };
		final IGalliumContourEngine e = GalliumContourEngineFactory.newMovingSquare(src);
		final GalliumTopology t = e.newTopology(thresholds);
		assertBounds(t.getBounds(), 30.2f, 5.2f, 32.8f, 7.8f);
		final String[] xlevels = { "(31.0,6.2)(31.2,6.0)(32.0,5.2)(32.8,6.0)(32.0,6.8)(31.8,7.0)(31.0,7.8)(30.2,7.0)CLOSE" };
		assertLevels(t, thresholds, xlevels);
	}

	@Test
	public void t20_saddle() {
		//@formatter:off
		final float[][] matrix = {
				{ 0.0f, 0.0f, 0.0f },
				{ 0.0f, 1.0f, 0.0f },
				{ 0.6f, 0.0f, 1.0f }
				};
		//@formatter:on
		final TestImpContourable src = TestImpContourable.newInstance(30.0f, 5.0f, 1.0f, matrix);
		final float[] thresholds = { 0.8f };
		final IGalliumContourEngine e = GalliumContourEngineFactory.newMovingSquare(src);
		final GalliumTopology t = e.newTopology(thresholds);
		assertBounds(t.getBounds(), 30.0f, 5.8f, 31.2f, 7.0f);
		final String[] xlevels = { "(31.0,5.8)(31.2,6.0)(31.0,6.2)(30.8,6.0)CLOSE\n(30.0,6.8)(30.2,7.0)" };
		assertLevels(t, thresholds, xlevels);
	}

	@Test
	public void t35_crater() {
		//@formatter:off
		final float[][] matrix = {
				{ 0.0f, 0.0f, 0.0f, 0.0f, 0.0f },
				{ 0.0f, 2.0f, 2.0f, 2.0f , 0.0f},
				{ 0.0f, 2.0f, 1.0f, 2.0f, 0.0f },
				{ 0.0f, 2.0f, 2.0f, 2.0f, 0.0f },
				{ 0.0f, 0.0f, 0.0f, 0.0f, 0.0f }
				};
		//@formatter:on
		final TestImpContourable src = TestImpContourable.newInstance(30.0f, 5.0f, 1.0f, matrix);
		final float[] thresholds = { 0.8f, 1.8f };
		final IGalliumContourEngine e = GalliumContourEngineFactory.newMovingSquare(src);
		final GalliumTopology t = e.newTopology(thresholds);
		assertBounds(t.getBounds(), 30.4f, 5.4f, 33.6f, 8.6f);
		final String[] xlevels = {
				"(31.0,5.4)(32.0,5.4)(33.0,5.4)(33.6,6.0)(33.6,7.0)(33.6,8.0)(33.0,8.6)(32.0,8.6)(31.0,8.6)(30.4,8.0)(30.4,7.0)(30.4,6.0)CLOSE",
				"(31.0,5.9)(32.0,5.9)(33.0,5.9)(33.1,6.0)(33.1,7.0)(33.1,8.0)(33.0,8.1)(32.0,8.1)(31.0,8.1)(30.9,8.0)(30.9,7.0)(30.9,6.0)CLOSE"
						+ "\n(32.0,6.2)(32.8,7.0)(32.0,7.8)(31.2,7.0)CLOSE" };
		assertLevels(t, thresholds, xlevels);
	}

	@Test
	public void t40_textbook() {
		//@formatter:off
		final float[][] matrix = {
				{ 0.0f, 1.0f, 1.0f, 3.0f, 2.0f, 0.0f },
				{ 1.0f, 3.0f, 6.0f, 6.0f, 3.0f, 0.0f },
				{ 3.0f, 7.0f, 9.0f, 7.0f, 3.0f, 0.0f }, 
				{ 2.0f, 7.0f, 8.0f, 6.0f, 2.0f, 1.0f }, 
				{ 1.0f, 2.0f, 3.0f, 4.0f, 7.0f,  6.0f} 
				};
		//@formatter:on
		final TestImpContourable src = TestImpContourable.newInstance(30.0f, 5.0f, 1.0f, matrix);
		final float[] thresholds = { 1.8f, 4.8f, 7.5f, 9.6f };
		final IGalliumContourEngine e = GalliumContourEngineFactory.newMovingSquare(src);
		final GalliumTopology t = e.newTopology(thresholds);
		assertBounds(t.getBounds(), 30.0f, 5.0f, 34.0f, 10.0f);
		final String[] xlevels = {
				"(34.0,9.1)(33.0,9.4)(32.0,9.4)(31.0,9.2)(30.84,10.0)"
						+ "\n(32.6,5.0)(33.0,5.4)(33.6,6.0)(33.84,7.0)(34.0,7.4)" + "\n(30.8,5.0)(30.0,5.8)",
				"(31.0,5.56)(32.0,5.45)(32.55,6.0)(33.0,6.6)(33.24,7.0)(33.4,8.0)(33.0,8.4)(32.0,8.55)(31.0,8.3)(30.4,8.0)(30.36,7.0)(30.56,6.0)CLOSE"
						+ "\n(30.0,8.266666)(30.44,9.0)(30.24,10.0)",
				"(31.0,6.5)(32.0,6.25)(32.5,7.0)(32.0,7.75)(31.0,7.25)(30.9,7.0)CLOSE", "" };
		assertLevels(t, thresholds, xlevels);
	}

	@Test
	public void t50_circle() {
		//@formatter:off
		final float[][] matrix = {
				{ 0.0f, 0.0f, 0.0f },
				{ 0.0f, 1.0f, 0.0f },
				{ 0.0f, 0.0f, 0.0f }
				};
		//@formatter:on
		final TestImpContourable src = TestImpContourable.newInstance(30.0f, 5.0f, 1.0f, matrix);
		final float[] thresholds = { 0.8f };
		final IGalliumContourEngine e = GalliumContourEngineFactory.newMovingSquare(src);
		final GalliumTopology t = e.newTopology(thresholds);
		assertBounds(t.getBounds(), 30.8f, 5.8f, 31.2f, 6.2f);
		final String[] xlevels = { "(31.0,5.8)(31.2,6.0)(31.0,6.2)(30.8,6.0)CLOSE" };
		assertLevels(t, thresholds, xlevels);
	}

	@Test
	public void t55_ellipse() {
		//@formatter:off
		final float[][] matrix = {
				{ 0.0f, 1.0f, 0.0f },
				{ 2.0f, 4.0f, 1.0f },
				{ 0.0f, 5.0f, 1.0f },
				{ 2.0f, 3.0f, 1.0f },
				{ 1.0f, 1.0f, 1.0f }
				};
		//@formatter:on
		final TestImpContourable src = TestImpContourable.newInstance(30.0f, 5.0f, 1.0f, matrix);
		final float[] thresholds = { 2.5f };
		final IGalliumContourEngine e = GalliumContourEngineFactory.newMovingSquare(src);
		final GalliumTopology t = e.newTopology(thresholds);
		assertBounds(t.getBounds(), 30.75f, 5.25f, 33.5f, 6.625f);
		final String[] xlevels = { "(31.0,5.5)(32.0,5.5)(33.0,5.25)(33.5,6.0)(33.0,6.5)(32.0,6.625)(31.0,6.25)(30.75,6.0)CLOSE" };
		assertLevels(t, thresholds, xlevels);
	}

	@Test
	public void t60_bang() {
		//@formatter:off
		final float[][] matrix = {
				{ 5.0f, 1.0f, 1.0f, 5.0f },
				{ 1.0f, 0.0f, 0.0f, 1.0f },
				{ 2.0f, 1.0f, 1.0f, 5.0f },
				{ 6.0f, 2.0f, 1.0f, 6.0f }
				};
		//@formatter:on
		final float[] thresholds = { 1.5f };
		final TestImpContourable src = TestImpContourable.newInstance(30.0f, 5.0f, 1.0f, matrix);
		final IGalliumContourEngine e = GalliumContourEngineFactory.newMovingSquare(src);
		final GalliumTopology t = e.newTopology(thresholds);
		assertBounds(t.getBounds(), 30.0f, 5.0f, 33.0f, 8.0f);
		final String[] xlevels = { "(31.5,5.0)(31.0,5.5)(30.5,6.0)(30.0,6.5)\n(31.875,8.0)(31.0,7.125)(30.0,7.1)\n(32.125,5.0)(33.0,5.875)\n(33.0,7.125)(32.125,8.0)" };
		assertLevels(t, thresholds, xlevels);
	}

}
