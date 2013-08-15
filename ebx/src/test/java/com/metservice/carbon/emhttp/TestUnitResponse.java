/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.carbon.emhttp;

import org.eclipse.jetty.server.Request;
import org.junit.Test;

import com.metservice.carbon.TestNeon;

/**
 * @author roach
 */
public class TestUnitResponse extends TestNeon {

	@Test
	public void t40_html1() {
		final HttpEmInstaller installer = newInstaller();
		final Expectation x = new Expectation("ct", "text/html;charset=iso-8859-1");
		x.add("$return", "<DIV id=\"d1\">\n <SPAN class=\"s\">s1</SPAN><SPAN class=\"s\">s2</SPAN></DIV>");
		jsassert(x, "response_html1", installer);
	}

	@Test
	public void t50_json() {
		final HttpEmInstaller installer = newInstaller();
		final Expectation x = new Expectation("ct", "text/plain;charset=us-ascii");
		x.add("$return", "{\"a\":\"alpha\",\"b\":[3,5,7],\"c\":true}");
		jsassert(x, "response_json", installer);
	}

	@Test
	public void t55_kml() {
		final HttpEmInstaller installer = newInstaller();
		final Expectation x = new Expectation("ct", "application/vnd.google-earth.kml+xml");
		jsassert(x, "response_kml", installer);
	}

	@Test
	public void t60_redirect1() {
		final HttpEmInstaller installer = newInstaller();
		final Expectation x = new Expectation("$return", "302 http://met.com");
		jsassert(x, "response_redirect1", installer);
	}

	@Test
	public void t65_notFound() {
		final HttpEmInstaller installer = newInstaller();
		final Expectation x = new Expectation("$throw", "HTTP404 Missing Thing");
		jsassert(x, "response_notFound", installer);
	}

	@Test
	public void t65_redirect2() {
		final HttpEmInstaller installer = newInstaller();
		final Expectation x = new Expectation("$return", "307 http://met.com");
		jsassert(x, "response_redirect2", installer);
	}

	private static HttpEmInstaller newInstaller() {
		return new HttpEmInstaller(new Request(), 1024);
	}
}
