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
public class TestUnitArgs extends TestNeon {

	@Test
	public void t50_ArgsVar() {
		final Expectation x = new Expectation("s1", "a,b,c,d,e");
		jsassert(x, "ArgsVar");
	}
}
