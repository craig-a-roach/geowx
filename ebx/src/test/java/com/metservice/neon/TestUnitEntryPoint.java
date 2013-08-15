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
public class TestUnitEntryPoint extends TestNeon {

	@Test
	public void t10_Level01() {
		final Expectation xa = new Expectation("#return", 5);
		final Expectation xb = new Expectation("return", "one");
		final Expectation xc = new Expectation("#return", 15);
		final IEsOperand[] zptArgs = { new EsPrimitiveNumberInteger(3), new EsPrimitiveNumberInteger(2) };
		final EsCallableEntryPoint epa = EsCallableEntryPoint.newFixed("fa", zptArgs);
		final EsCallableEntryPoint epb = EsCallableEntryPoint.newInstance("fb", null, zptArgs);
		final EsCallableEntryPoint epc = EsCallableEntryPoint.newFixed("fc", zptArgs);
		jsassert(xa, "EntryPoint", epa);
		jsassert(xb, "EntryPoint", epb);
		jsassert(xc, "EntryPoint", epc);
	}
}
