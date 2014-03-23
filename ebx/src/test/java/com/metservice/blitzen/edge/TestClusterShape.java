/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.awt.Color;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestClusterShape {

	//@formatter:off
	private static final String popA =
			"   3.5:1.5,2.0,2.5,3.0,3.5"
			+"|3.0:2.0,2.5,3.0,3.5,4.0|"
			+"|2.5:1.5,2.0,2.5,3.0,3.5,4.0,4.5"
			+"|2.0:2.0,4.0,4.5,5.0"
			+"|1.5:1.5,4.0,4.5"
			+"|1.0:4.0"
			;
	
	private static final float epsB = 0.05f;
	private static final String[] popB = {
		"1341965133494,-6.3704, 152.8754,-2.0,GROUND",
		"1341976756872,-6.3213, 152.8321,-11.0,GROUND",
		"1341978101046,-6.3412, 152.9054,-5.0,GROUND",
		"1341978815425,-6.3373, 152.8744,-1.0,GROUND",
		"1341979254621,-6.3779, 152.8444,-64.0,GROUND",
		"1342013599646,-6.3079, 152.9032,-2.0,GROUND",
		 "1342014685240,-6.3908, 152.9207,-2.0,GROUND"
	};
	//@formatter:on
	private static BzeStrike[] sa(List<BzeStrike> list) {
		return list.toArray(new BzeStrike[list.size()]);
	}

	private void paint(BzeStrikeClusterShape shape, String filePrefix, BzeStrike[] strikes) {
		final BzeStrikeBounds bounds = shape.bounds();
		final TestHelpCanvas canvas = new TestHelpCanvas(bounds, 500, 400);
		final BzeStrikePolygon[] polygons = shape.polygons();
		for (int i = 0; i < polygons.length; i++) {
			canvas.plot(polygons[i], Color.cyan, null);
		}
		final BzeStrikePolyline[] polylines = shape.polylines();
		for (int i = 0; i < polylines.length; i++) {
			canvas.plot(polylines[i], Color.orange, null);
		}
		final BzeStrikeCell[] cells = shape.cells();
		for (int i = 0; i < cells.length; i++) {
			canvas.plot(cells[i], Color.green, null);
		}
		for (int i = 0; i < strikes.length; i++) {
			final BzeStrike strike = strikes[i];
			final String text = "[" + strike.x + "," + strike.y + "]";
			canvas.plot(strike, Color.darkGray, text);
		}
		canvas.save(filePrefix);
	}

	@Test
	public void a30_popA1() {
		final BzeStrike[] strikes = sa(TestHelpLoader.newListFromGenerator(popA));
		final BzeStrikeClusterShape cs = BzeStrikeClusterShape.newInstance(strikes, 0.8f);
		Assert.assertEquals(1, cs.polygons().length);
		Assert.assertEquals(0, cs.polylines().length);
		Assert.assertEquals(0, cs.cells().length);
		System.out.println(cs.polygons()[0].toString());
		paint(cs, "a30", strikes);
	}

	@Test
	public void a32_popA2() {
		final BzeStrike[] strikes = sa(TestHelpLoader.newListFromGenerator(popA));
		final BzeStrikeClusterShape cs = BzeStrikeClusterShape.newInstance(strikes, 0.5f);
		Assert.assertEquals(0, cs.polygons().length);
		Assert.assertEquals(1, cs.polylines().length);
		Assert.assertEquals(0, cs.cells().length);
		System.out.println(cs.polylines()[0].toString());
		paint(cs, "a32", strikes);
	}

	@Test
	public void a50_popB() {
		final BzeStrike[] strikes = sa(TestHelpLoader.newListFromLines(popB));
		final BzeStrikeClusterShape cs = BzeStrikeClusterShape.newInstance(strikes, epsB);
		Assert.assertEquals(1, cs.polygons().length);
		Assert.assertEquals(0, cs.polylines().length);
		Assert.assertEquals(0, cs.cells().length);
		paint(cs, "a50", strikes);
	}

}
