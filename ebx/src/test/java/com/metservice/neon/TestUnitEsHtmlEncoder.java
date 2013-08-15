/*
 * Copyright 2011 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.neon;

import org.junit.Test;

/**
 * @author roach
 */
public class TestUnitEsHtmlEncoder extends TestNeon {

	@Test
	public void t10_Level01() {
		final Expectation x = new Expectation("sh", new Resource("HtmlEncoder.Level01.txt"));
		jsassert(x, "HtmlEncoder.Level01");
	}

	@Test
	public void t20_Level02() {
		final Expectation x = new Expectation("sh", new Resource("HtmlEncoder.Level02.txt"));
		jsassert(x, "HtmlEncoder.Level02");
	}

	@Test
	public void t30_Level03() {
		final Expectation x = new Expectation("sh", new Resource("HtmlEncoder.Level03.txt"));
		jsassert(x, "HtmlEncoder.Level03");
	}

	@Test
	public void t40_Level04() {
		final Expectation x = new Expectation();
		x.add("s1", "<A href=\"/alpha\">a1</A>");
		x.add("s2", "<A href=\"/alpha?f=V%26F&amp;g=V+G&amp;h=H%3A1&amp;h=H%2F2\">a2</A>");
		x.add("s3", "<A href=\"/alpha?y\">a3</A>");
		x.add("s4", "<A href=\"/alpha#beta\">a4</A>");
		x.add("s5", "<A href=\"http://webserv1:8080/alpha%20beta\">a5</A>");
		jsassert(x, "HtmlEncoder.Level04");
	}

	@Test
	public void t50_Level05() {
		final Expectation x = new Expectation();
		x.add("x1", new Resource("HtmlEncoder.Level05.1.txt"));
		x.add("x2", new Resource("HtmlEncoder.Level05.2.txt"));
		jsassert(x, "HtmlEncoder.Level05");
	}
}
