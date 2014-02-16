/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.aggregator;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestStrikeTree {

	//@formatter:off
	private static final String[] popA = {
		"1000,5,4,  10,GROUND",
		"1001,1,1,  10,GROUND",
		"1002,6,4,  10,GROUND",
		"1003,6,5,  10,GROUND",
		"1004,4,1,  10,GROUND",
		"1005,7,5,  10,GROUND",
		"1006,7,4,  10,GROUND",
		"1007,6,3,  10,GROUND",
		"1008,2,3,  10,GROUND",
		"1009,4,2,  10,GROUND",
		"1010,4,4,  10,GROUND",
		"1011,4,3,  10,GROUND",
		"1012,5,5,  10,GROUND"
		};
	//@formatter:on

	private static final String[] popB = { "1341964827332,-5.0799,152.8558,-5,GROUND",
			"1341964865803,-5.2584,152.8397,-2,GROUND", "1341964868601,-39.0116,154.2394,0,CLOUD",
			"1341964925321,-38.9847,154.1223,6,GROUND", "1341964925335,-38.9962,154.191,0,GROUND",
			"1341964938907,-6.8852,152.463,-2,GROUND", "1341964939069,-6.5587,152.561,-10,GROUND",
			"1341964960250,-6.7375,153.1356,-1,GROUND", "1341964982314,-39.0316,154.2593,0,CLOUD",
			"1341964982328,-39.0137,154.1638,-3,GROUND" };

	private static void print(Strike[] strikes, int[] ids) {
		for (int i = 0; i < ids.length; i++) {
			final int sid = ids[i];
			final Strike strike = strikes[sid];
			System.out.println("sid=" + sid + " " + strikes);
		}
	}

	@Test
	public void t10_popA() {
		final Strike[] strikes = TestHelpLoader.newArrayFromLines(popA);
		final StrikeTree oTree = StrikeTree.newInstance(strikes, 3);
		System.out.println(oTree);
		Assert.assertNotNull(oTree);
		final int[] zlMatch1 = oTree.query(strikes, 0, 1.0f);
		print(strikes, zlMatch1);
		final int[] zlMatch2 = oTree.query(strikes, 2, 1.0f);
		print(strikes, zlMatch2);
	}

	@Test
	public void t10_popB() {
		final Strike[] strikes = TestHelpLoader.newArrayFromLines(popB);
		final StrikeTree oTree = StrikeTree.newInstance(strikes, 3);
		System.out.println(oTree);
	}
}
