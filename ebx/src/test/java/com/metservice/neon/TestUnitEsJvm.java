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
public class TestUnitEsJvm extends TestNeon {

	@Test
	public void t10_Level01() {
		final Expectation x = new Expectation("#xa", 7, "xb", "true:abc:3.14");
		jsassert(x, "Jvm.Level01");
	}

	@Test
	public void t20_Level02() {
		final Expectation x = new Expectation();
		x.expectFail(true);
		jsassert(x, "Jvm.Level02");
	}
}
