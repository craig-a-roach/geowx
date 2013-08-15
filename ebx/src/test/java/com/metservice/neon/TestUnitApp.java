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
public class TestUnitApp extends TestNeon {

	@Test
	public void t85_AppB() {
		final Expectation x = new Expectation();
		x.add("a1", "6h", "a2", "12h", "a3", "18h", "a4", "18h");
		x.add("a10", 0, "a11", 1, "a12", 2, "a13", 3, "a14", 4);
		x.add("b1", "3h", "b2", "6h", "b3", "9h", "b4", "12h", "b5", "18h", "b6", "1d", "b7", "1d");
		x.add("c1", 7, "c2", 6, "c3", 5, "c4", 4, "c5", 3, "c6", 2, "c7", 1, "c8", 0, "c9", 1, "c10", 0, "c11", 0);
		x.add("k1s", "0 18h 1d");
		jsassert(x, "AppB");
	}

	public void t90_AppA() {
		final Expectation x = new Expectation();
		x.add("pgAs1", new Resource("AppA.01.txt"));
		x.add("pgAs2", new Resource("AppA.02.txt"));
		x.add("pgAs3", "");
		x.add("pgBs1", new Resource("AppA.10.txt"));
		x.add("pgBs2", new Resource("AppA.11.txt"));
		x.add("pgCs1", new Resource("AppA.20.txt"));
		jsassert(x, "AppA");
	}
}
