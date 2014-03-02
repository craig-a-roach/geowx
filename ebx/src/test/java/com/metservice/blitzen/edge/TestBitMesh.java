/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestBitMesh {

	@Test
	public void t10() {
		final BitMesh bm = new BitMesh(200, 50);
		bm.set(190, 48, true);
		bm.set(190, 49, true);
		bm.set(190, 47, true);
		bm.set(0, 0, true);
		bm.set(1, 0, true);
		bm.set(0, 1, true);
		bm.set(1, 1, true);
		Assert.assertTrue("190,48", bm.value(190, 48));
		bm.set(190, 48, false);
		Assert.assertFalse("190,48", bm.value(190, 48));
		Assert.assertTrue("0,1", bm.value(0, 1));
		bm.set(0, 1, false);
		Assert.assertFalse("190,48", bm.value(0, 1));
		bm.set(199, 49, true);
		Assert.assertTrue("199,49", bm.value(199, 49));
	}

	@Test
	public void t20() {
		final BitMesh bm = new BitMesh(73, 10);
		bm.set(70, 7, true);
		bm.set(71, 7, true);
		bm.set(69, 7, true);
		bm.set(70, 8, true);
		bm.set(70, 6, true);
		System.out.println("Pre");
		System.out.println(bm);
		bm.set(70, 7, false);
		System.out.println("Post");
		System.out.println(bm);
	}

}
