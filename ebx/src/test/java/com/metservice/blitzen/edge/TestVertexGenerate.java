/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import org.junit.Test;

/**
 * @author roach
 */
public class TestVertexGenerate {

	//@formatter:off
	private static final String[] BM1 = {
		"======X",
		"=====XXX",
		"==XXXXXXX",
		"==XX====XXX",
		"==XX===XX==X",
		"=XXX==XX",
		"XXX===X",
		"===X"
	};
	//@formatter:on

	@Test
	public void t50() {
		final BitMesh bm = TestHelpLoader.newBitMeshFromLines(BM1, 'X');
		System.out.println(bm);
		final VertexGenerator vg = new VertexGenerator(bm);
		vg.newShape();
	}

}
