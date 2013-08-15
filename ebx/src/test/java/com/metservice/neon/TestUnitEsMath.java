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
public class TestUnitEsMath extends TestNeon {

	@Test
	public void t50_round() {
		final Expectation x = new Expectation();
		x.add("#step01", 24);
		x.add("#step02", 24);
		x.add("#step03", 24);
		x.add("#step04", 30);
		x.add("step11", "1d");
		x.add("step12", "1d");
		x.add("step13", "1d");
		x.add("step14", "30h");
		x.add("r01", "1d");
		x.add("r02", "1d");
		x.add("r03", "1d");
		x.add("r04", "30h");

		jsassert(x, "MathRound");
	}
}
