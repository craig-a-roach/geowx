/*
 * Copyright 2014 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.blitzen.edge;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author roach
 */
public class TestVertexGenerate {

	//@formatter:off
	private static final String[] BM1 = {
		"======X",
		"X==XXXXX",
		"==XXXXXXXX",
		"=XXX====XXXXX",
		"=XXX=X=XXXXXXX",
		"=XXX===XXX==XX",
		"XXX===XXX",
		"==XX====XX"
	};
	private static final String[] BM2 = {
		"X=====X",
		"===XX",
		"==X===X",
		"=X===X"
	};
	
	private static final String[] BM3 = {
	"=X==",
	"XXX=",
	"=XXX",
	"=XXX",
	"XXXX"
	};
	//@formatter:on

	@Test
	public void a10() {
		final BitMesh bm = TestHelpLoader.newBitMeshFromLines(BM3, 'X');
		System.out.println(bm);
		final VertexGenerator vg = new VertexGenerator(bm);
		final List<IPolyline> pl = vg.newShape();
		Assert.assertEquals(1, pl.size());
		Assert.assertEquals("[(0,0)(1,1)(1,2)(0,3)(1,4)(3,2)(3,0)CLOSE]", pl.get(0).toString());
	}

	@Test
	public void t50() {
		final BitMesh bm = TestHelpLoader.newBitMeshFromLines(BM1, 'X');
		System.out.println(bm);
		final VertexGenerator vg = new VertexGenerator(bm);
		final List<IPolyline> pl = vg.newShape();
		Assert.assertEquals(3, pl.size());
		final String p0 = "[(0,1)(1,2)(1,4)(3,6)(5,6)(6,7)(8,5)(9,5)(10,4)(12,4)(13,3)(13,2)(12,2)(11,3)(10,3)(8,1)(9,0)(8,0)(7,1)(6,1)(7,2)(7,3)(8,4)(7,5)(4,5)(3,4)(3,2)(2,1)(3,0)(2,0)(1,1)CLOSE]";
		final String p1 = "(0,6)";
		final String p2 = "(5,3)";
		Assert.assertEquals(p0, pl.get(0).toString());
		Assert.assertEquals(p1, pl.get(1).toString());
		Assert.assertEquals(p2, pl.get(2).toString());
	}

	@Test
	public void t60() {
		final BitMesh bm = TestHelpLoader.newBitMeshFromLines(BM2, 'X');
		System.out.println(bm);
		final VertexGenerator vg = new VertexGenerator(bm);
		final List<IPolyline> pl = vg.newShape();
		Assert.assertEquals(4, pl.size());
		Assert.assertEquals("(0,3)", pl.get(0).toString());
		Assert.assertEquals("[(1,0)(3,2)(4,2)]", pl.get(1).toString());
		Assert.assertEquals("[(5,0)(6,1)]", pl.get(2).toString());
		Assert.assertEquals("(6,3)", pl.get(3).toString());
	}

}
