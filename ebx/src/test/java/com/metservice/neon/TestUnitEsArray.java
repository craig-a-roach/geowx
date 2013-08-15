/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import org.junit.Test;

/**
 * @author roach
 */
public class TestUnitEsArray extends TestNeon {

	@Test
	public void t10_index() {
		final Expectation x = new Expectation("x1", EsPrimitiveNull.Instance, "x2", "good");
		jsassert(x, "ArrayIndex");
	}

	@Test
	public void t20_push() {
		final Expectation x = new Expectation("s1", "b.c1.e", "s2", "a,b,c,d,e,f,g,h");
		jsassert(x, "ArrayPush");
	}

	@Test
	public void t30_concat() {
		final Expectation x = new Expectation("s3", "a,b,c,d,e,f");
		jsassert(x, "ArrayConcat");
	}

	@Test
	public void t40_compact() {
		final Expectation x = new Expectation();
		x.add("s1", "a,,c,");
		x.add("s2", "a,,c");
		x.add("s3", "a,c");
		x.add("s4", "c");
		x.add("s5", "c,dd");
		jsassert(x, "ArrayCompact");
	}

	@Test
	public void t50_slice() {
		final Expectation x = new Expectation("a1_all", "a,b,c,d,e");
		x.add("a1_1", "b,c,d,e");
		x.add("a1_13", "b,c");
		x.add("a1_1m1", "b,c,d");
		x.add("a1_bad", "a,b,c,d,e");
		x.add("a1_t", "b,c,d,e");
		x.add("a1_tt", "c,d,e");
		x.add("a1_02t", "b");
		x.add("a1_e", "");
		jsassert(x, "ArraySlice");
	}

}
