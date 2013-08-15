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
public class TestUnitEsBinary extends TestNeon {

	@Test
	public void t50_resourceLoad() {
		final Expectation x = new Expectation("ab0", "s", "ab1", "", "ab2", "", "ab3", EsPrimitiveUndefined.Instance);
		x.add("#bb0", 4);
		x.add("bb1", "Y");
		x.add("#bb2", 122);
		x.add("bb3", "1a2");
		x.add("bb4", EsPrimitiveNull.Instance);
		jsassert(x, "BinaryLoad");
	}
}
