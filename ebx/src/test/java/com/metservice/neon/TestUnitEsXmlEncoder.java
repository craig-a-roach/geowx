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
public class TestUnitEsXmlEncoder extends TestNeon {

	@Test
	public void t01_Level00() {
		final Expectation x = new Expectation("sx", new Resource("XmlEncoder.Level00.txt"));
		jsassert(x, "XmlEncoder.Level00");
	}

	@Test
	public void t10_Level01() {
		final Expectation x = new Expectation("sx", new Resource("XmlEncoder.Level01.txt"));
		jsassert(x, "XmlEncoder.Level01");
	}

	@Test
	public void t20_Level02() {
		final Expectation x = new Expectation("sx", new Resource("XmlEncoder.Level02.xml"));
		jsassert(x, "XmlEncoder.Level02");
	}

	@Test
	public void t30_Level03() {
		final Expectation x = new Expectation("sx", new Resource("XmlEncoder.Level03.txt"));
		jsassert(x, "XmlEncoder.Level03");
	}

	@Test
	public void t50_VBaggingJob() {
		final Expectation x = new Expectation("sx", new Resource("XmlEncoder.VBaggingJob.xml"));
		jsassert(x, "XmlEncoder.VBaggingJob");
	}

}
