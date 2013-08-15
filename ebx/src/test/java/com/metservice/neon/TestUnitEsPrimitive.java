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
public class TestUnitEsPrimitive extends TestNeon {

	@Test
	public void t30_PrimitiveBasic() {
		final Expectation x = new Expectation();
		x.add("ou", "missing", "qu", "missing");
		x.add("ox", "have", "qx", "missing");
		x.add("os", "have", "qs", "missing");
		x.add("oz", "have", "qz", "missing");
		x.add("od", "have", "qd", "have");
		x.add("oF1", "have", "qF1", "have");
		x.add("oF2", "missing", "qF2", "missing");
		x.add("VA1", "default 2", "VA2", "NULL", "VA3", "0");
		jsassert(x, "PrimitiveBasic");
	}

	@Test
	public void t40_PrimitiveTime() {
		final Expectation x = new Expectation();
		x.add("t0", 0);
		x.add("n1", 43);
		x.add("t2", -1);
		x.add("n4", "90m");
		jsassert(x, "PrimitiveNumber");
	}

	@Test
	public void t50_PrimitiveTime() {
		final Expectation x = new Expectation("e21", "195m", "d21h", "3.25", "i21q", "13");
		x.add("qtr", "15m", "bq1", "true", "bq2", "true");
		x.add("t3", "20100409T0630Z00M000");
		x.add("t4", "20100409T0630Z00M000");
		x.add("b3", "true", "b4", "true");
		x.add("yy", "400d");
		x.add("p1", "4");
		x.add("p2", "3323.076923076923");
		jsassert(x, "PrimitiveTime");
	}

	@Test
	public void t55_PrimitiveTimefactors() {
		final Expectation x = new Expectation();
		x.add("e1", "true");
		x.add("e2", "false");
		x.add("e3", "false");
		x.add("fa6", "20100409 0600.00");
		jsassert(x, "PrimitiveTimefactors");
	}
}
