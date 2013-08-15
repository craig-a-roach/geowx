/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import org.junit.Test;

import com.metservice.argon.json.JsonArray;
import com.metservice.argon.json.JsonObject;

/**
 * @author roach
 */
public class TestUnitEsControl extends TestNeon {

	@Test
	public void t30_function() {
		final Expectation x = new Expectation("p1a", "1h", "p1b", "1h");
		x.add("c1", "17", "c2", "20", "c3", "20");
		jsassert(x, "ControlFunction");
	}

	@Test
	public void t40_loop() {
		final String ys = "b:p:echo,b:p:fox,b:p:golf" + ",b:q:echo,b:q:fox,b:q:golf" + ",b:r:echo,b:r:fox,b:r:golf"
				+ ",c:p:echo,c:p:fox,c:p:golf" + ",c:q:echo,c:q:fox,c:q:golf" + ",c:r:echo,c:r:fox,c:r:golf"
				+ ",a:p:echo,a:p:fox,a:p:golf" + ",a:q:echo,a:q:fox,a:q:golf" + ",a:r:echo,a:r:fox,a:r:golf";
		final Expectation x = new Expectation("ys", ys);
		jsassert(x, "ControlLoop");
	}

	@Test
	public void t50_throw() {
		final Expectation x = new Expectation("y1", "6", "throw", "Value of x (-5) is non-positive");
		x.add("return", EsPrimitiveUndefined.Instance);
		jsassert(x, "ControlThrow");
	}

	@Test
	public void t60_return() {
		final JsonObject c0 = JsonObject.newMutable();
		c0.putInteger("ca", 3);
		c0.putElapsedMs("cb", 4000L);
		final JsonObject c1 = JsonObject.newMutable();
		c1.putInteger("ca", 4);
		c1.putElapsedMs("cb", 5000L);
		final JsonArray c = JsonArray.newMutable();
		c.add(c0);
		c.add(c1);
		final JsonObject expected = JsonObject.newMutable();
		expected.putString("a", "A");
		expected.putBoolean("b", true);
		expected.put("c", c);

		final Expectation x = new Expectation("@return", expected);
		jsassert(x, "ControlReturn");
	}

}
